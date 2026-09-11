package br.gov.sifap.sharedkernel.domain;

import java.time.Clock;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

public record Competence(YearMonth value) {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("uuuuMM");
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM");

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

    // Representacao do contrato REST; a canonica do dominio continua YYYYMM, como YEAR-MONTH-REF (N6).
    public static Competence ofIso(String value) {
        Objects.requireNonNull(value, "Competencia nao pode ser nula");
        try {
            return new Competence(YearMonth.parse(value, ISO_FORMATTER));
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Competencia deve estar no formato YYYY-MM", exception);
        }
    }

    public static Competence current(Clock clock) {
        return new Competence(YearMonth.now(Objects.requireNonNull(clock, "Clock nao pode ser nulo")));
    }

    public String toIsoString() {
        return value.format(ISO_FORMATTER);
    }

    @Override
    public String toString() {
        return value.format(FORMATTER);
    }
}
