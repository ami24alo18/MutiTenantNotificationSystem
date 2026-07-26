-- Phase 1 baseline schema.
-- Domain tables (tenants, users, templates, deliveries, etc.) are added in later phases.

CREATE TABLE schema_metadata (
    meta_key    VARCHAR(100) PRIMARY KEY,
    meta_value  VARCHAR(255) NOT NULL,
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO schema_metadata (meta_key, meta_value)
VALUES ('phase', '1'),
       ('description', 'Project foundation baseline');
