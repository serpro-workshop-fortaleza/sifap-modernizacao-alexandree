package br.gov.sifap.audit.api;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Registro imutavel da trilha. Campos derivados de CCAUDIT.NSC:L7-L10.
 * O dominio de {@code action} inclui o codigo {@code DV} usado por BATCHCON.NSP:L318,
 * cuja origem permanece questao em aberto; por isso nao ha enum fechado aqui.
 */
public record AuditEntry(
        String action,
        String module,
        String entityType,
        String entityId,
        String performedBy,
        LocalDateTime occurredAt) {

    public AuditEntry {
        Objects.requireNonNull(action, "Acao nao pode ser nula");
        Objects.requireNonNull(module, "Modulo nao pode ser nulo");
        Objects.requireNonNull(entityType, "Tipo de entidade nao pode ser nulo");
        Objects.requireNonNull(entityId, "Identificador nao pode ser nulo");
        Objects.requireNonNull(performedBy, "Responsavel nao pode ser nulo");
        Objects.requireNonNull(occurredAt, "Data e hora nao podem ser nulas");
    }
}
