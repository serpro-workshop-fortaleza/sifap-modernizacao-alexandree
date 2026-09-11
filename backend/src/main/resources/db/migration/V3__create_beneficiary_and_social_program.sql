-- Contexto 1 (Cadastro de Beneficiarios) e contexto 2 (Catalogo de Programas Sociais),
-- ambos somente leitura na feature 001-benefit-calculation.
-- Origem: BENEFIC.ddm (FNR 150) e SOCPROG.ddm (FNR 151).

CREATE TABLE beneficiary (
    registration_number BIGINT       PRIMARY KEY,
    cpf                 VARCHAR(11)  NOT NULL,
    name                VARCHAR(120) NOT NULL,
    status              VARCHAR(1)   NOT NULL,
    program_code        VARCHAR(4)   NOT NULL,
    region_code         VARCHAR(2)   NOT NULL,
    household_income    NUMERIC(15, 2) NOT NULL,
    dependent_count     INTEGER      NOT NULL,
    birth_date          DATE         NOT NULL,
    registered_at       DATE         NOT NULL,
    CONSTRAINT uk_beneficiary_cpf UNIQUE (cpf),
    CONSTRAINT ck_beneficiary_status CHECK (status IN ('A', 'S', 'C', 'D', 'I')),
    CONSTRAINT ck_beneficiary_dependent_count CHECK (dependent_count >= 0)
);

CREATE INDEX ix_beneficiary_program_code ON beneficiary (program_code);

CREATE TABLE social_program (
    code              VARCHAR(4)     PRIMARY KEY,
    name              VARCHAR(120)   NOT NULL,
    status            VARCHAR(1)     NOT NULL,
    base_amount       NUMERIC(15, 2) NOT NULL,
    -- Zero desliga a verificacao de teto de renda (REQ-003).
    max_income        NUMERIC(15, 2) NOT NULL,
    adjustment_factor NUMERIC(9, 6)  NOT NULL,
    valid_from        VARCHAR(6)     NOT NULL,
    valid_to          VARCHAR(6),
    CONSTRAINT ck_social_program_status CHECK (status IN ('A', 'I')),
    CONSTRAINT ck_social_program_max_income CHECK (max_income >= 0)
);

-- Grupo periodico de 5 faixas de calculo do SOCPROG; band_order preserva a ordem
-- crescente de teto exigida por REQ-005.
CREATE TABLE social_program_income_band (
    id           BIGSERIAL      PRIMARY KEY,
    program_code VARCHAR(4)     NOT NULL,
    band_order   INTEGER        NOT NULL,
    ceiling      NUMERIC(15, 2) NOT NULL,
    factor       NUMERIC(9, 6)  NOT NULL,
    CONSTRAINT fk_income_band_program FOREIGN KEY (program_code) REFERENCES social_program (code),
    CONSTRAINT uk_income_band_order UNIQUE (program_code, band_order)
);
