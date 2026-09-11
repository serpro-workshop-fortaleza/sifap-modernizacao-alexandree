package br.gov.sifap.payment.domain;

import br.gov.sifap.beneficiary.api.BeneficiarySnapshot;
import br.gov.sifap.payment.api.CalculationResult;
import br.gov.sifap.sharedkernel.domain.Money;
import br.gov.sifap.socialprogram.api.ProgramParameters;
import java.util.List;

/**
 * Calculo do valor do beneficio.
 *
 * <p>REQ-004 recusa o beneficiario sem situacao ativa. REQ-005 determina o fator de renda.
 * REQ-006 trunca o resultado em duas casas, o que {@link Money} garante em toda operacao.
 *
 * <p>O desconto nao e calculado aqui: a questao de projeto P2 (se REQ-007 e REQ-008 valem
 * durante a folha ou apenas em execucao avulsa) permanece aberta.
 */
public class BenefitCalculator {

    private final List<BenefitFactor> factors;

    public BenefitCalculator(List<BenefitFactor> factors) {
        this.factors = List.copyOf(factors);
    }

    public CalculationResult calculate(BeneficiarySnapshot beneficiary, ProgramParameters program) {
        // REQ-004
        if (!beneficiary.status().isActive()) {
            return CalculationResult.rejected(
                    CalculationResult.RETURN_BENEFICIARY_NOT_ACTIVE, "Beneficiario sem situacao ativa");
        }

        var gross = program.baseAmount();
        for (var factor : factors) {
            var resolved = factor.resolve(beneficiary, program);
            if (resolved.isEmpty()) {
                return CalculationResult.rejected(
                        CalculationResult.RETURN_NO_APPLICABLE_FACTOR,
                        "Nao foi possivel determinar o " + factor.name() + " para o programa " + program.programCode());
            }
            gross = gross.multiply(resolved.orElseThrow());
        }

        // Sem desconto e sem bonus nesta etapa; o liquido acompanha o bruto ate P2 ser respondida.
        return CalculationResult.calculated(gross, Money.ZERO, gross, Money.ZERO);
    }

    public static BenefitCalculator withConfirmedFactorsOnly() {
        return new BenefitCalculator(List.of(new IncomeBandFactor()));
    }
}
