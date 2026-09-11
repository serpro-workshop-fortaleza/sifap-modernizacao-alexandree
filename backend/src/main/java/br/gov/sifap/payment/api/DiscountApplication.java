package br.gov.sifap.payment.api;

import br.gov.sifap.sharedkernel.domain.Money;
import java.math.BigDecimal;
import java.util.List;

/**
 * Aplicacao avulsa de descontos sobre um pagamento ja gravado (REQ-007, REQ-008).
 *
 * <p>Deliberadamente fora de {@link PayrollGeneration}: a questao de projeto P2 — se os
 * descontos valem durante a folha ou apenas em execucao avulsa — permanece aberta, e
 * nenhum chamador de CALCDSCT.NSP existe no corpus legado.
 */
public interface DiscountApplication {

    PaymentView apply(Long paymentNumber, List<DiscountRequest> discounts);

    record DiscountRequest(String type, Money amount, BigDecimal percentage, String caseNumber) {
    }
}
