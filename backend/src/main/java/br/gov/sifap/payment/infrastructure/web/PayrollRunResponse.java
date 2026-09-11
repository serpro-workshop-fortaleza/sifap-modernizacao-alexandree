package br.gov.sifap.payment.infrastructure.web;

import br.gov.sifap.payment.api.PayrollHistoryPoint;
import br.gov.sifap.payment.api.PayrollResult;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Contrato REST do resumo de uma execucao da folha. */
record PayrollRunResponse(
        String competence,
        int paymentsIssued,
        int paymentsRejected,
        int ignoredBeneficiaries,
        BigDecimal totalGrossAmount,
        BigDecimal totalNetAmount,
        int returnCode,
        LocalDateTime finishedAt) {

    static PayrollRunResponse from(PayrollResult result) {
        return new PayrollRunResponse(
                result.competence().toIsoString(),
                result.paymentsIssued(),
                result.paymentsRejected(),
                result.ignoredBeneficiaries(),
                result.totalGrossAmount().amount(),
                result.totalNetAmount().amount(),
                result.returnCode(),
                result.finishedAt());
    }

    record HistoryPoint(String competence, BigDecimal totalNetAmount, long paymentsIssued) {

        static HistoryPoint from(PayrollHistoryPoint point) {
            return new HistoryPoint(
                    point.competence().toIsoString(),
                    point.totalNetAmount().amount(),
                    point.paymentsIssued());
        }
    }
}
