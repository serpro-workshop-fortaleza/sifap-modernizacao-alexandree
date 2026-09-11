package br.gov.sifap.payment.api;

import br.gov.sifap.sharedkernel.domain.Competence;
import br.gov.sifap.sharedkernel.domain.Money;

/** Ponto da serie historica da folha, agregado a partir dos pagamentos gravados. */
public record PayrollHistoryPoint(Competence competence, Money totalNetAmount, long paymentsIssued) {
}
