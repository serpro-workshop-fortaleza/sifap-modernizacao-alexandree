package br.gov.sifap.beneficiary.api;

import br.gov.sifap.sharedkernel.domain.Cpf;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Unica porta de entrada de outros modulos no contexto Cadastro de Beneficiarios.
 * Somente leitura nesta feature: nenhum consumidor grava em BENEFIC.
 */
public interface BeneficiaryQuery {

    Optional<BeneficiarySnapshot> findByCpf(Cpf cpf);

    /**
     * Ordem por CPF, como o READ LOGICAL BY NUM-CPF de BATCHPGT.NSP:L250. A ordem real
     * da qual os sistemas a jusante dependem permanece questao em aberto.
     */
    List<BeneficiarySnapshot> findAllOrderedByCpf();

    List<BeneficiarySummary> list();

    List<BeneficiarySummary> findSummariesByCpf(Collection<Cpf> cpfs);

    long countByProgramCode(String programCode);
}
