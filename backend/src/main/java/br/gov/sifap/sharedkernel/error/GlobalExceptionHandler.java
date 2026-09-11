package br.gov.sifap.sharedkernel.error;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Formato unico de erro (RFC 7807) para toda a API. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    ProblemDetail handleNotFound(ResourceNotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, exception.getMessage(), exception);
    }

    @ExceptionHandler(BusinessRuleViolationException.class)
    ProblemDetail handleConflict(BusinessRuleViolationException exception) {
        return problem(HttpStatus.CONFLICT, exception.getMessage(), exception);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException exception) {
        var detail = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((first, second) -> first + "; " + second)
                .orElse("Requisicao invalida");
        return problem(HttpStatus.BAD_REQUEST, detail, exception);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail handleIllegalArgument(IllegalArgumentException exception) {
        return problem(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception exception) {
        // A mensagem original nao vai para a resposta: ela pode carregar dado sensivel.
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno ao processar a requisicao", exception);
    }

    private static ProblemDetail problem(HttpStatus status, String detail, Exception exception) {
        var correlationId = UUID.randomUUID().toString();
        log.error("falha na requisicao correlationId={} status={}", correlationId, status.value(), exception);
        var body = ProblemDetail.forStatusAndDetail(status, detail);
        body.setProperty("correlationId", correlationId);
        return body;
    }
}
