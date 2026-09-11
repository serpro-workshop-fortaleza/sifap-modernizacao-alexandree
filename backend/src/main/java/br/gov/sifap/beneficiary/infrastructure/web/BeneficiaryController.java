package br.gov.sifap.beneficiary.infrastructure.web;

import br.gov.sifap.beneficiary.api.BeneficiaryQuery;
import br.gov.sifap.beneficiary.api.BeneficiarySummary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/beneficiaries")
@Tag(name = "Beneficiários", description = "Superfície de leitura do contexto Cadastro de Beneficiários")
class BeneficiaryController {

    private final BeneficiaryQuery beneficiaryQuery;

    BeneficiaryController(BeneficiaryQuery beneficiaryQuery) {
        this.beneficiaryQuery = beneficiaryQuery;
    }

    @GetMapping
    @Operation(
            summary = "Listar beneficiários",
            description = "Ordenados por CPF, como o READ LOGICAL de BATCHPGT.NSP:L250.")
    @ApiResponse(responseCode = "200", description = "Lista retornada")
    List<BeneficiaryResponse> list() {
        return beneficiaryQuery.list().stream().map(BeneficiaryResponse::from).toList();
    }

    record BeneficiaryResponse(
            String id,
            String cpf,
            String name,
            String status,
            String socialProgramCode,
            BigDecimal householdIncome,
            LocalDate registeredAt) {

        static BeneficiaryResponse from(BeneficiarySummary summary) {
            return new BeneficiaryResponse(
                    String.valueOf(summary.registrationNumber()),
                    summary.cpf().value(),
                    summary.name(),
                    summary.status().code(),
                    summary.programCode(),
                    summary.householdIncome().amount(),
                    summary.registeredAt());
        }
    }
}
