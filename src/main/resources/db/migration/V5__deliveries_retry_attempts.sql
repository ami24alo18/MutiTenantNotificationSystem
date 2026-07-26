-- Deliveries, retry metadata, attempts, and idempotency (Phases 5-9).

CREATE TABLE deliveries (
    id                UUID PRIMARY KEY,
    tenant_id         UUID         NOT NULL,
    template_id       UUID,
    channel           VARCHAR(32)  NOT NULL,
    recipient         VARCHAR(255) NOT NULL,
    subject           VARCHAR(500),
    content           TEXT         NOT NULL,
    status            VARCHAR(32)  NOT NULL,
    idempotency_key   VARCHAR(100),
    scheduled_at      TIMESTAMP WITH TIME ZONE,
    processing_at     TIMESTAMP WITH TIME ZONE,
    sent_at           TIMESTAMP WITH TIME ZONE,
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    retry_attempts    INT          NOT NULL DEFAULT 0,
    next_retry_at     TIMESTAMP WITH TIME ZONE,
    last_error        VARCHAR(1000),
    version           BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT fk_deliveries_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_deliveries_template FOREIGN KEY (template_id) REFERENCES notification_templates (id),
    CONSTRAINT chk_deliveries_channel CHECK (channel IN ('EMAIL', 'SMS', 'PUSH', 'IN_APP')),
    CONSTRAINT chk_deliveries_status CHECK (status IN (
        'PENDING', 'SCHEDULED', 'PROCESSING', 'SENT', 'FAILED', 'RETRY'
    )),
    CONSTRAINT uq_deliveries_tenant_idempotency UNIQUE (tenant_id, idempotency_key)
);

CREATE INDEX idx_deliveries_tenant_id ON deliveries (tenant_id);
CREATE INDEX idx_deliveries_status ON deliveries (status);
CREATE INDEX idx_deliveries_scheduled_at ON deliveries (scheduled_at);
CREATE INDEX idx_deliveries_next_retry_at ON deliveries (next_retry_at);
CREATE INDEX idx_deliveries_created_at ON deliveries (created_at);

CREATE TABLE delivery_attempts (
    id                 UUID PRIMARY KEY,
    delivery_id        UUID         NOT NULL,
    attempt_number     INT          NOT NULL,
    success            BOOLEAN      NOT NULL,
    response_message   VARCHAR(1000),
    attempted_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_delivery_attempts_delivery FOREIGN KEY (delivery_id) REFERENCES deliveries (id)
);

CREATE INDEX idx_delivery_attempts_delivery_id ON delivery_attempts (delivery_id);
