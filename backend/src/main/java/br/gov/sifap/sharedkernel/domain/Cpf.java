package br.gov.sifap.sharedkernel.domain;

import java.util.Objects;

public record Cpf(String value) {

    public Cpf {
        Objects.requireNonNull(value, "CPF nao pode ser nulo");
        value = value.replace(".", "").replace("-", "");
        if (!value.matches("\\d{11}")) {
            throw new IllegalArgumentException("CPF deve conter 11 digitos");
        }
    }

    public static Cpf of(String value) {
        return new Cpf(value);
    }
}
