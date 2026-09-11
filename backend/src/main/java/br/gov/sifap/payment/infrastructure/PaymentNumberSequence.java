package br.gov.sifap.payment.infrastructure;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

/** Equivalente ao contador #SEQ-PAYMENT de BATCHPGT.NSP:L465, delegado ao PostgreSQL. */
@Component
public class PaymentNumberSequence {

    private final EntityManager entityManager;

    PaymentNumberSequence(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public long next() {
        var value = entityManager.createNativeQuery("SELECT nextval('payment_number_seq')").getSingleResult();
        return ((Number) value).longValue();
    }
}
