package br.gov.sifap.sharedkernel.error;

/** Invariante de negocio violada; mapeada para 409 pelo tratador global. */
public class BusinessRuleViolationException extends RuntimeException {

    public BusinessRuleViolationException(String message) {
        super(message);
    }
}
