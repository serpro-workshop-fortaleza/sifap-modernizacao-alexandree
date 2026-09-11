package br.gov.sifap.socialprogram.api;

import br.gov.sifap.sharedkernel.domain.Money;
import java.util.Objects;

/** Ocorrencia do grupo periodico de faixas de calculo de SOCPROG.ddm. */
public record IncomeBand(Money ceiling, java.math.BigDecimal factor) {

    public IncomeBand {
        Objects.requireNonNull(ceiling, "Teto da faixa nao pode ser nulo");
        Objects.requireNonNull(factor, "Fator da faixa nao pode ser nulo");
    }

    public boolean covers(Money income) {
        return !income.isGreaterThan(ceiling);
    }
}
