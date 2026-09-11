package br.gov.sifap.payment.domain;

import br.gov.sifap.beneficiary.api.BeneficiarySnapshot;
import br.gov.sifap.socialprogram.api.ProgramParameters;
import java.math.BigDecimal;
import java.util.Optional;

/**
 * Ponto de extensao explicito do calculo do beneficio.
 *
 * <p>Hoje existe uma unica implementacao registrada, {@link IncomeBandFactor}, que atende
 * REQ-005. Os fatores regional, familiar e etario de CALCBENF.NSN estao classificados como
 * Misterio e nao possuem requisito (questao de projeto P5); por isso estao ausentes em vez
 * de presentes com valor 1,0 implicito.
 *
 * <p>{@code Optional.empty()} significa que o fator nao pode ser determinado com os
 * parametros disponiveis, e nao que ele seja neutro.
 */
public interface BenefitFactor {

    String name();

    Optional<BigDecimal> resolve(BeneficiarySnapshot beneficiary, ProgramParameters program);
}
