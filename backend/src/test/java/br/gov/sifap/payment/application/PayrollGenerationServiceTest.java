package br.gov.sifap.payment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.gov.sifap.audit.api.AuditEntry;
import br.gov.sifap.audit.api.AuditTrail;
import br.gov.sifap.beneficiary.api.BeneficiaryQuery;
import br.gov.sifap.beneficiary.api.BeneficiarySnapshot;
import br.gov.sifap.beneficiary.api.BeneficiaryStatus;
import br.gov.sifap.payment.api.PayrollResult;
import br.gov.sifap.payment.domain.BenefitCalculator;
import br.gov.sifap.payment.domain.EligibilityPolicy;
import br.gov.sifap.payment.domain.Payment;
import br.gov.sifap.payment.domain.PayrollRun;
import br.gov.sifap.payment.infrastructure.PaymentNumberSequence;
import br.gov.sifap.payment.infrastructure.PaymentRepository;
import br.gov.sifap.payment.infrastructure.PayrollRunRepository;
import br.gov.sifap.sharedkernel.domain.Competence;
import br.gov.sifap.sharedkernel.domain.Cpf;
import br.gov.sifap.sharedkernel.domain.Money;
import br.gov.sifap.socialprogram.api.IncomeBand;
import br.gov.sifap.socialprogram.api.ProgramParameters;
import br.gov.sifap.socialprogram.api.SocialProgramCatalog;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PayrollGenerationServiceTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-09-11T12:00:00Z"), ZoneOffset.UTC);

    @Mock
    private BeneficiaryQuery beneficiaryQuery;

    @Mock
    private SocialProgramCatalog socialProgramCatalog;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PayrollRunRepository payrollRunRepository;

    @Mock
    private PaymentNumberSequence paymentNumberSequence;

    @Mock
    private AuditTrail auditTrail;

    @Mock
    private TransactionTemplate transactionTemplate;

    private PayrollGenerationService service;

    @BeforeEach
    void setUp() {
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            TransactionStatus status = new SimpleTransactionStatus();
            return callback.doInTransaction(status);
        });
        when(paymentNumberSequence.next()).thenReturn(1L, 2L, 3L);
        when(socialProgramCatalog.findByCode("P001")).thenReturn(Optional.of(activeProgram()));

        service = new PayrollGenerationService(
                beneficiaryQuery,
                socialProgramCatalog,
                paymentRepository,
                payrollRunRepository,
                paymentNumberSequence,
                new EligibilityPolicy(),
                BenefitCalculator.withConfirmedFactorsOnly(),
                auditTrail,
                transactionTemplate,
                FIXED_CLOCK);
    }

    @Test
    void should_derive_competence_from_current_date_when_none_is_given() { // REQ-011
        when(beneficiaryQuery.findAllOrderedByCpf()).thenReturn(List.of());

        var result = service.run(null);

        assertThat(result.competence()).isEqualTo(Competence.of("202609"));
    }

    @Test
    void should_use_the_given_competence_over_the_current_date() { // REQ-011
        when(beneficiaryQuery.findAllOrderedByCpf()).thenReturn(List.of());

        var result = service.run(Competence.of("202605"));

        assertThat(result.competence()).isEqualTo(Competence.of("202605"));
    }

    @Test
    void should_ignore_beneficiary_without_active_status() { // REQ-009
        when(beneficiaryQuery.findAllOrderedByCpf())
                .thenReturn(List.of(beneficiary("10000000004", BeneficiaryStatus.SUSPENDED, "250.00")));

        var result = service.run(Competence.of("202609"));

        assertThat(result.ignoredBeneficiaries()).isEqualTo(1);
        assertThat(result.paymentsIssued()).isZero();
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void should_ignore_beneficiary_already_paid_in_the_competence() { // REQ-010
        when(beneficiaryQuery.findAllOrderedByCpf())
                .thenReturn(List.of(beneficiary("10000000001", BeneficiaryStatus.ACTIVE, "250.00")));
        when(paymentRepository.existsFor(any(), any())).thenReturn(true);

        var result = service.run(Competence.of("202609"));

        assertThat(result.ignoredBeneficiaries()).isEqualTo(1);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void should_issue_payment_and_record_audit_for_eligible_beneficiary() { // REQ-005, REQ-014
        when(beneficiaryQuery.findAllOrderedByCpf())
                .thenReturn(List.of(beneficiary("10000000001", BeneficiaryStatus.ACTIVE, "250.00")));
        when(paymentRepository.existsFor(any(), any())).thenReturn(false);

        var result = service.run(Competence.of("202609"));

        assertThat(result.paymentsIssued()).isEqualTo(1);
        assertThat(result.returnCode()).isEqualTo(PayrollResult.RETURN_SUCCESS);
        assertThat(result.totalNetAmount().amount()).isEqualByComparingTo("720.00");

        var saved = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(saved.capture());
        assertThat(saved.getValue().generatedAt()).isEqualTo(LocalDate.of(2026, 9, 11));
        assertThat(saved.getValue().status()).isEqualTo("G");

        var audit = ArgumentCaptor.forClass(AuditEntry.class);
        verify(auditTrail).record(audit.capture());
        assertThat(audit.getValue().action()).isEqualTo("BT");
        assertThat(audit.getValue().entityType()).isEqualTo("Payment");
    }

    @Test
    void should_reject_beneficiary_whose_program_does_not_exist() { // REQ-012
        when(beneficiaryQuery.findAllOrderedByCpf())
                .thenReturn(List.of(beneficiary("10000000001", BeneficiaryStatus.ACTIVE, "250.00", "P999")));
        when(socialProgramCatalog.findByCode("P999")).thenReturn(Optional.empty());

        var result = service.run(Competence.of("202609"));

        assertThat(result.paymentsRejected()).isEqualTo(1);
        assertThat(result.returnCode()).isEqualTo(PayrollResult.RETURN_NOTHING_GENERATED);
    }

    @Test
    void should_return_4_when_there_are_issued_and_rejected_payments() { // REQ-012
        when(beneficiaryQuery.findAllOrderedByCpf()).thenReturn(List.of(
                beneficiary("10000000001", BeneficiaryStatus.ACTIVE, "250.00"),
                beneficiary("10000000002", BeneficiaryStatus.ACTIVE, "250.00", "P999")));
        when(socialProgramCatalog.findByCode("P999")).thenReturn(Optional.empty());
        when(paymentRepository.existsFor(any(), any())).thenReturn(false);

        var result = service.run(Competence.of("202609"));

        assertThat(result.paymentsIssued()).isEqualTo(1);
        assertThat(result.paymentsRejected()).isEqualTo(1);
        assertThat(result.returnCode()).isEqualTo(PayrollResult.RETURN_WITH_REJECTIONS);
    }

    @Test
    void should_ignore_beneficiary_rejected_by_eligibility() { // REQ-003
        when(beneficiaryQuery.findAllOrderedByCpf())
                .thenReturn(List.of(beneficiary("10000000005", BeneficiaryStatus.ACTIVE, "5000.00")));

        var result = service.run(Competence.of("202609"));

        assertThat(result.ignoredBeneficiaries()).isEqualTo(1);
        assertThat(result.paymentsIssued()).isZero();
    }

    @Test
    void should_return_12_and_persist_the_run_when_execution_fails() { // REQ-013
        doThrow(new IllegalStateException("falha simulada")).when(transactionTemplate).execute(any());

        var result = service.run(Competence.of("202609"));

        assertThat(result.returnCode()).isEqualTo(PayrollResult.RETURN_EXECUTION_ERROR);
        assertThat(result.paymentsIssued()).isZero();
        verify(payrollRunRepository, times(1)).save(any(PayrollRun.class));
    }

    private static BeneficiarySnapshot beneficiary(String cpf, BeneficiaryStatus status, String income) {
        return beneficiary(cpf, status, income, "P001");
    }

    private static BeneficiarySnapshot beneficiary(
            String cpf, BeneficiaryStatus status, String income, String programCode) {
        return new BeneficiarySnapshot(
                Cpf.of(cpf),
                1L,
                status,
                Money.of(income),
                LocalDate.of(1980, 1, 1),
                0,
                programCode,
                "01");
    }

    private static ProgramParameters activeProgram() {
        return new ProgramParameters(
                "P001",
                "Renda Cidada",
                true,
                Money.of("600.00"),
                Money.of("1200.00"),
                List.of(new IncomeBand(Money.of("300.00"), new BigDecimal("1.20")),
                        new IncomeBand(Money.of("999999.99"), new BigDecimal("0.90"))),
                BigDecimal.ZERO);
    }
}
