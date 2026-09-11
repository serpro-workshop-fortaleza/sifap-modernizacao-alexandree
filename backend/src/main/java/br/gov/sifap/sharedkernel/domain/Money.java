package br.gov.sifap.sharedkernel.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Money(BigDecimal amount) {

    public static final Money ZERO = new Money(BigDecimal.ZERO);

    public Money {
        Objects.requireNonNull(amount, "Valor monetario nao pode ser nulo");
        amount = amount.setScale(2, RoundingMode.DOWN);
    }

    public static Money of(String amount) {
        return new Money(new BigDecimal(amount));
    }

    public static Money of(BigDecimal amount) {
        return new Money(amount);
    }

    public Money add(Money other) {
        return new Money(amount.add(other.amount()));
    }

    public Money subtract(Money other) {
        return new Money(amount.subtract(other.amount()));
    }

    // Multiplica na escala cheia e trunca so no resultado, como o COMPUTE seguido de truncamento do Natural.
    public Money multiply(BigDecimal factor) {
        return new Money(amount.multiply(Objects.requireNonNull(factor, "Fator nao pode ser nulo")));
    }

    public boolean isGreaterThan(Money other) {
        return amount.compareTo(other.amount()) > 0;
    }

    public boolean isZero() {
        return amount.signum() == 0;
    }

    public boolean isPositive() {
        return amount.signum() > 0;
    }
}
