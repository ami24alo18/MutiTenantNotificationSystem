-- Notification templates with per-tenant isolation (Phase 3).

CREATE TABLE notification_templates (
    id          UUID PRIMARY KEY,
    tenant_id   UUID         NOT NULL,
    code        VARCHAR(100) NOT NULL,
    name        VARCHAR(255) NOT NULL,
    channel     VARCHAR(32)  NOT NULL,
    subject     VARCHAR(500),
    body        TEXT         NOT NULL,
    variables   VARCHAR(2000) NOT NULL DEFAULT '[]',
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_templates_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT uq_templates_tenant_code UNIQUE (tenant_id, code),
    CONSTRAINT chk_templates_channel CHECK (channel IN ('EMAIL', 'SMS', 'PUSH', 'IN_APP'))
);

CREATE INDEX idx_templates_tenant_id ON notification_templates (tenant_id);
CREATE INDEX idx_templates_channel ON notification_templates (channel);
