CREATE TABLE payment (
    payment_number BIGINT PRIMARY KEY,
    cpf VARCHAR(11) NOT NULL,
    registration_number BIGINT NOT NULL,
    program_code VARCHAR(4) NOT NULL,
    competence VARCHAR(6) NOT NULL,
    cycle INTEGER NOT NULL,
    gross_amount NUMERIC(15, 2) NOT NULL,
    net_amount NUMERIC(15, 2) NOT NULL,
    discount_total NUMERIC(15, 2) NOT NULL,
    bonus_amount NUMERIC(15, 2) NOT NULL,
    status VARCHAR(1) NOT NULL
);

CREATE TABLE payment_discount (
    id BIGSERIAL PRIMARY KEY,
    payment_number BIGINT NOT NULL,
    discount_type VARCHAR(3) NOT NULL,
    amount NUMERIC(15, 2) NOT NULL,
    percentage NUMERIC(5, 2),
    case_number VARCHAR(20),
    CONSTRAINT fk_payment_discount_payment
        FOREIGN KEY (payment_number) REFERENCES payment (payment_number)
);

CREATE INDEX ix_payment_cpf_competence ON payment (cpf, competence);
