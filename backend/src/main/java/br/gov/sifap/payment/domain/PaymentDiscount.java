package br.gov.sifap.payment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "payment_discount")
public class PaymentDiscount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_number", nullable = false)
    private Payment payment;

    @Column(name = "discount_type", nullable = false, length = 3)
    private String discountType;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "percentage", precision = 5, scale = 2)
    private BigDecimal percentage;

    @Column(name = "case_number", length = 20)
    private String caseNumber;

    protected PaymentDiscount() {
    }

    public PaymentDiscount(String discountType, BigDecimal amount, BigDecimal percentage, String caseNumber) {
        this.discountType = Objects.requireNonNull(discountType, "Tipo do desconto nao pode ser nulo");
        this.amount = Objects.requireNonNull(amount, "Valor do desconto nao pode ser nulo")
                .setScale(2, java.math.RoundingMode.DOWN);
        this.percentage = percentage == null ? null : percentage.setScale(2, java.math.RoundingMode.DOWN);
        this.caseNumber = caseNumber;
    }

    PaymentDiscount attachTo(Payment payment) {
        if (this.payment != null && this.payment != payment) {
            throw new IllegalStateException("Desconto ja associado a outro pagamento");
        }
        this.payment = Objects.requireNonNull(payment, "Pagamento nao pode ser nulo");
        return this;
    }

    public String discountType() {
        return discountType;
    }

    public BigDecimal amount() {
        return amount;
    }

    public boolean isJudicial() {
        return "JD".equals(discountType);
    }
}
