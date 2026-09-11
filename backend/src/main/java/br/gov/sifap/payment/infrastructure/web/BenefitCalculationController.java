package br.gov.sifap.payment.infrastructure.web;

import br.gov.sifap.payment.api.BenefitCalculation;
import br.gov.sifap.payment.api.CalculationResult;
import br.gov.sifap.sharedkernel.domain.Competence;
import br.gov.sifap.sharedkernel.domain.Cpf;
import br.gov.sifap.sharedkernel.domain.Money;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/benefit-calculations")
@Tag(name = "Cálculo de benefício", description = "Avaliação de elegibilidade e cálculo do valor")
class BenefitCalculationController {

    private final BenefitCalculation benefitCalculation;

    BenefitCalculationController(BenefitCalculation benefitCalculation) {
        this.benefitCalculation = benefitCalculation;
    }

    @PostMapping
    @Operation(
            summary = "Calcular o benefício de um beneficiário em uma competência",
            description = """
                    Avalia a elegibilidade (REQ-001, REQ-002, REQ-003) e, quando elegível, calcula
                    o valor com o fator da faixa de renda (REQ-005), truncado em duas casas (REQ-006).
                    Beneficiário sem situação ativa devolve o código 2002 sem produzir valor (REQ-004).
                    Não grava pagamento: quem grava é a folha.
                    """)
    @ApiResponse(responseCode = "200", description = "Cálculo avaliado, com ou sem valor")
    @ApiResponse(responseCode = "400", description = "CPF ou competência inválidos")
    @ApiResponse(responseCode = "404", description = "Beneficiário ou programa inexistente")
    CalculationResponse calculate(@Valid @RequestBody CalculationRequest request) {
        var result = benefitCalculation.calculate(
                Cpf.of(request.cpf()), Competence.ofIso(request.competence()));
        return CalculationResponse.from(result);
    }

    record CalculationRequest(
            @NotBlank @Pattern(regexp = "\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}") String cpf,
            @NotBlank @Pattern(regexp = "\\d{4}-\\d{2}") String competence) {
    }

    record CalculationResponse(
            int returnCode,
            String message,
            boolean calculated,
            BigDecimal grossAmount,
            BigDecimal discountTotal,
            BigDecimal netAmount,
            BigDecimal bonusAmount) {

        static CalculationResponse from(CalculationResult result) {
            return new CalculationResponse(
                    result.returnCode(),
                    result.message(),
                    result.isSuccessful(),
                    amountOf(result.grossAmount()),
                    amountOf(result.discountTotal()),
                    amountOf(result.netAmount()),
                    amountOf(result.bonusAmount()));
        }

        private static BigDecimal amountOf(Money money) {
            return money == null ? null : money.amount();
        }
    }
}
