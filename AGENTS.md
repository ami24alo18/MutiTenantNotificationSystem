# AGENTS.md

Guidance for AI agents working on the Multi-Tenant Notification System.

## Non-negotiable delivery rules

1. Do **not** implement everything in one go.
2. Work **only** on the current phase requested by the human.
3. **Stop** after completing that phase and wait for explicit approval.
4. Do **not** start the next phase without explicit human confirmation.
5. Do **not** refactor unless required for the current phase.
6. Keep changes minimal and production quality.
7. Follow SOLID principles and clean architecture.
8. Every phase must compile successfully.
9. Add unit tests whenever appropriate for the phase.
10. Update README only when the phase introduces a visible feature.

## Stack (fixed)

- Java
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Flyway
- Maven

Do **not** add Docker, Kubernetes, Kafka, Redis, or other distributed components unless a later phase explicitly requires them.

## Phase roadmap

| Phase | Scope | Expected commit message |
|------:|-------|-------------------------|
| 1 | Project foundation | `Initial project setup` |
| 2 | Authentication & RBAC | `Authentication and RBAC` |
| 3 | Template management | `Template management` |
| 4 | Channel configuration | `Channel configuration` |
| 5 | Notification APIs | `Notification APIs` |
| 6 | Async processing | `Async notification processing` |
| 7 | Retry mechanism | `Retry mechanism` |
| 8 | Rate limiting | `Tenant rate limiting` |
| 9 | Delivery reports | `Delivery tracking` |
| 10 | Testing pass | `Tests` |
| 11 | Documentation | `Documentation` |

## Architecture conventions

- Package by feature under `com.multitenant.notification`.
- Shared cross-cutting concerns live in `common` and `config`.
- Controllers stay thin; business rules belong in services (introduced with domain phases).
- Persist schema only via Flyway migrations; keep `spring.jpa.hibernate.ddl-auto=validate`.
- Prefer constructor injection.
- Return consistent API envelopes (`ApiResponse` / `ErrorResponse`).
- Map failures through `GlobalExceptionHandler`.

## Commit ownership

The human reviews, tests, and creates commits unless they explicitly ask the agent to commit.
Suggested commit messages per phase are listed above.

## Skills used during development

Project-local skill notes live under `.cursor/skills/` and capture conventions applied while building each phase:

- `phase-gated-delivery`
- `spring-boot-foundation`
- `auth-rbac`
