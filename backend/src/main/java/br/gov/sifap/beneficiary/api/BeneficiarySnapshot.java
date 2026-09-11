package br.gov.sifap.beneficiary.api;

import br.gov.sifap.sharedkernel.domain.Cpf;
import br.gov.sifap.sharedkernel.domain.Money;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Projecao de leitura consumida pelo contexto de Pagamento de Beneficios. Nunca expoe
 * a entidade JPA do cadastro.
 *
 * <p>{@code consideredIncome} carrega a renda familiar total, como em VALELEG.NSN:L174-L180.
 * A questao de projeto P1 (familiar total x per capita) permanece aberta e nao pode ser
 * fechada por escolha de implementacao.
 */
public record BeneficiarySnapshot(
        Cpf cpf,
        Long registrationNumber,
        BeneficiaryStatus status,
        Money consideredIncome,
        LocalDate birthDate,
        int dependentCount,
        String programCode,
        String regionCode) {

    public BeneficiarySnapshot {
        Objects.requireNonNull(cpf, "CPF nao pode ser nulo");
        Objects.requireNonNull(registrationNumber, "Numero de inscricao nao pode ser nulo");
        Objects.requireNonNull(status, "Situacao nao pode ser nula");
        Objects.requireNonNull(consideredIncome, "Renda considerada nao pode ser nula");
        Objects.requireNonNull(birthDate, "Data de nascimento nao pode ser nula");
        Objects.requireNonNull(programCode, "Codigo do programa nao pode ser nulo");
        Objects.requireNonNull(regionCode, "Codigo da regiao nao pode ser nulo");
    }
}
