package br.gov.sifap.payment.infrastructure;

import br.gov.sifap.payment.domain.PayrollRun;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayrollRunRepository extends JpaRepository<PayrollRun, Long> {

    Optional<PayrollRun> findFirstByCompetenceOrderByFinishedAtDesc(String competence);
}
