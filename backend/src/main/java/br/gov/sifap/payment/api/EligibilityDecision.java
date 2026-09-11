package br.gov.sifap.payment.api;

import java.util.List;
import java.util.Objects;

/**
 * Resultado da avaliacao de elegibilidade (REQ-001, REQ-002, REQ-003).
 *
 * <p>O legado acumula ate dez motivos e devolve apenas o primeiro
 * (VALELEG.NSN:L231), comportamento classificado como Misterio. Aqui todos os
 * motivos acumulados sao expostos; isso amplia a informacao sem alterar a decisao.
 */
public record EligibilityDecision(boolean eligible, int returnCode, List<String> reasons) {

    public static final int RETURN_ELIGIBLE = 0;
    public static final int RETURN_PROGRAM_INACTIVE = 2004;
    public static final int RETURN_INELIGIBLE = 2003;

    public EligibilityDecision {
        reasons = List.copyOf(Objects.requireNonNull(reasons, "Motivos nao podem ser nulos"));
    }

    public static EligibilityDecision approved() {
        return new EligibilityDecision(true, RETURN_ELIGIBLE, List.of());
    }

    public static EligibilityDecision programInactive() {
        return new EligibilityDecision(false, RETURN_PROGRAM_INACTIVE, List.of("Programa social inativo"));
    }

    public static EligibilityDecision ineligible(List<String> reasons) {
        return new EligibilityDecision(false, RETURN_INELIGIBLE, reasons);
    }
}
