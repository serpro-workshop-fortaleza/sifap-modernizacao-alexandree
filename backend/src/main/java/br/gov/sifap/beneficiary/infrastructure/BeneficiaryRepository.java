package br.gov.sifap.beneficiary.infrastructure;

import br.gov.sifap.beneficiary.domain.Beneficiary;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {

    Optional<Beneficiary> findByCpf(String cpf);

    List<Beneficiary> findAllByOrderByCpfAsc();

    List<Beneficiary> findByCpfIn(Collection<String> cpfs);

    long countByProgramCode(String programCode);
}
