package br.gov.sifap.payment.domain;

import br.gov.sifap.payment.api.PayrollResult;
import br.gov.sifap.sharedkernel.domain.Competence;
import br.gov.sifap.sharedkernel.domain.Money;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Resultado persistido de uma execucao da folha, incluindo o codigo de retorno. */
@Entity
@Table(name = "payroll_run")
public class PayrollRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "competence", nullable = false, length = 6)
    private String competence;

    @Column(name = "payments_issued", nullable = false)
    private Integer paymentsIssued;

    @Column(name = "payments_rejected", nullable = false)
    private Integer paymentsRejected;

    @Column(name = "ignored_beneficiaries", nullable = false)
    private Integer ignoredBeneficiaries;

    @Column(name = "total_gross_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalGrossAmount;

    @Column(name = "total_net_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalNetAmount;

    @Column(name = "return_code", nullable = false)
    private Integer returnCode;

    @Column(name = "finished_at", nullable = false)
    private LocalDateTime finishedAt;

    protected PayrollRun() {
    }

    public static PayrollRun from(PayrollResult result) {
        var run = new PayrollRun();
        run.competence = result.competence().toString();
        run.paymentsIssued = result.paymentsIssued();
        run.paymentsRejected = result.paymentsRejected();
        run.ignoredBeneficiaries = result.ignoredBeneficiaries();
        run.totalGrossAmount = result.totalGrossAmount().amount();
        run.totalNetAmount = result.totalNetAmount().amount();
        run.returnCode = result.returnCode();
        run.finishedAt = result.finishedAt();
        return run;
    }

    public PayrollResult toResult() {
        return new PayrollResult(
                Competence.of(competence),
                paymentsIssued,
                paymentsRejected,
                ignoredBeneficiaries,
                Money.of(totalGrossAmount),
                Money.of(totalNetAmount),
                returnCode,
                finishedAt);
    }
}
