# Multi-Tenant Notification System

Spring Boot service for multi-tenant notifications across email, SMS, push, and in-app channels.

## Current status

**Completed through Phase 4:** foundation + auth + templates + channel configuration.

| Phase | Capability | Status |
|---|---|---|
| 1 | Project foundation, Flyway, health, error handling | Done |
| 2 | JWT auth, roles, tenant/user CRUD | Done |
| 3 | Notification templates, placeholders, tenant isolation | Done |
| 4 | Per-tenant channel config (email/SMS/push/in-app) | Done |
| 5+ | Notifications, retries, rate limits | Not started |

## Tech stack

- Java 25
- Spring Boot 4.1
- Spring Data JPA
- Spring Security + JWT (JJWT)
- PostgreSQL
- Flyway (`spring-boot-starter-flyway`)
- Maven
- Bean Validation
- Spring Actuator

## Prerequisites

- JDK 25+
- Maven 3.9+
- PostgreSQL 14+ on `localhost:5432`

### Database setup

```sql
CREATE USER notification WITH PASSWORD 'notification';
CREATE DATABASE notification_db OWNER notification;
GRANT ALL PRIVILEGES ON DATABASE notification_db TO notification;
```

## Run

```bash
./mvnw spring-boot:run
```

App: `http://localhost:8080`

### Bootstrap platform admin

On first startup (when no `PLATFORM_ADMIN` exists), the service creates:

- Email: `platform.admin@system.local`
- Password: `Admin@123`

Configured under `app.bootstrap.platform-admin` in `application.yml`.

### Health

```bash
curl http://localhost:8080/api/v1/health
```

### Login

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"platform.admin@system.local","password":"Admin@123"}'
```

Use the returned `accessToken` as `Authorization: Bearer <token>`.

## Phase 2 APIs

| Method | Path | Access |
|---|---|---|
| POST | `/api/v1/auth/login` | Public |
| GET/POST | `/api/v1/tenants` | Platform admin (tenant admin: GET own only) |
| GET/PUT/DELETE | `/api/v1/tenants/{id}` | Platform admin (tenant admin: GET own) |
| GET/POST | `/api/v1/users` | Platform / tenant admin (scoped) |
| GET/PUT/DELETE | `/api/v1/users/{id}` | Platform / tenant admin (scoped) |

### Role rules

- `PLATFORM_ADMIN`: no tenant; manages all tenants and users.
- `TENANT_ADMIN`: belongs to one tenant; manages users in that tenant only; cannot create/update/delete tenants.

Deletes are soft (`active=false`).

## Phase 3 APIs — templates

| Method | Path | Access |
|---|---|---|
| POST | `/api/v1/templates` | Platform / tenant admin |
| GET | `/api/v1/templates` | Platform (all) / tenant (own) |
| GET | `/api/v1/templates/{id}` | Platform / owning tenant admin |
| PUT | `/api/v1/templates/{id}` | Platform / owning tenant admin |
| DELETE | `/api/v1/templates/{id}` | Platform / owning tenant admin (soft) |
| POST | `/api/v1/templates/{id}/preview` | Platform / owning tenant admin |

### Template rules

- Placeholders use `{{variableName}}` syntax (letters, digits, underscore).
- Variables are extracted from subject + body and returned on create/update.
- `EMAIL` templates require a subject; other channels may omit it.
- Channels: `EMAIL`, `SMS`, `PUSH`, `IN_APP`.
- Code is unique per tenant.
- Platform admins must pass `tenantId` when creating templates.
- Tenant admins are locked to their own tenant (cross-tenant access returns 404).

Example create:

```bash
curl -s -X POST http://localhost:8080/api/v1/templates \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "code":"welcome-email",
    "name":"Welcome Email",
    "channel":"EMAIL",
    "subject":"Welcome {{firstName}}",
    "body":"Hello {{firstName}}, your id is {{userId}}."
  }'
```

## Phase 4 APIs — channel configuration

| Method | Path | Access |
|---|---|---|
| GET | `/api/v1/channels?tenantId=` | Platform (tenantId required) / tenant admin (own) |
| GET | `/api/v1/channels/{channel}` | Same |
| PUT | `/api/v1/channels/{channel}` | Upsert enable/disable + provider + settings |
| PUT | `/api/v1/channels/{channel}/enabled` | Toggle only |

Channels: `EMAIL`, `SMS`, `PUSH`, `IN_APP`. Missing configs are auto-created as disabled on first list/get.

Required settings when enabling:

| Channel | Required settings keys |
|---|---|
| EMAIL | `fromAddress` |
| SMS | `senderId` |
| PUSH | `appId` |
| IN_APP | `retentionDays` (1–3650) |

## Tests

```bash
./mvnw test
```

Tests use H2 (`test` profile).

## Project structure

```
com.multitenant.notification
├── auth/            # JWT, security, users, login
├── tenant/          # Tenant CRUD
├── template/        # Template CRUD + placeholders
├── channel/         # Channel enable/disable + settings
├── notification/    # Phase 5+
├── delivery/        # Phase 5+
├── common/          # Shared responses & exceptions
├── config/          # Cross-cutting Spring configuration
└── health/          # Health API
```

## Assumptions

1. Single Spring Boot monolith (no microservices).
2. Flyway owns schema; JPA `ddl-auto=validate`.
3. Consistent API envelopes (`ApiResponse` / `ErrorResponse`).
4. Email is globally unique and is the login identifier.
5. Platform admins are not tenant-scoped; tenant admins must belong to a tenant.
6. JWT HMAC secret is local-dev only (`app.jwt.secret`) — replace before any shared environment.
7. H2 is test-only; runtime uses PostgreSQL.
8. Template placeholders use `{{name}}`; malformed placeholders are rejected at write time.
9. Template preview substitutes variables but does not send notifications (Phase 5+).
10. Channel providers are configuration-only in Phase 4 (no external provider calls yet).

## Agent guidance

See [AGENTS.md](AGENTS.md) for phased delivery rules.
