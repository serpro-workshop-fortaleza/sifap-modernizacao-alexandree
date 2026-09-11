package br.gov.sifap.audit.api;

/**
 * Escrita unidirecional na trilha, sem retorno de dominio: nenhum contexto le o modelo
 * de auditoria para decidir regra de negocio (REQ-014).
 */
public interface AuditTrail {

    void record(AuditEntry entry);
}
