package br.gov.sifap.payment.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.gov.sifap.payment.domain.Payment;
import br.gov.sifap.payment.domain.PaymentDiscount;
import br.gov.sifap.sharedkernel.domain.Competence;
import br.gov.sifap.sharedkernel.domain.Cpf;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PaymentRepositoryTest {

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:postgresql://localhost:5432/sifap");
        registry.add("spring.datasource.username", () -> "sifap");
        registry.add("spring.datasource.password", () -> "sifap_local");
    }

    @Autowired
    private PaymentRepository paymentRepository;

    @BeforeEach
    void cleanDatabase() {
        paymentRepository.deleteAll();
    }

    @Test
    void should_persist_payment_with_periodic_discounts_when_aggregate_is_valid() { // REQ-010
        var payment = payment(1001L);
        payment.addDiscount(new PaymentDiscount("JD", new BigDecimal("10.129"), new BigDecimal("5.009"), "CASE-1"));

        paymentRepository.saveAndFlush(payment);
        var loaded = paymentRepository.findById(1001L);

        assertThat(loaded).isPresent();
        assertThat(loaded.orElseThrow().discounts())
                .singleElement()
                .satisfies(discount -> assertThat(discount.amount()).isEqualByComparingTo("10.12"));
    }

    @Test
    void should_find_payment_by_cpf_and_competence_when_record_exists() { // REQ-010
        paymentRepository.saveAndFlush(payment(1002L));

        var result = paymentRepository.findByCpfAndCompetence(
                Cpf.of("12345678909"), Competence.of("202609"));

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().paymentNumber()).isEqualTo(1002L);
    }

    @Test
    void should_reject_ninth_discount_when_payment_already_has_eight() {
        var payment = payment(1003L);
        for (var index = 0; index < 8; index++) {
            payment.addDiscount(new PaymentDiscount("TX", BigDecimal.ONE, null, null));
        }

        assertThatThrownBy(() -> payment.addDiscount(new PaymentDiscount("TX", BigDecimal.ONE, null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Pagamento nao pode possuir mais de 8 descontos");
    }

    private static Payment payment(long paymentNumber) {
        return new Payment(
                paymentNumber,
                Cpf.of("12345678909"),
                456L,
                "P001",
                Competence.of("202609"),
                1,
                new BigDecimal("100.00"),
                new BigDecimal("90.00"),
                new BigDecimal("10.00"),
                BigDecimal.ZERO,
                "P");
    }
}
