package br.gov.sifap.payment.api;

import br.gov.sifap.sharedkernel.domain.Competence;
import br.gov.sifap.sharedkernel.domain.Cpf;
import br.gov.sifap.sharedkernel.domain.Money;
import java.time.LocalDate;
import java.util.List;

/** Modelo de leitura de um pagamento, com o nome do beneficiario resolvido na composicao. */
public record PaymentView(
        Long paymentNumber,
        Cpf cpf,
        String beneficiaryName,
        String programCode,
        Competence competence,
        Money grossAmount,
        Money discountTotal,
        Money netAmount,
        String status,
        LocalDate generatedAt,
        List<DiscountView> discounts) {

    public record DiscountView(String type, Money amount, boolean judicial) {
    }
}
