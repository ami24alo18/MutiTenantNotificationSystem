-- Tenants and users for authentication / RBAC (Phase 2).

CREATE TABLE tenants (
    id          UUID PRIMARY KEY,
    code        VARCHAR(64)  NOT NULL,
    name        VARCHAR(255) NOT NULL,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_tenants_code UNIQUE (code)
);

CREATE TABLE users (
    id              UUID PRIMARY KEY,
    tenant_id       UUID,
    email           VARCHAR(255) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(255) NOT NULL,
    role            VARCHAR(32)  NOT NULL,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT fk_users_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT chk_users_role CHECK (role IN ('PLATFORM_ADMIN', 'TENANT_ADMIN')),
    CONSTRAINT chk_users_tenant_for_role CHECK (
        (role = 'PLATFORM_ADMIN' AND tenant_id IS NULL)
        OR (role = 'TENANT_ADMIN' AND tenant_id IS NOT NULL)
    )
);

CREATE INDEX idx_users_tenant_id ON users (tenant_id);
CREATE INDEX idx_users_role ON users (role);
