package br.gov.sifap.payment.api;

import static org.assertj.core.api.Assertions.assertThat;

import br.gov.sifap.sharedkernel.domain.Competence;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class PayrollResultTest {

    @Test
    void should_return_8_when_no_payment_was_generated() { // REQ-012
        assertThat(PayrollResult.resolveReturnCode(0, 0)).isEqualTo(PayrollResult.RETURN_NOTHING_GENERATED);
        assertThat(PayrollResult.resolveReturnCode(0, 3)).isEqualTo(PayrollResult.RETURN_NOTHING_GENERATED);
    }

    @Test
    void should_return_4_when_there_are_payments_and_at_least_one_rejection() { // REQ-012
        assertThat(PayrollResult.resolveReturnCode(10, 1)).isEqualTo(PayrollResult.RETURN_WITH_REJECTIONS);
    }

    @Test
    void should_return_0_when_there_are_payments_and_no_rejection() { // REQ-012
        assertThat(PayrollResult.resolveReturnCode(10, 0)).isEqualTo(PayrollResult.RETURN_SUCCESS);
    }

    @Test
    void should_return_12_with_zeroed_counters_on_execution_error() { // REQ-013
        var finishedAt = LocalDateTime.of(2026, 9, 10, 3, 0);

        var result = PayrollResult.executionError(Competence.of("202609"), finishedAt);

        assertThat(result.returnCode()).isEqualTo(PayrollResult.RETURN_EXECUTION_ERROR);
        assertThat(result.paymentsIssued()).isZero();
        assertThat(result.totalNetAmount().amount()).isEqualByComparingTo("0.00");
        assertThat(result.finishedAt()).isEqualTo(finishedAt);
    }
}
