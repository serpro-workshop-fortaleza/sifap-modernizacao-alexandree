package br.gov.sifap.payment.api;

import br.gov.sifap.sharedkernel.domain.Competence;
import br.gov.sifap.sharedkernel.domain.Money;
import java.time.LocalDateTime;
import java.util.Objects;

/** Resultado da geracao da folha; {@code returnCode} segue REQ-012 e REQ-013. */
public record PayrollResult(
        Competence competence,
        int paymentsIssued,
        int paymentsRejected,
        int ignoredBeneficiaries,
        Money totalGrossAmount,
        Money totalNetAmount,
        int returnCode,
        LocalDateTime finishedAt) {

    public static final int RETURN_SUCCESS = 0;
    public static final int RETURN_WITH_REJECTIONS = 4;
    public static final int RETURN_NOTHING_GENERATED = 8;
    public static final int RETURN_EXECUTION_ERROR = 12;

    public PayrollResult {
        Objects.requireNonNull(competence, "Competencia nao pode ser nula");
        Objects.requireNonNull(totalGrossAmount, "Total bruto nao pode ser nulo");
        Objects.requireNonNull(totalNetAmount, "Total liquido nao pode ser nulo");
    }

    /** REQ-012: 8 sem pagamento gerado, 4 com rejeicao, 0 quando nao houve rejeicao. */
    public static int resolveReturnCode(int issued, int rejected) {
        if (issued == 0) {
            return RETURN_NOTHING_GENERATED;
        }
        return rejected > 0 ? RETURN_WITH_REJECTIONS : RETURN_SUCCESS;
    }

    public static PayrollResult executionError(Competence competence, LocalDateTime finishedAt) {
        return new PayrollResult(competence, 0, 0, 0, Money.ZERO, Money.ZERO, RETURN_EXECUTION_ERROR, finishedAt);
    }
}
