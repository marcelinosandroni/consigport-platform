-- ConsigPort — schema V1 (ARCHITECTURE.md §6).
--
-- Flyway owns the schema: Hibernate only validates it. The trail of events is
-- append-only, and the trigger below is the last line of defense.

CREATE TABLE authorizations (
    id              UUID PRIMARY KEY,
    beneficiary_id  VARCHAR(64) NOT NULL,
    agreement_id    VARCHAR(64) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_authorizations_beneficiary_agreement
        UNIQUE (beneficiary_id, agreement_id)
);

CREATE TABLE portability_operations (
    id                  UUID PRIMARY KEY,
    idempotency_key     VARCHAR(64) NOT NULL,
    protocol            VARCHAR(64),
    beneficiary_id      VARCHAR(64) NOT NULL,
    installments        JSONB NOT NULL,
    assignee_creditor   VARCHAR(255) NOT NULL,
    receiving_bank      VARCHAR(64) NOT NULL,
    receiving_branch    VARCHAR(32) NOT NULL,
    receiving_account   VARCHAR(64) NOT NULL,
    total_amount        NUMERIC(15,2) NOT NULL,
    status              VARCHAR(32) NOT NULL,
    rejection_reason    VARCHAR(255),
    retry_count         INTEGER NOT NULL DEFAULT 0,
    next_poll_at        TIMESTAMPTZ,
    account_updated_at  TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL,
    updated_at          TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_portability_operations_idempotency_key UNIQUE (idempotency_key),
    CONSTRAINT uk_portability_operations_protocol UNIQUE (protocol),
    CONSTRAINT ck_portability_operations_status CHECK (
        status IN ('CREATED','PENDING','CONFIRMED','COMPLETED','REJECTED','INTEGRATION_ERROR')
    ),
    CONSTRAINT ck_portability_operations_installments CHECK (
        jsonb_typeof(installments) = 'array' AND jsonb_array_length(installments) > 0
    ),
    CONSTRAINT ck_portability_operations_total_amount CHECK (total_amount >= 0)
);

CREATE INDEX ix_portability_operations_poll
    ON portability_operations (status, next_poll_at);

CREATE TABLE operation_events (
    id              BIGSERIAL PRIMARY KEY,
    operation_id    UUID NOT NULL REFERENCES portability_operations(id),
    previous_status VARCHAR(32),
    new_status      VARCHAR(32) NOT NULL,
    event_type      VARCHAR(48) NOT NULL,
    details         TEXT NOT NULL,
    raw_payload     JSONB,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX ix_operation_events_operation
    ON operation_events (operation_id, created_at);

CREATE OR REPLACE FUNCTION reject_operation_event_mutation()
RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    RAISE EXCEPTION 'operation_events is append-only';
END;
$$;

CREATE TRIGGER trg_operation_events_append_only
BEFORE UPDATE OR DELETE ON operation_events
FOR EACH ROW EXECUTE FUNCTION reject_operation_event_mutation();
