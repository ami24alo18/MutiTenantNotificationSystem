---
name: template-management
description: Conventions for tenant-scoped notification templates and {{variable}} placeholders. Use when working on template CRUD, preview, or placeholder extraction/validation.
---

# Template Management Conventions

## Model

- Templates belong to exactly one tenant.
- Code is unique within a tenant.
- Channel is one of `EMAIL`, `SMS`, `PUSH`, `IN_APP`.
- Soft delete via `active=false`.

## Placeholders

- Syntax: `{{variableName}}` where name matches `[a-zA-Z_][a-zA-Z0-9_]*`
- Extract from subject + body on create/update
- Reject malformed placeholders
- Preview endpoint renders with provided values and fails on missing keys

## Isolation

- Tenant admin: own tenant only
- Platform admin: any tenant; `tenantId` required on create
