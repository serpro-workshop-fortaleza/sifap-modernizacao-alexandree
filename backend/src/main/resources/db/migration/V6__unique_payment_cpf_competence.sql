-- ADR-002 / questao de projeto P4a.
--
-- Esta migracao existe isolada de proposito. Ela NAO implementa REQ-010: o requisito
-- e atendido pela consulta previa de PaymentRepository.findByCpfAndCompetence, que
-- espelha a guarda de reentrada de BATCHPGT.NSP:L292-L298.
--
-- O indice unico e uma decisao de projeto adicional, estritamente mais forte que
-- REQ-010: ele tambem impede a dupla gravacao na mesma iteracao que o legado produz
-- em CALCBENF.NSN:L319 e BATCHPGT.NSP:L488 (TICKET 6622/2011, em aberto).
--
-- Enquanto P4b nao for respondida pela Coordenacao de Beneficios, a reversao desta
-- decisao deve custar apenas o DROP abaixo, sem alterar entidade nem mapeamento JPA.
DROP INDEX IF EXISTS ix_payment_cpf_competence;

CREATE UNIQUE INDEX ux_payment_cpf_competence ON payment (cpf, competence);
