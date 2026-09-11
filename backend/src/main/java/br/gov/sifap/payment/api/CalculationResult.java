package br.gov.sifap.payment.api;

import br.gov.sifap.sharedkernel.domain.Money;
import java.util.Objects;
import java.util.Optional;

/** Resultado do calculo do beneficio (REQ-004, REQ-005, REQ-006). */
public record CalculationResult(
        int returnCode,
        String message,
        Money grossAmount,
        Money discountTotal,
        Money netAmount,
        Money bonusAmount) {

    public static final int RETURN_OK = 0;
    public static final int RETURN_BENEFICIARY_NOT_ACTIVE = 2002;

    /**
     * Codigo local, sem equivalente no legado: nenhuma faixa do programa comporta a renda
     * do beneficiario. Sinaliza lacuna de parametrizacao do catalogo, nao regra de negocio.
     */
    public static final int RETURN_NO_APPLICABLE_FACTOR = 9001;

    public CalculationResult {
        Objects.requireNonNull(message, "Mensagem nao pode ser nula");
    }

    public boolean isSuccessful() {
        return returnCode == RETURN_OK;
    }

    public static CalculationResult rejected(int returnCode, String message) {
        return new CalculationResult(returnCode, message, null, null, null, null);
    }

    public static CalculationResult calculated(Money gross, Money discountTotal, Money net, Money bonus) {
        return new CalculationResult(RETURN_OK, "Calculo concluido", gross, discountTotal, net, bonus);
    }

    public Optional<Money> netAmountIfCalculated() {
        return Optional.ofNullable(netAmount);
    }
}
