package br.gov.sifap.payment.domain;

import br.gov.sifap.beneficiary.api.BeneficiarySnapshot;
import br.gov.sifap.beneficiary.api.BeneficiaryStatus;
import br.gov.sifap.payment.api.EligibilityDecision;
import br.gov.sifap.socialprogram.api.ProgramParameters;
import java.util.ArrayList;
import java.util.List;

/**
 * Elegibilidade do beneficiario na competencia.
 *
 * <p>Cobre apenas as regras confirmadas: REQ-001, REQ-002 e REQ-003. As faixas de idade
 * (VALELEG.NSN:L152-L167) e o desvio da regiao 99 (VALELEG.NSN:L123) nao viraram requisito
 * e por isso nao sao avaliados aqui.
 */
public class EligibilityPolicy {

    public EligibilityDecision evaluate(BeneficiarySnapshot beneficiary, ProgramParameters program) {
        // REQ-001: programa inativo encerra a avaliacao antes das demais condicoes.
        if (!program.active()) {
            return EligibilityDecision.programInactive();
        }

        var reasons = new ArrayList<String>();

        // REQ-002
        if (!beneficiary.status().isActive()) {
            reasons.add(reasonForStatus(beneficiary.status()));
        }

        // REQ-003: teto zero desliga a verificacao.
        if (program.hasIncomeCeiling() && beneficiary.consideredIncome().isGreaterThan(program.maxIncome())) {
            reasons.add("Renda considerada acima do teto do programa");
        }

        return reasons.isEmpty() ? EligibilityDecision.approved() : EligibilityDecision.ineligible(List.copyOf(reasons));
    }

    private static String reasonForStatus(BeneficiaryStatus status) {
        return switch (status) {
            case SUSPENDED -> "Beneficiario suspenso";
            case CANCELLED, TERMINATED -> "Beneficiario cancelado ou desligado";
            case INACTIVE -> "Beneficiario inativo";
            case ACTIVE -> throw new IllegalStateException("Situacao ativa nao produz motivo de inelegibilidade");
        };
    }
}
