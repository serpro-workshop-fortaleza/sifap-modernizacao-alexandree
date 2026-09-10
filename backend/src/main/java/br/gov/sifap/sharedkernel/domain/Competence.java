package br.gov.sifap.sharedkernel.domain;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

public record Competence(YearMonth value) {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("uuuuMM");

    public Competence {
        Objects.requireNonNull(value, "Competencia nao pode ser nula");
    }

    public static Competence of(String value) {
        Objects.requireNonNull(value, "Competencia nao pode ser nula");
        try {
            return new Competence(YearMonth.parse(value, FORMATTER));
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Competencia deve estar no formato YYYYMM", exception);
        }
    }

    @Override
    public String toString() {
        return value.format(FORMATTER);
    }
}
