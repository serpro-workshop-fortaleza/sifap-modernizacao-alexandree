package br.gov.sifap.payment.infrastructure.web;

import br.gov.sifap.payment.api.DiscountApplication;
import br.gov.sifap.payment.api.PaymentQuery;
import br.gov.sifap.sharedkernel.domain.Competence;
import br.gov.sifap.sharedkernel.domain.Money;
import br.gov.sifap.sharedkernel.error.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Pagamentos", description = "Pagamentos gerados pela folha e aplicação avulsa de descontos")
class PaymentController {

    private static final int DEFAULT_LIMIT = 200;

    private final PaymentQuery paymentQuery;
    private final DiscountApplication discountApplication;

    PaymentController(PaymentQuery paymentQuery, DiscountApplication discountApplication) {
        this.paymentQuery = paymentQuery;
        this.discountApplication = discountApplication;
    }

    @GetMapping
    @Operation(
            summary = "Listar pagamentos",
            description = "Sem competência informada, devolve os mais recentes de todas as competências.")
    @ApiResponse(responseCode = "200", description = "Lista retornada")
    @ApiResponse(responseCode = "400", description = "Competência fora do formato AAAA-MM")
    List<PaymentResponse> list(@RequestParam(required = false) String competence) {
        var parsed = Optional.ofNullable(competence).map(Competence::ofIso);
        return paymentQuery.list(parsed, DEFAULT_LIMIT).stream().map(PaymentResponse::from).toList();
    }

    @GetMapping("/{paymentNumber}")
    @Operation(summary = "Consultar um pagamento")
    @ApiResponse(responseCode = "200", description = "Pagamento encontrado")
    @ApiResponse(responseCode = "404", description = "Pagamento inexistente")
    PaymentResponse findOne(@PathVariable Long paymentNumber) {
        return paymentQuery.findByNumber(paymentNumber)
                .map(PaymentResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento nao encontrado: " + paymentNumber));
    }

    @PostMapping("/{paymentNumber}/discounts")
    @Operation(
            summary = "Aplicar descontos a um pagamento",
            description = """
                    Execução avulsa. REQ-007 limita o total a 30% do valor bruto e REQ-008 isenta
                    o desconto judicial (`JD`) desse limite. Não roda dentro da folha: a questão
                    de projeto P2 permanece aberta.
                    """)
    @ApiResponse(responseCode = "200", description = "Descontos aplicados")
    @ApiResponse(responseCode = "400", description = "Requisição inválida")
    @ApiResponse(responseCode = "404", description = "Pagamento inexistente")
    PaymentResponse applyDiscounts(
            @PathVariable Long paymentNumber, @Valid @RequestBody ApplyDiscountsRequest request) {
        var discounts = request.discounts().stream()
                .map(discount -> new DiscountApplication.DiscountRequest(
                        discount.type(), Money.of(discount.amount()), discount.percentage(), discount.caseNumber()))
                .toList();
        return PaymentResponse.from(discountApplication.apply(paymentNumber, discounts));
    }

    record ApplyDiscountsRequest(
            @NotEmpty @Size(max = 8) @Valid List<DiscountRequest> discounts) {
    }

    record DiscountRequest(
            @NotBlank @Size(max = 3) String type,
            @NotNull @PositiveOrZero BigDecimal amount,
            @PositiveOrZero BigDecimal percentage,
            @Size(max = 20) String caseNumber) {
    }
}
