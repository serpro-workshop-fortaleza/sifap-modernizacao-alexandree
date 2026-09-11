package br.gov.sifap.payment.api;

import br.gov.sifap.sharedkernel.domain.Competence;

/** Geracao da folha da competencia. */
public interface PayrollGeneration {

    /** Competencia nula e derivada do ano e mes correntes, conforme REQ-011. */
    PayrollResult run(Competence competence);
}
