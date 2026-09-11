package br.gov.sifap.payment.infrastructure.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.gov.sifap.payment.api.PaymentQuery;
import br.gov.sifap.payment.api.PayrollGeneration;
import br.gov.sifap.payment.api.PayrollHistoryPoint;
import br.gov.sifap.payment.api.PayrollResult;
import br.gov.sifap.sharedkernel.domain.Competence;
import br.gov.sifap.sharedkernel.domain.Money;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PayrollRunController.class)
class PayrollRunControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PayrollGeneration payrollGeneration;

    @MockBean
    private PaymentQuery paymentQuery;

    @Test
    void should_return_201_with_return_code_zero_when_payroll_runs_without_rejections() throws Exception { // REQ-012
        when(payrollGeneration.run(any())).thenReturn(result(6, 0, 6, PayrollResult.RETURN_SUCCESS));

        mockMvc.perform(post("/api/v1/payroll-runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"competence\":\"2026-09\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.competence").value("2026-09"))
                .andExpect(jsonPath("$.paymentsIssued").value(6))
                .andExpect(jsonPath("$.ignoredBeneficiaries").value(6))
                .andExpect(jsonPath("$.returnCode").value(0));
    }

    @Test
    void should_derive_competence_from_current_date_when_body_is_empty() throws Exception { // REQ-011
        when(payrollGeneration.run(isNull())).thenReturn(result(0, 0, 0, PayrollResult.RETURN_NOTHING_GENERATED));

        mockMvc.perform(post("/api/v1/payroll-runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.returnCode").value(8));

        verify(payrollGeneration).run(isNull());
    }

    @Test
    void should_return_400_when_competence_is_malformed() throws Exception {
        mockMvc.perform(post("/api/v1/payroll-runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"competence\":\"202609\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Competencia deve estar no formato YYYY-MM"))
                .andExpect(jsonPath("$.correlationId").exists());
    }

    @Test
    void should_expose_history_in_iso_competence_format() throws Exception {
        when(paymentQuery.history()).thenReturn(List.of(
                new PayrollHistoryPoint(Competence.of("202608"), Money.of("3345.00"), 6)));

        mockMvc.perform(get("/api/v1/payroll-runs/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].competence").value("2026-08"))
                .andExpect(jsonPath("$[0].totalNetAmount").value(3345.00))
                .andExpect(jsonPath("$[0].paymentsIssued").value(6));
    }

    @Test
    void should_return_summary_without_finish_date_when_competence_was_never_executed() throws Exception {
        when(paymentQuery.currentSummary(Optional.empty())).thenReturn(new PayrollResult(
                Competence.of("202609"), 0, 0, 0, Money.ZERO, Money.ZERO,
                PayrollResult.RETURN_NOTHING_GENERATED, null));

        mockMvc.perform(get("/api/v1/payroll-runs/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.finishedAt").doesNotExist())
                .andExpect(jsonPath("$.returnCode").value(8));
    }

    private static PayrollResult result(int issued, int rejected, int ignored, int returnCode) {
        return new PayrollResult(
                Competence.of("202609"),
                issued,
                rejected,
                ignored,
                Money.of("3345.00"),
                Money.of("3345.00"),
                returnCode,
                LocalDateTime.of(2026, 9, 10, 3, 0));
    }
}
