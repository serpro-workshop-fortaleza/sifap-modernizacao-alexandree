package br.gov.sifap.sharedkernel.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.math.BigDecimal;
import java.time.YearMonth;

import org.junit.jupiter.api.Test;

class ValueObjectsTest {

    @Test
    void should_truncate_money_to_two_decimal_places_when_created() { // REQ-006
        var money = Money.of("123.459");

        assertThat(money.amount()).isEqualByComparingTo(new BigDecimal("123.45"));
        assertThat(money.amount().scale()).isEqualTo(2);
    }

    @Test
    void should_normalize_formatted_cpf_when_created() {
        var cpf = Cpf.of("123.456.789-09");

        assertThat(cpf.value()).isEqualTo("12345678909");
    }

    @Test
    void should_reject_cpf_with_invalid_length() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> Cpf.of("123"));
    }

    @Test
    void should_parse_competence_in_year_month_format() {
        var competence = Competence.of("202609");

        assertThat(competence.value()).isEqualTo(YearMonth.of(2026, 9));
        assertThat(competence).hasToString("202609");
    }

    @Test
    void should_reject_competence_with_invalid_format() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> Competence.of("2026-09"));
    }
}
