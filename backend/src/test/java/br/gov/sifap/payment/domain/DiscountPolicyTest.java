package br.gov.sifap.payment.domain;

import static org.assertj.core.api.Assertions.assertThat;

import br.gov.sifap.sharedkernel.domain.Money;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class DiscountPolicyTest {

    private final DiscountPolicy policy = new DiscountPolicy();

    @Test
    void should_keep_total_when_discounts_are_below_the_thirty_percent_ceiling() { // REQ-007
        var assessment = policy.assess(Money.of("1000.00"), List.of(discount("IR", "100.00"), discount("CS", "50.00")));

        assertThat(assessment.totalApplied().amount()).isEqualByComparingTo("150.00");
        assertThat(assessment.ceilingApplied()).isFalse();
    }

    @Test
    void should_cap_total_at_thirty_percent_of_gross_amount() { // REQ-007
        var assessment = policy.assess(Money.of("1000.00"), List.of(discount("IR", "200.00"), discount("CS", "250.00")));

        assertThat(assessment.totalApplied().amount()).isEqualByComparingTo("300.00");
        assertThat(assessment.ceilingApplied()).isTrue();
    }

    @Test
    void should_truncate_the_ceiling_in_two_decimal_places() { // REQ-006, REQ-007
        // 333,33 x 0,30 = 99,999 -> truncado para 99,99, nunca 100,00
        var assessment = policy.assess(Money.of("333.33"), List.of(discount("IR", "500.00")));

        assertThat(assessment.ceiling().amount()).isEqualByComparingTo("99.99");
        assertThat(assessment.totalApplied().amount()).isEqualByComparingTo("99.99");
    }

    @Test
    void should_apply_judicial_discount_in_full_above_the_ceiling() { // REQ-008
        var assessment = policy.assess(Money.of("1000.00"), List.of(discount("JD", "400.00")));

        assertThat(assessment.totalApplied().amount()).isEqualByComparingTo("400.00");
        assertThat(assessment.ceilingApplied()).isFalse();
    }

    @Test
    void should_not_run_the_ceiling_check_on_the_judicial_iteration() { // REQ-008
        var assessment = policy.assess(
                Money.of("1000.00"), List.of(discount("IR", "100.00"), discount("JD", "500.00")));

        assertThat(assessment.totalApplied().amount()).isEqualByComparingTo("600.00");
        assertThat(assessment.ceilingApplied()).isFalse();
    }

    @Test
    void should_return_zero_when_there_is_no_discount() { // REQ-007
        var assessment = policy.assess(Money.of("1000.00"), List.of());

        assertThat(assessment.totalApplied().amount()).isEqualByComparingTo("0.00");
    }

    private static DiscountPolicy.Request discount(String type, String amount) {
        return new DiscountPolicy.Request(type, Money.of(amount), BigDecimal.ZERO, null);
    }
}
