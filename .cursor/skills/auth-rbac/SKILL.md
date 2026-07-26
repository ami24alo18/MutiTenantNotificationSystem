---
name: auth-rbac
description: Conventions for JWT authentication and RBAC in this notification service. Use when working on login, users, tenants, roles, or Spring Security configuration.
---

# Auth & RBAC Conventions

## Roles

- `PLATFORM_ADMIN` — global scope, `tenant_id` must be null
- `TENANT_ADMIN` — tenant-scoped, `tenant_id` required

Authorities are exposed as `ROLE_PLATFORM_ADMIN` / `ROLE_TENANT_ADMIN`.

## Security

- Stateless JWT bearer auth (`Authorization: Bearer <token>`)
- Public: `/api/v1/auth/login`, `/api/v1/health`, actuator health/info
- Method security via `@PreAuthorize`
- Soft deletes (`active=false`) for users and tenants

## Isolation

- Platform admin can manage all tenants/users
- Tenant admin can only access their own tenant and its users
- Tenant admins cannot create platform admins or other tenants
