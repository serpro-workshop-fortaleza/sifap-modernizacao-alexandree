-- Data de geracao do pagamento, equivalente a PAYMENT.DT-GENERATION do legado
-- (BATCHPGT.NSP:L478). Preenche as linhas existentes antes de tornar a coluna obrigatoria.
ALTER TABLE payment ADD COLUMN generated_at DATE;

UPDATE payment SET generated_at = CURRENT_DATE WHERE generated_at IS NULL;

ALTER TABLE payment ALTER COLUMN generated_at SET NOT NULL;
