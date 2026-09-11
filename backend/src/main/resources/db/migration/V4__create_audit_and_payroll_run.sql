-- Contexto 5 (Trilha de Auditoria), origem CCAUDIT.NSC e AUDIT.ddm (FNR 153).
-- Escrita unidirecional: nenhum contexto le este modelo para decidir regra de negocio.
CREATE TABLE audit_entry (
    id           BIGSERIAL   PRIMARY KEY,
    action       VARCHAR(2)  NOT NULL,
    module       VARCHAR(20) NOT NULL,
    entity_type  VARCHAR(30) NOT NULL,
    entity_id    VARCHAR(40) NOT NULL,
    performed_by VARCHAR(40) NOT NULL,
    occurred_at  TIMESTAMP   NOT NULL
);

CREATE INDEX ix_audit_entry_occurred_at ON audit_entry (occurred_at);

-- Resultado de cada execucao da folha, incluindo o codigo de retorno de REQ-012 e REQ-013.
CREATE TABLE payroll_run (
    id                    BIGSERIAL      PRIMARY KEY,
    competence            VARCHAR(6)     NOT NULL,
    payments_issued       INTEGER        NOT NULL,
    payments_rejected     INTEGER        NOT NULL,
    ignored_beneficiaries INTEGER        NOT NULL,
    total_gross_amount    NUMERIC(18, 2) NOT NULL,
    total_net_amount      NUMERIC(18, 2) NOT NULL,
    return_code           INTEGER        NOT NULL,
    finished_at           TIMESTAMP      NOT NULL,
    CONSTRAINT ck_payroll_run_return_code CHECK (return_code IN (0, 4, 8, 12))
);

CREATE INDEX ix_payroll_run_competence ON payroll_run (competence);
