package br.gov.sifap.payment.domain;

import static org.assertj.core.api.Assertions.assertThat;

import br.gov.sifap.beneficiary.api.BeneficiarySnapshot;
import br.gov.sifap.beneficiary.api.BeneficiaryStatus;
import br.gov.sifap.payment.api.EligibilityDecision;
import br.gov.sifap.sharedkernel.domain.Cpf;
import br.gov.sifap.sharedkernel.domain.Money;
import br.gov.sifap.socialprogram.api.IncomeBand;
import br.gov.sifap.socialprogram.api.ProgramParameters;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class EligibilityPolicyTest {

    private final EligibilityPolicy policy = new EligibilityPolicy();

    @Test
    void should_reject_with_2004_when_program_is_not_active() { // REQ-001
        var decision = policy.evaluate(beneficiary(BeneficiaryStatus.ACTIVE, "100.00"), program(false, "1000.00"));

        assertThat(decision.eligible()).isFalse();
        assertThat(decision.returnCode()).isEqualTo(EligibilityDecision.RETURN_PROGRAM_INACTIVE);
        assertThat(decision.reasons()).containsExactly("Programa social inativo");
    }

    @Test
    void should_not_evaluate_other_conditions_when_program_is_not_active() { // REQ-001
        var decision = policy.evaluate(beneficiary(BeneficiaryStatus.SUSPENDED, "9999.00"), program(false, "10.00"));

        assertThat(decision.reasons()).hasSize(1);
    }

    @Test
    void should_be_ineligible_when_beneficiary_is_suspended() { // REQ-002
        var decision = policy.evaluate(beneficiary(BeneficiaryStatus.SUSPENDED, "100.00"), program(true, "1000.00"));

        assertThat(decision.eligible()).isFalse();
        assertThat(decision.reasons()).containsExactly("Beneficiario suspenso");
    }

    @Test
    void should_be_ineligible_when_beneficiary_is_cancelled_or_terminated() { // REQ-002
        var cancelled = policy.evaluate(beneficiary(BeneficiaryStatus.CANCELLED, "100.00"), program(true, "1000.00"));
        var terminated = policy.evaluate(beneficiary(BeneficiaryStatus.TERMINATED, "100.00"), program(true, "1000.00"));

        assertThat(cancelled.reasons()).containsExactly("Beneficiario cancelado ou desligado");
        assertThat(terminated.reasons()).containsExactly("Beneficiario cancelado ou desligado");
    }

    @Test
    void should_be_ineligible_when_beneficiary_is_inactive() { // REQ-002
        var decision = policy.evaluate(beneficiary(BeneficiaryStatus.INACTIVE, "100.00"), program(true, "1000.00"));

        assertThat(decision.reasons()).containsExactly("Beneficiario inativo");
    }

    @Test
    void should_be_ineligible_when_income_is_above_program_ceiling() { // REQ-003
        var decision = policy.evaluate(beneficiary(BeneficiaryStatus.ACTIVE, "1000.01"), program(true, "1000.00"));

        assertThat(decision.eligible()).isFalse();
        assertThat(decision.reasons()).containsExactly("Renda considerada acima do teto do programa");
    }

    @Test
    void should_be_eligible_when_income_equals_program_ceiling() { // REQ-003
        var decision = policy.evaluate(beneficiary(BeneficiaryStatus.ACTIVE, "1000.00"), program(true, "1000.00"));

        assertThat(decision.eligible()).isTrue();
        assertThat(decision.returnCode()).isEqualTo(EligibilityDecision.RETURN_ELIGIBLE);
    }

    @Test
    void should_skip_income_check_when_ceiling_is_zero() { // REQ-003
        var decision = policy.evaluate(beneficiary(BeneficiaryStatus.ACTIVE, "99999.00"), program(true, "0.00"));

        assertThat(decision.eligible()).isTrue();
    }

    @Test
    void should_accumulate_every_reason_when_more_than_one_condition_fails() { // REQ-002, REQ-003
        var decision = policy.evaluate(beneficiary(BeneficiaryStatus.SUSPENDED, "5000.00"), program(true, "1000.00"));

        assertThat(decision.reasons())
                .containsExactly("Beneficiario suspenso", "Renda considerada acima do teto do programa");
    }

    private static BeneficiarySnapshot beneficiary(BeneficiaryStatus status, String income) {
        return new BeneficiarySnapshot(
                Cpf.of("12345678909"),
                1L,
                status,
                Money.of(income),
                LocalDate.of(1980, 1, 1),
                0,
                "P001",
                "01");
    }

    private static ProgramParameters program(boolean active, String maxIncome) {
        return new ProgramParameters(
                "P001",
                "Programa de teste",
                active,
                Money.of("600.00"),
                Money.of(maxIncome),
                List.of(new IncomeBand(Money.of("999999.99"), BigDecimal.ONE)),
                BigDecimal.ZERO);
    }
}
