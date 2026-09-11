package br.gov.sifap.socialprogram.infrastructure.web;

import br.gov.sifap.beneficiary.api.BeneficiaryQuery;
import br.gov.sifap.socialprogram.api.ProgramParameters;
import br.gov.sifap.socialprogram.api.SocialProgramCatalog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/social-programs")
@Tag(name = "Programas sociais", description = "Superfície de leitura do Catálogo de Programas Sociais")
class SocialProgramController {

    private final SocialProgramCatalog socialProgramCatalog;
    private final BeneficiaryQuery beneficiaryQuery;

    SocialProgramController(SocialProgramCatalog socialProgramCatalog, BeneficiaryQuery beneficiaryQuery) {
        this.socialProgramCatalog = socialProgramCatalog;
        this.beneficiaryQuery = beneficiaryQuery;
    }

    @GetMapping
    @Operation(
            summary = "Listar programas sociais",
            description = """
                    A contagem de beneficiários é composta aqui, na camada de aplicação:
                    o catálogo não consulta a tabela do cadastro.
                    """)
    @ApiResponse(responseCode = "200", description = "Lista retornada")
    List<SocialProgramResponse> list() {
        return socialProgramCatalog.list().stream()
                .map(program -> SocialProgramResponse.from(
                        program, beneficiaryQuery.countByProgramCode(program.programCode())))
                .toList();
    }

    record SocialProgramResponse(
            String code,
            String name,
            boolean active,
            BigDecimal maxPerCapitaIncome,
            long beneficiaryCount) {

        static SocialProgramResponse from(ProgramParameters program, long beneficiaryCount) {
            return new SocialProgramResponse(
                    program.programCode(),
                    program.name(),
                    program.active(),
                    program.maxIncome().amount(),
                    beneficiaryCount);
        }
    }
}
