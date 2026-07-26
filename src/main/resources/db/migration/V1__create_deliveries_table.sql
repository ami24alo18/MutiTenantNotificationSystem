CREATE TABLE deliveries (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    channel VARCHAR(255) NOT NULL,
    recipient VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(255) NOT NULL,
    scheduled_at TIMESTAMP,
    sent_at TIMESTAMP
);
