package br.gov.sifap.payment.infrastructure.web;

import br.gov.sifap.payment.api.PaymentView;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Contrato REST de um pagamento. */
record PaymentResponse(
        String id,
        String beneficiaryCpf,
        String beneficiaryName,
        String competence,
        BigDecimal grossAmount,
        List<DiscountResponse> discounts,
        BigDecimal netAmount,
        String status,
        LocalDate issuedAt) {

    static PaymentResponse from(PaymentView view) {
        return new PaymentResponse(
                String.valueOf(view.paymentNumber()),
                view.cpf().value(),
                view.beneficiaryName(),
                view.competence().toIsoString(),
                view.grossAmount().amount(),
                view.discounts().stream().map(DiscountResponse::from).toList(),
                view.netAmount().amount(),
                PaymentStatusView.of(view.status()),
                view.generatedAt());
    }

    record DiscountResponse(String type, BigDecimal amount, boolean judicial) {

        static DiscountResponse from(PaymentView.DiscountView discount) {
            return new DiscountResponse(discount.type(), discount.amount().amount(), discount.judicial());
        }
    }
}
