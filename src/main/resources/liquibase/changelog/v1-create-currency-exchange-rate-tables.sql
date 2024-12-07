-- liquibase formatted sql
-- changeset RomanTrukhanovich:1
CREATE TABLE currency
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(3) NOT NULL UNIQUE
);
--rollback DROP TABLE currency;

-- changeset RomanTrukhanovich:2
CREATE TABLE exchange_rate
(
    id                 SERIAL PRIMARY KEY,
    base_currency_id   INT            NOT NULL,
    target_currency_id INT            NOT NULL,
    rate               DECIMAL(19, 4) NOT NULL,
    updated            TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_base_currency FOREIGN KEY (base_currency_id) REFERENCES currency (id),
    CONSTRAINT fk_target_currency FOREIGN KEY (target_currency_id) REFERENCES currency (id),
    CONSTRAINT unique_base_target UNIQUE (base_currency_id, target_currency_id)
);
--rollback DROP TABLE exchange_rate;

-- changeset RomanTrukhanovich:3
CREATE INDEX idx_exchange_rate_base_currency ON exchange_rate (base_currency_id);
--rollback DROP INDEX IF EXISTS idx_exchange_rate_base_currency;

-- changeset RomanTrukhanovich:4
CREATE INDEX idx_exchange_rate_target_currency ON exchange_rate (target_currency_id);
--rollback DROP INDEX IF EXISTS idx_exchange_rate_target_currency;
