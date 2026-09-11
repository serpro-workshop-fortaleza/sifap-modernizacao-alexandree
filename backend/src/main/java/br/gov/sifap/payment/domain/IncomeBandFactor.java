package br.gov.sifap.payment.domain;

import br.gov.sifap.beneficiary.api.BeneficiarySnapshot;
import br.gov.sifap.socialprogram.api.ProgramParameters;
import java.math.BigDecimal;
import java.util.Optional;

/**
 * REQ-005: aplica o fator da primeira faixa, em ordem crescente de teto, cujo limite
 * superior seja maior ou igual a renda considerada. Faixas posteriores que tambem
 * comportem a renda sao ignoradas.
 */
public class IncomeBandFactor implements BenefitFactor {

    @Override
    public String name() {
        return "fator de renda";
    }

    @Override
    public Optional<BigDecimal> resolve(BeneficiarySnapshot beneficiary, ProgramParameters program) {
        return program.incomeBands().stream()
                .filter(band -> band.covers(beneficiary.consideredIncome()))
                .findFirst()
                .map(band -> band.factor());
    }
}
