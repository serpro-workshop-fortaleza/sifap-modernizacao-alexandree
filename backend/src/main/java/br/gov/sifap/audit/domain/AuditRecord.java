package br.gov.sifap.audit.domain;

import br.gov.sifap.audit.api.AuditEntry;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/** Origem: AUDIT.ddm (FNR 153). Retencao imutavel exigida por IN-TCU 63/2010. */
@Entity
@Table(name = "audit_entry")
public class AuditRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "action", nullable = false, length = 2)
    private String action;

    @Column(name = "module", nullable = false, length = 20)
    private String module;

    @Column(name = "entity_type", nullable = false, length = 30)
    private String entityType;

    @Column(name = "entity_id", nullable = false, length = 40)
    private String entityId;

    @Column(name = "performed_by", nullable = false, length = 40)
    private String performedBy;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    protected AuditRecord() {
    }

    public static AuditRecord from(AuditEntry entry) {
        var record = new AuditRecord();
        record.action = entry.action();
        record.module = entry.module();
        record.entityType = entry.entityType();
        record.entityId = entry.entityId();
        record.performedBy = entry.performedBy();
        record.occurredAt = entry.occurredAt();
        return record;
    }
}
