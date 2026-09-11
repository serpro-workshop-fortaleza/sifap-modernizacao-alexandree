package br.gov.sifap.payment.application;

import br.gov.sifap.audit.api.AuditEntry;
import br.gov.sifap.audit.api.AuditTrail;
import br.gov.sifap.beneficiary.api.BeneficiaryQuery;
import br.gov.sifap.beneficiary.api.BeneficiarySnapshot;
import br.gov.sifap.payment.api.PayrollGeneration;
import br.gov.sifap.payment.api.PayrollResult;
import br.gov.sifap.payment.domain.BenefitCalculator;
import br.gov.sifap.payment.domain.EligibilityPolicy;
import br.gov.sifap.payment.domain.Payment;
import br.gov.sifap.payment.domain.PayrollRun;
import br.gov.sifap.payment.infrastructure.PaymentNumberSequence;
import br.gov.sifap.payment.infrastructure.PaymentRepository;
import br.gov.sifap.payment.infrastructure.PayrollRunRepository;
import br.gov.sifap.sharedkernel.domain.Competence;
import br.gov.sifap.sharedkernel.domain.Money;
import br.gov.sifap.socialprogram.api.ProgramParameters;
import br.gov.sifap.socialprogram.api.SocialProgramCatalog;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
class PayrollGenerationService implements PayrollGeneration {

    private static final Logger log = LoggerFactory.getLogger(PayrollGenerationService.class);
    private static final String PAYMENT_GENERATED_STATUS = "G";
    private static final String BATCH_AUDIT_ACTION = "BT";
    private static final int SINGLE_CYCLE = 1;

    private final BeneficiaryQuery beneficiaryQuery;
    private final SocialProgramCatalog socialProgramCatalog;
    private final PaymentRepository paymentRepository;
    private final PayrollRunRepository payrollRunRepository;
    private final PaymentNumberSequence paymentNumberSequence;
    private final EligibilityPolicy eligibilityPolicy;
    private final BenefitCalculator benefitCalculator;
    private final AuditTrail auditTrail;
    private final TransactionTemplate transactionTemplate;
    private final Clock clock;

    PayrollGenerationService(
            BeneficiaryQuery beneficiaryQuery,
            SocialProgramCatalog socialProgramCatalog,
            PaymentRepository paymentRepository,
            PayrollRunRepository payrollRunRepository,
            PaymentNumberSequence paymentNumberSequence,
            EligibilityPolicy eligibilityPolicy,
            BenefitCalculator benefitCalculator,
            AuditTrail auditTrail,
            TransactionTemplate transactionTemplate,
            Clock clock) {
        this.beneficiaryQuery = beneficiaryQuery;
        this.socialProgramCatalog = socialProgramCatalog;
        this.paymentRepository = paymentRepository;
        this.payrollRunRepository = payrollRunRepository;
        this.paymentNumberSequence = paymentNumberSequence;
        this.eligibilityPolicy = eligibilityPolicy;
        this.benefitCalculator = benefitCalculator;
        this.auditTrail = auditTrail;
        this.transactionTemplate = transactionTemplate;
        this.clock = clock;
    }

    @Override
    public PayrollResult run(Competence requested) {
        // REQ-011: competencia ausente e derivada do ano e mes correntes.
        var competence = requested == null ? Competence.current(clock) : requested;
        PayrollResult result;
        try {
            result = transactionTemplate.execute(status -> generate(competence));
        } catch (RuntimeException exception) {
            // REQ-013: a transacao ja foi desfeita pelo TransactionTemplate; resta encerrar com 12.
            log.error("Folha interrompida por erro de execucao competencia={}", competence, exception);
            result = PayrollResult.executionError(competence, LocalDateTime.now(clock));
        }
        payrollRunRepository.save(PayrollRun.from(result));
        return result;
    }

    private PayrollResult generate(Competence competence) {
        var issued = 0;
        var rejected = 0;
        var ignored = 0;
        var totalGross = Money.ZERO;
        var totalNet = Money.ZERO;
        var generatedAt = LocalDate.now(clock);

        for (BeneficiarySnapshot beneficiary : beneficiaryQuery.findAllOrderedByCpf()) {
            // REQ-009
            if (!beneficiary.status().isActive()) {
                ignored++;
                continue;
            }

            // REQ-010
            if (paymentRepository.existsFor(beneficiary.cpf(), competence)) {
                ignored++;
                continue;
            }

            Optional<ProgramParameters> program = socialProgramCatalog.findByCode(beneficiary.programCode());
            if (program.isEmpty()) {
                log.warn("Programa inexistente na folha competencia={} programa={}",
                        competence, beneficiary.programCode());
                rejected++;
                continue;
            }

            // REQ-001, REQ-002, REQ-003
            var decision = eligibilityPolicy.evaluate(beneficiary, program.orElseThrow());
            if (!decision.eligible()) {
                ignored++;
                continue;
            }

            // REQ-004, REQ-005, REQ-006
            var calculation = benefitCalculator.calculate(beneficiary, program.orElseThrow());
            if (!calculation.isSuccessful()) {
                rejected++;
                continue;
            }

            var payment = new Payment(
                    paymentNumberSequence.next(),
                    beneficiary.cpf(),
                    beneficiary.registrationNumber(),
                    beneficiary.programCode(),
                    competence,
                    SINGLE_CYCLE,
                    calculation.grossAmount().amount(),
                    calculation.netAmount().amount(),
                    calculation.discountTotal().amount(),
                    calculation.bonusAmount().amount(),
                    PAYMENT_GENERATED_STATUS,
                    generatedAt);
            paymentRepository.save(payment);

            // REQ-014: sem trilha nao ha operacao valida; a escrita cai junto no rollback.
            auditTrail.record(new AuditEntry(
                    BATCH_AUDIT_ACTION,
                    "payment",
                    "Payment",
                    String.valueOf(payment.paymentNumber()),
                    "folha-automatica",
                    LocalDateTime.now(clock)));

            issued++;
            totalGross = totalGross.add(calculation.grossAmount());
            totalNet = totalNet.add(calculation.netAmount());
        }

        var returnCode = PayrollResult.resolveReturnCode(issued, rejected);
        log.info("Folha concluida competencia={} emitidos={} rejeitados={} ignorados={} retorno={}",
                competence, issued, rejected, ignored, returnCode);
        return new PayrollResult(
                competence, issued, rejected, ignored, totalGross, totalNet, returnCode, LocalDateTime.now(clock));
    }
}
