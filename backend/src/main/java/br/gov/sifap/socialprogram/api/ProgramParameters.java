package br.gov.sifap.socialprogram.api;

import br.gov.sifap.sharedkernel.domain.Money;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Parametros vigentes de um programa social. Fonte unica: substitui os valores
 * duplicados hoje cravados em LDASIFAP.NSL, CALCBENF.NSN e BATCHPGT.NSP.
 *
 * <p>{@code adjustmentFactor} e transportado porque existe em SOCPROG, mas nenhum
 * requisito desta feature o aplica; ver questao de projeto P5.
 */
public record ProgramParameters(
        String programCode,
        String name,
        boolean active,
        Money baseAmount,
        Money maxIncome,
        List<IncomeBand> incomeBands,
        BigDecimal adjustmentFactor) {

    public ProgramParameters {
        Objects.requireNonNull(programCode, "Codigo do programa nao pode ser nulo");
        Objects.requireNonNull(name, "Nome do programa nao pode ser nulo");
        Objects.requireNonNull(baseAmount, "Valor base nao pode ser nulo");
        Objects.requireNonNull(maxIncome, "Teto de renda nao pode ser nulo");
        Objects.requireNonNull(adjustmentFactor, "Fator de ajuste nao pode ser nulo");
        incomeBands = List.copyOf(Objects.requireNonNull(incomeBands, "Faixas nao podem ser nulas"));
    }

    /** Teto zero desliga a verificacao de renda: VALELEG.NSN:L174 exige MAX-PERCAP-INCOME > 0. */
    public boolean hasIncomeCeiling() {
        return maxIncome.isPositive();
    }
}
