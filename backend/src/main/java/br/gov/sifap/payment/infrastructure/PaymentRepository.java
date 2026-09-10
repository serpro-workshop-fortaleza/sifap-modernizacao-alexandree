package br.gov.sifap.payment.infrastructure;

import br.gov.sifap.payment.domain.Payment;
import br.gov.sifap.sharedkernel.domain.Cpf;
import br.gov.sifap.sharedkernel.domain.Competence;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByCpfAndCompetence(String cpf, String competence);

    default Optional<Payment> findByCpfAndCompetence(Cpf cpf, Competence competence) {
        return findByCpfAndCompetence(cpf.value(), competence.toString());
    }
}
