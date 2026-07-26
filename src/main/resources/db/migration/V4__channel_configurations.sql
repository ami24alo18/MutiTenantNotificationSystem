-- Per-tenant channel configuration (Phase 4).

CREATE TABLE channel_configurations (
    id           UUID PRIMARY KEY,
    tenant_id    UUID         NOT NULL,
    channel      VARCHAR(32)  NOT NULL,
    enabled      BOOLEAN      NOT NULL DEFAULT FALSE,
    provider     VARCHAR(100),
    settings_json VARCHAR(4000) NOT NULL DEFAULT '{}',
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_channel_config_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT uq_channel_config_tenant_channel UNIQUE (tenant_id, channel),
    CONSTRAINT chk_channel_config_channel CHECK (channel IN ('EMAIL', 'SMS', 'PUSH', 'IN_APP'))
);

CREATE INDEX idx_channel_config_tenant_id ON channel_configurations (tenant_id);
