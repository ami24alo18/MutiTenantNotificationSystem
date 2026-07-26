# Multi-Tenant Notification System

Spring Boot service for multi-tenant notifications across email, SMS, push, and in-app channels.

## Current status

**Completed through Phase 11** (foundation → documentation). Core send/retry/report paths repaired and aligned with phases 1–4.

| Phase | Capability | Status |
|---|---|---|
| 1 | Foundation, Flyway, health, errors | Done |
| 2 | JWT auth, roles, tenant/user CRUD | Done |
| 3 | Templates + `{{placeholders}}` | Done |
| 4 | Channel config enable/disable | Done |
| 5 | Immediate + scheduled notification APIs | Done |
| 6 | Bounded async worker pool + queue abstraction | Done |
| 7 | Retry with exponential backoff + idempotency | Done |
| 8 | Per-tenant rate limiting + fair scheduler | Done |
| 9 | Delivery reports + attempt audit trail | Done |
| 10 | Unit/integration tests | Done |
| 11 | Documentation | Done |

## Tech stack

- Java 25, Spring Boot 4.1, Spring Data JPA, Spring Security + JWT
- PostgreSQL + Flyway, Maven, Bean Validation, Actuator
- Guava `RateLimiter` for per-tenant intake limits
- In-process bounded `ThreadPoolTaskExecutor` (no Kafka/Redis)

## Run

```bash
./mvnw spring-boot:run
./mvnw test
```

Bootstrap admin: `platform.admin@system.local` / `Admin@123`

## Core APIs

| Area | Paths |
|---|---|
| Auth | `POST /api/v1/auth/login` |
| Tenants/Users | `/api/v1/tenants`, `/api/v1/users` |
| Templates | `/api/v1/templates` (+ `/preview`) |
| Channels | `/api/v1/channels/{EMAIL\|SMS\|PUSH\|IN_APP}` |
| Notifications | `POST /api/v1/notifications/send`, `POST /api/v1/notifications/schedule`, `GET /api/v1/notifications/{id}` |
| Reports | `GET /api/v1/reports/deliveries`, `.../{id}`, `.../{id}/attempts` |

All secured endpoints require `Authorization: Bearer <token>`.

### Send example

```bash
curl -s -X POST http://localhost:8080/api/v1/notifications/send \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "templateId":"...",
    "recipient":"user@example.com",
    "channel":"EMAIL",
    "variables":{"name":"Ada"},
    "idempotencyKey":"order-42-email"
  }'
```

## Design notes

1. **Tenant isolation:** tenant admins scoped to own tenant; platform admins pass `tenantId` where needed.
2. **Templates render** at intake; channel must be enabled for the tenant.
3. **Async dispatch:** `NotificationQueue` → bounded `notificationTaskExecutor`.
4. **Idempotency:** unique `(tenant_id, idempotency_key)`; already-`SENT` deliveries are never re-sent (claim + status guard).
5. **Retry:** exponential backoff (`app.retry.*`); attempts persisted in `delivery_attempts`.
6. **Fairness:** scheduler caps enqueues per tenant per tick.
7. **Stub sender:** no real provider I/O; recipients ending with `+fail@test.local` simulate transient failure.
8. **Rate limit:** per-tenant permits/sec at intake (`app.rate-limiting.*`).

## Assumptions

1. Single Spring Boot monolith.
2. Flyway owns schema (`ddl-auto=validate`).
3. H2 is test-only; runtime uses PostgreSQL (`notification_db`).
4. Channel “send” is stubbed for evaluation (config is real; provider SDKs out of scope).
5. JWT secret in `application.yml` is local-dev only.

## Agent / skills

See [AGENTS.md](AGENTS.md), [Claude.md](Claude.md), and `.cursor/skills/`.
