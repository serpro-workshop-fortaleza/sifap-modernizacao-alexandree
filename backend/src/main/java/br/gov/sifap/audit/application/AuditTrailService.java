package br.gov.sifap.audit.application;

import br.gov.sifap.audit.api.AuditEntry;
import br.gov.sifap.audit.api.AuditTrail;
import br.gov.sifap.audit.domain.AuditRecord;
import br.gov.sifap.audit.infrastructure.AuditRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
class AuditTrailService implements AuditTrail {

    private final AuditRecordRepository auditRecordRepository;

    AuditTrailService(AuditRecordRepository auditRecordRepository) {
        this.auditRecordRepository = auditRecordRepository;
    }

    // MANDATORY: a ausencia do registro de auditoria invalida a operacao (REQ-014),
    // portanto a escrita compartilha a transacao de quem chama e cai junto no rollback.
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void record(AuditEntry entry) {
        auditRecordRepository.save(AuditRecord.from(entry));
    }
}
