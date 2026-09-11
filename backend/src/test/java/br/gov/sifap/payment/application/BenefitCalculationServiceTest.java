package br.gov.sifap.payment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import br.gov.sifap.beneficiary.api.BeneficiaryQuery;
import br.gov.sifap.beneficiary.api.BeneficiarySnapshot;
import br.gov.sifap.beneficiary.api.BeneficiaryStatus;
import br.gov.sifap.payment.api.CalculationResult;
import br.gov.sifap.payment.api.EligibilityDecision;
import br.gov.sifap.payment.domain.BenefitCalculator;
import br.gov.sifap.payment.domain.EligibilityPolicy;
import br.gov.sifap.sharedkernel.domain.Competence;
import br.gov.sifap.sharedkernel.domain.Cpf;
import br.gov.sifap.sharedkernel.domain.Money;
import br.gov.sifap.sharedkernel.error.ResourceNotFoundException;
import br.gov.sifap.socialprogram.api.IncomeBand;
import br.gov.sifap.socialprogram.api.ProgramParameters;
import br.gov.sifap.socialprogram.api.SocialProgramCatalog;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BenefitCalculationServiceTest {

    private static final Cpf CPF = Cpf.of("10000000001");
    private static final Competence COMPETENCE = Competence.of("202609");

    @Mock
    private BeneficiaryQuery beneficiaryQuery;

    @Mock
    private SocialProgramCatalog socialProgramCatalog;

    private BenefitCalculationService service;

    @BeforeEach
    void setUp() {
        service = new BenefitCalculationService(
                beneficiaryQuery,
                socialProgramCatalog,
                new EligibilityPolicy(),
                BenefitCalculator.withConfirmedFactorsOnly());
    }

    @Test
    void should_calculate_when_beneficiary_is_eligible() { // REQ-005, REQ-006
        when(beneficiaryQuery.findByCpf(CPF)).thenReturn(Optional.of(beneficiary(BeneficiaryStatus.ACTIVE, "250.00")));
        when(socialProgramCatalog.findByCode("P001")).thenReturn(Optional.of(program(true)));

        var result = service.calculate(CPF, COMPETENCE);

        assertThat(result.isSuccessful()).isTrue();
        assertThat(result.grossAmount().amount()).isEqualByComparingTo("720.00");
        assertThat(result.netAmount()).isEqualTo(result.grossAmount());
    }

    @Test
    void should_return_2004_when_program_is_inactive() { // REQ-001
        when(beneficiaryQuery.findByCpf(CPF)).thenReturn(Optional.of(beneficiary(BeneficiaryStatus.ACTIVE, "250.00")));
        when(socialProgramCatalog.findByCode("P001")).thenReturn(Optional.of(program(false)));

        var result = service.calculate(CPF, COMPETENCE);

        assertThat(result.returnCode()).isEqualTo(EligibilityDecision.RETURN_PROGRAM_INACTIVE);
        assertThat(result.grossAmount()).isNull();
    }

    @Test
    void should_not_calculate_when_beneficiary_is_ineligible() { // REQ-002, REQ-004
        when(beneficiaryQuery.findByCpf(CPF))
                .thenReturn(Optional.of(beneficiary(BeneficiaryStatus.SUSPENDED, "250.00")));
        when(socialProgramCatalog.findByCode("P001")).thenReturn(Optional.of(program(true)));

        var result = service.calculate(CPF, COMPETENCE);

        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.message()).isEqualTo("Beneficiario suspenso");
        assertThat(result.netAmountIfCalculated()).isEmpty();
    }

    @Test
    void should_fail_when_beneficiary_does_not_exist() {
        when(beneficiaryQuery.findByCpf(CPF)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.calculate(CPF, COMPETENCE))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Beneficiario nao encontrado");
    }

    @Test
    void should_fail_when_program_does_not_exist() {
        when(beneficiaryQuery.findByCpf(CPF)).thenReturn(Optional.of(beneficiary(BeneficiaryStatus.ACTIVE, "250.00")));
        when(socialProgramCatalog.findByCode("P001")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.calculate(CPF, COMPETENCE))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("P001");
    }

    @Test
    void should_expose_2002_constant_for_inactive_beneficiary_at_calculation() { // REQ-004
        assertThat(CalculationResult.RETURN_BENEFICIARY_NOT_ACTIVE).isEqualTo(2002);
    }

    private static BeneficiarySnapshot beneficiary(BeneficiaryStatus status, String income) {
        return new BeneficiarySnapshot(
                CPF, 1L, status, Money.of(income), LocalDate.of(1980, 1, 1), 0, "P001", "01");
    }

    private static ProgramParameters program(boolean active) {
        return new ProgramParameters(
                "P001",
                "Renda Cidada",
                active,
                Money.of("600.00"),
                Money.of("1200.00"),
                List.of(new IncomeBand(Money.of("300.00"), new BigDecimal("1.20")),
                        new IncomeBand(Money.of("999999.99"), new BigDecimal("0.90"))),
                BigDecimal.ZERO);
    }
}
