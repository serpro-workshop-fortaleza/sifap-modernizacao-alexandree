package br.gov.sifap.payment.infrastructure.web;

import br.gov.sifap.payment.api.PaymentQuery;
import br.gov.sifap.payment.api.PayrollGeneration;
import br.gov.sifap.sharedkernel.domain.Competence;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payroll-runs")
@Tag(name = "Folha", description = "Execução e acompanhamento da folha da competência")
class PayrollRunController {

    private final PayrollGeneration payrollGeneration;
    private final PaymentQuery paymentQuery;

    PayrollRunController(PayrollGeneration payrollGeneration, PaymentQuery paymentQuery) {
        this.payrollGeneration = payrollGeneration;
        this.paymentQuery = paymentQuery;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Executar a folha de uma competência",
            description = """
                    Competência ausente é derivada do ano e do mês correntes (REQ-011).
                    O corpo de resposta traz o código de retorno 0, 4, 8 ou 12 (REQ-012, REQ-013).
                    Beneficiário sem situação ativa é ignorado (REQ-009) e competência já
                    processada para o CPF não gera novo pagamento (REQ-010).
                    """)
    @ApiResponse(responseCode = "201", description = "Folha executada")
    @ApiResponse(responseCode = "400", description = "Competência fora do formato AAAA-MM")
    PayrollRunResponse run(@Valid @RequestBody(required = false) RunPayrollRequest request) {
        var competence = Optional.ofNullable(request)
                .map(RunPayrollRequest::competence)
                .filter(value -> !value.isBlank())
                .map(Competence::ofIso)
                .orElse(null);
        return PayrollRunResponse.from(payrollGeneration.run(competence));
    }

    @GetMapping("/current")
    @Operation(
            summary = "Consultar o resumo da competência",
            description = """
                    Descreve a competência, não a última tentativa: emitidos e totais vêm dos
                    pagamentos gravados; rejeitados, ignorados e `finishedAt` vêm da última
                    execução registrada, e `finishedAt` fica nulo quando nunca houve execução.
                    """)
    @ApiResponse(responseCode = "200", description = "Resumo retornado")
    PayrollRunResponse current(@RequestParam(required = false) String competence) {
        var parsed = Optional.ofNullable(competence)
                .filter(value -> !value.isBlank())
                .map(Competence::ofIso);
        return PayrollRunResponse.from(paymentQuery.currentSummary(parsed));
    }

    @GetMapping("/history")
    @Operation(
            summary = "Consultar a série histórica da folha",
            description = "Agregada a partir dos pagamentos gravados, em ordem crescente de competência.")
    @ApiResponse(responseCode = "200", description = "Série retornada")
    List<PayrollRunResponse.HistoryPoint> history() {
        return paymentQuery.history().stream().map(PayrollRunResponse.HistoryPoint::from).toList();
    }

    record RunPayrollRequest(String competence) {
    }
}
