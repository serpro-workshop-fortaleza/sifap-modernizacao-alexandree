package br.gov.sifap.beneficiary.api;

import br.gov.sifap.sharedkernel.domain.Cpf;
import br.gov.sifap.sharedkernel.domain.Money;
import java.time.LocalDate;

/** Modelo de leitura do cadastro para listagem e composicao de relatorio. */
public record BeneficiarySummary(
        Long registrationNumber,
        Cpf cpf,
        String name,
        BeneficiaryStatus status,
        String programCode,
        Money householdIncome,
        LocalDate registeredAt) {
}
