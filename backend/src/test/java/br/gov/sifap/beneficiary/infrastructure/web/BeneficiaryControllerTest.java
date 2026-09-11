package br.gov.sifap.beneficiary.infrastructure.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.gov.sifap.beneficiary.api.BeneficiaryQuery;
import br.gov.sifap.beneficiary.api.BeneficiaryStatus;
import br.gov.sifap.beneficiary.api.BeneficiarySummary;
import br.gov.sifap.sharedkernel.domain.Cpf;
import br.gov.sifap.sharedkernel.domain.Money;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BeneficiaryController.class)
class BeneficiaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BeneficiaryQuery beneficiaryQuery;

    @Test
    void should_list_beneficiaries_in_the_rest_contract() throws Exception {
        when(beneficiaryQuery.list()).thenReturn(List.of(new BeneficiarySummary(
                1001L,
                Cpf.of("10000000001"),
                "Ana Paula Ribeiro",
                BeneficiaryStatus.ACTIVE,
                "P001",
                Money.of("250.00"),
                LocalDate.of(2019, 4, 2))));

        mockMvc.perform(get("/api/v1/beneficiaries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1001"))
                .andExpect(jsonPath("$[0].cpf").value("10000000001"))
                .andExpect(jsonPath("$[0].status").value("A"))
                .andExpect(jsonPath("$[0].socialProgramCode").value("P001"))
                .andExpect(jsonPath("$[0].householdIncome").value(250.00))
                .andExpect(jsonPath("$[0].registeredAt").value("2019-04-02"));
    }

    @Test
    void should_return_empty_list_when_there_is_no_beneficiary() throws Exception {
        when(beneficiaryQuery.list()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/beneficiaries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}
