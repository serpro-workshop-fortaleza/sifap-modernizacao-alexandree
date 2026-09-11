package br.gov.sifap.payment.domain;

import static org.assertj.core.api.Assertions.assertThat;

import br.gov.sifap.beneficiary.api.BeneficiarySnapshot;
import br.gov.sifap.beneficiary.api.BeneficiaryStatus;
import br.gov.sifap.payment.api.CalculationResult;
import br.gov.sifap.sharedkernel.domain.Cpf;
import br.gov.sifap.sharedkernel.domain.Money;
import br.gov.sifap.socialprogram.api.IncomeBand;
import br.gov.sifap.socialprogram.api.ProgramParameters;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class BenefitCalculatorTest {

    private final BenefitCalculator calculator = BenefitCalculator.withConfirmedFactorsOnly();

    @Test
    void should_return_2002_and_no_amount_when_beneficiary_is_not_active() { // REQ-004
        var result = calculator.calculate(beneficiary(BeneficiaryStatus.SUSPENDED, "100.00"), program());

        assertThat(result.returnCode()).isEqualTo(CalculationResult.RETURN_BENEFICIARY_NOT_ACTIVE);
        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.grossAmount()).isNull();
        assertThat(result.netAmount()).isNull();
    }

    @Test
    void should_apply_first_band_that_covers_the_income() { // REQ-005
        var result = calculator.calculate(beneficiary(BeneficiaryStatus.ACTIVE, "250.00"), program());

        // 600,00 x 1,20 da primeira faixa (teto 300,00)
        assertThat(result.grossAmount().amount()).isEqualByComparingTo("720.00");
    }

    @Test
    void should_ignore_later_bands_that_also_cover_the_income() { // REQ-005
        var result = calculator.calculate(beneficiary(BeneficiaryStatus.ACTIVE, "850.00"), program());

        // Cabe na faixa de teto 900,00 (fator 1,05) e tambem na de 999999,99; vale a primeira.
        assertThat(result.grossAmount().amount()).isEqualByComparingTo("630.00");
    }

    @Test
    void should_apply_band_when_income_equals_its_ceiling() { // REQ-005
        var result = calculator.calculate(beneficiary(BeneficiaryStatus.ACTIVE, "300.00"), program());

        assertThat(result.grossAmount().amount()).isEqualByComparingTo("720.00");
    }

    @Test
    void should_truncate_and_not_round_the_calculated_amount() { // REQ-006
        var program = new ProgramParameters(
                "P001",
                "Programa de teste",
                true,
                Money.of("100.00"),
                Money.ZERO,
                List.of(new IncomeBand(Money.of("999999.99"), new BigDecimal("1.005"))),
                BigDecimal.ZERO);

        var result = calculator.calculate(beneficiary(BeneficiaryStatus.ACTIVE, "100.00"), program);

        // 100,00 x 1,005 = 100,50 exatos; o truncamento aparece no caso abaixo.
        assertThat(result.grossAmount().amount()).isEqualByComparingTo("100.50");
        assertThat(Money.of("123.459").amount()).isEqualByComparingTo("123.45");
    }

    @Test
    void should_reject_when_no_band_covers_the_income() {
        var program = new ProgramParameters(
                "P001",
                "Programa sem faixa abrangente",
                true,
                Money.of("600.00"),
                Money.ZERO,
                List.of(new IncomeBand(Money.of("100.00"), BigDecimal.ONE)),
                BigDecimal.ZERO);

        var result = calculator.calculate(beneficiary(BeneficiaryStatus.ACTIVE, "900.00"), program);

        assertThat(result.returnCode()).isEqualTo(CalculationResult.RETURN_NO_APPLICABLE_FACTOR);
        assertThat(result.message()).contains("fator de renda");
    }

    @Test
    void should_not_apply_unconfirmed_factors() { // questao de projeto P5
        var result = calculator.calculate(beneficiary(BeneficiaryStatus.ACTIVE, "250.00"), program());

        // Somente o fator de renda entra no calculo; regional, familiar e etario nao tem requisito.
        assertThat(result.grossAmount().amount()).isEqualByComparingTo("720.00");
        assertThat(result.discountTotal().amount()).isEqualByComparingTo("0.00");
        assertThat(result.netAmount()).isEqualTo(result.grossAmount());
    }

    private static BeneficiarySnapshot beneficiary(BeneficiaryStatus status, String income) {
        return new BeneficiarySnapshot(
                Cpf.of("12345678909"),
                1L,
                status,
                Money.of(income),
                LocalDate.of(1980, 1, 1),
                2,
                "P001",
                "01");
    }

    private static ProgramParameters program() {
        return new ProgramParameters(
                "P001",
                "Programa de teste",
                true,
                Money.of("600.00"),
                Money.ZERO,
                List.of(
                        new IncomeBand(Money.of("300.00"), new BigDecimal("1.20")),
                        new IncomeBand(Money.of("900.00"), new BigDecimal("1.05")),
                        new IncomeBand(Money.of("999999.99"), new BigDecimal("0.90"))),
                BigDecimal.ZERO);
    }
}
