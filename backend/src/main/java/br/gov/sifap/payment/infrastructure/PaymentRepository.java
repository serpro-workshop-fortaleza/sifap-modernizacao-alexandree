package br.gov.sifap.payment.infrastructure;

import br.gov.sifap.payment.domain.Payment;
import br.gov.sifap.sharedkernel.domain.Competence;
import br.gov.sifap.sharedkernel.domain.Cpf;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByCpfAndCompetence(String cpf, String competence);

    default Optional<Payment> findByCpfAndCompetence(Cpf cpf, Competence competence) {
        return findByCpfAndCompetence(cpf.value(), competence.toString());
    }

    boolean existsByCpfAndCompetence(String cpf, String competence);

    /** Guarda de reentrada de REQ-010, espelhando BATCHPGT.NSP:L292-L298. */
    default boolean existsFor(Cpf cpf, Competence competence) {
        return existsByCpfAndCompetence(cpf.value(), competence.toString());
    }

    List<Payment> findByCompetenceOrderByPaymentNumberAsc(String competence);

    List<Payment> findAllByOrderByCompetenceDescPaymentNumberDesc(Pageable pageable);

    @Query("""
            select p.competence as competence,
                   count(p) as paymentCount,
                   sum(p.grossAmount) as grossTotal,
                   sum(p.netAmount) as netTotal
              from Payment p
             group by p.competence
             order by p.competence asc
            """)
    List<CompetenceTotalsView> aggregateByCompetence();

    @Query("""
            select p.competence as competence,
                   count(p) as paymentCount,
                   sum(p.grossAmount) as grossTotal,
                   sum(p.netAmount) as netTotal
              from Payment p
             where p.competence = :competence
             group by p.competence
            """)
    Optional<CompetenceTotalsView> aggregateByCompetence(@Param("competence") String competence);

    interface CompetenceTotalsView {
        String getCompetence();

        long getPaymentCount();

        BigDecimal getGrossTotal();

        BigDecimal getNetTotal();
    }
}
