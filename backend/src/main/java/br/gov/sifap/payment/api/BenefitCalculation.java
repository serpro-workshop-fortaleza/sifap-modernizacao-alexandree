package br.gov.sifap.payment.api;

import br.gov.sifap.sharedkernel.domain.Competence;
import br.gov.sifap.sharedkernel.domain.Cpf;

/** Calculo do beneficio de um beneficiario em uma competencia. */
public interface BenefitCalculation {

    CalculationResult calculate(Cpf cpf, Competence competence);
}
