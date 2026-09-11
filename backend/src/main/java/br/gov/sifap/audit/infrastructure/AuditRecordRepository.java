package br.gov.sifap.audit.infrastructure;

import br.gov.sifap.audit.domain.AuditRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditRecordRepository extends JpaRepository<AuditRecord, Long> {
}
