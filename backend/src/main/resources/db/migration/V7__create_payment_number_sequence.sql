-- Numeracao dos pagamentos gerados pela folha, equivalente ao contador #SEQ-PAYMENT
-- de BATCHPGT.NSP:L465. Fica fora da entidade para nao alterar o construtor publico
-- de Payment, que recebe o numero ja resolvido.
CREATE SEQUENCE payment_number_seq START WITH 100000000 INCREMENT BY 1;
