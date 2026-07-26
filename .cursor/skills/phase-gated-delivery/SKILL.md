---
name: phase-gated-delivery
description: Enforce incremental phase delivery for the multi-tenant notification service. Use when implementing features, planning work, or deciding whether to continue to the next phase.
---

# Phase-Gated Delivery

## Instructions

1. Identify the phase the human explicitly requested.
2. Implement only that phase's checklist items.
3. Ensure the project compiles and relevant tests pass.
4. Update README only if the phase adds a user-visible capability.
5. Stop. Do not start the next phase until the human approves.

## Phase 1 checklist

- Spring Boot project structure
- PostgreSQL + Flyway configuration
- Initial migration
- Base packages
- Global exception handling
- Validation framework
- Common response models
- Health endpoint
- Application starts successfully
- No business/domain logic

## Phase 2 checklist

- User and Tenant entities
- Roles: PLATFORM_ADMIN, TENANT_ADMIN
- JWT authentication + Spring Security
- Login API
- User CRUD
- Tenant CRUD
- No notification logic

## Phase 3 checklist

- NotificationTemplate entity
- Template CRUD APIs
- Variable placeholders (`{{name}}`)
- Validation (syntax, EMAIL subject, unique code per tenant)
- Tenant isolation
- No send/dispatch logic

## Phase 4 checklist

- Channel configuration entity
- EMAIL / SMS / PUSH / IN_APP
- Enable/disable per tenant
- Tenant-specific settings
- No external provider dispatch
