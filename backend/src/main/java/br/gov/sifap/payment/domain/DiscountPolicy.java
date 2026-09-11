package br.gov.sifap.payment.domain;

import br.gov.sifap.sharedkernel.domain.Money;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Aplicacao dos descontos de um pagamento.
 *
 * <p>REQ-007 limita o total a 30% do valor bruto. REQ-008 isenta o desconto judicial
 * desse limite: a verificacao do teto nao roda na iteracao do desconto judicial, como
 * em CALCDSCT.NSP:L128-L137 e L170-L174.
 */
public class DiscountPolicy {

    public static final String JUDICIAL_TYPE = "JD";
    private static final BigDecimal CEILING_RATE = new BigDecimal("0.30");

    public Assessment assess(Money grossAmount, List<Request> requested) {
        Objects.requireNonNull(grossAmount, "Valor bruto nao pode ser nulo");
        var ceiling = grossAmount.multiply(CEILING_RATE);
        var total = Money.ZERO;
        var ceilingApplied = false;

        for (var request : requested) {
            total = total.add(request.amount());
            if (!request.isJudicial() && total.isGreaterThan(ceiling)) {
                total = ceiling;
                ceilingApplied = true;
            }
        }

        return new Assessment(total, ceiling, ceilingApplied);
    }

    /** Um desconto solicitado; o tipo segue o dominio de PAYMENT.ddm:L47. */
    public record Request(String type, Money amount, BigDecimal percentage, String caseNumber) {

        public Request {
            Objects.requireNonNull(type, "Tipo do desconto nao pode ser nulo");
            Objects.requireNonNull(amount, "Valor do desconto nao pode ser nulo");
        }

        public boolean isJudicial() {
            return JUDICIAL_TYPE.equals(type);
        }
    }

    public record Assessment(Money totalApplied, Money ceiling, boolean ceilingApplied) {
    }
}
