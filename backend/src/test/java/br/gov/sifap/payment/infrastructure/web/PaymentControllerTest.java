package br.gov.sifap.payment.infrastructure.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.gov.sifap.payment.api.DiscountApplication;
import br.gov.sifap.payment.api.PaymentQuery;
import br.gov.sifap.payment.api.PaymentView;
import br.gov.sifap.sharedkernel.domain.Competence;
import br.gov.sifap.sharedkernel.domain.Cpf;
import br.gov.sifap.sharedkernel.domain.Money;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentQuery paymentQuery;

    @MockBean
    private DiscountApplication discountApplication;

    @Test
    void should_list_payments_of_a_competence_in_the_rest_contract() throws Exception {
        when(paymentQuery.list(eq(Optional.of(Competence.of("202608"))), anyInt()))
                .thenReturn(List.of(payment()));

        mockMvc.perform(get("/api/v1/payments").param("competence", "2026-08"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("20260800001"))
                .andExpect(jsonPath("$[0].beneficiaryCpf").value("10000000001"))
                .andExpect(jsonPath("$[0].beneficiaryName").value("Ana Paula Ribeiro"))
                .andExpect(jsonPath("$[0].competence").value("2026-08"))
                .andExpect(jsonPath("$[0].netAmount").value(720.00))
                // Traducao provisoria de 'G' enquanto a questao de projeto P3 nao for respondida.
                .andExpect(jsonPath("$[0].status").value("PENDENTE"))
                .andExpect(jsonPath("$[0].issuedAt").value("2026-08-05"));
    }

    @Test
    void should_return_400_when_competence_parameter_is_malformed() throws Exception {
        mockMvc.perform(get("/api/v1/payments").param("competence", "08/2026"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.correlationId").exists());
    }

    @Test
    void should_return_404_when_payment_does_not_exist() throws Exception {
        when(paymentQuery.findByNumber(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/payments/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void should_apply_discounts_and_return_the_updated_payment() throws Exception { // REQ-007, REQ-008
        when(discountApplication.apply(eq(20260800001L), any())).thenReturn(payment());

        mockMvc.perform(post("/api/v1/payments/20260800001/discounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"discounts":[{"type":"JD","amount":150.00,"caseNumber":"0001-22"}]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("20260800001"));
    }

    @Test
    void should_return_400_when_discount_list_is_empty() throws Exception {
        mockMvc.perform(post("/api/v1/payments/20260800001/discounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"discounts\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.correlationId").exists());
    }

    @Test
    void should_return_400_when_discount_amount_is_negative() throws Exception {
        mockMvc.perform(post("/api/v1/payments/20260800001/discounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"discounts\":[{\"type\":\"IR\",\"amount\":-1.00}]}"))
                .andExpect(status().isBadRequest());
    }

    private static PaymentView payment() {
        return new PaymentView(
                20260800001L,
                Cpf.of("10000000001"),
                "Ana Paula Ribeiro",
                "P001",
                Competence.of("202608"),
                Money.of("720.00"),
                Money.ZERO,
                Money.of("720.00"),
                "G",
                LocalDate.of(2026, 8, 5),
                List.of());
    }
}
