package br.gov.sifap.sharedkernel.error;

/** Recurso inexistente no limite da API; mapeada para 404 pelo tratador global. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
