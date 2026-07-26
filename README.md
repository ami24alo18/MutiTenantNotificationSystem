# Multi-Tenant Notification System

Spring Boot service for multi-tenant notifications across email, SMS, push, and in-app channels.

## Phase 1 status

**Completed:** project foundation only (no business logic).

| Capability | Status |
|---|---|
| Spring Boot + Maven project | Done |
| PostgreSQL + Flyway | Done |
| Base packages | Done |
| Global exception handling | Done |
| Validation framework | Done |
| Common API response models | Done |
| Health endpoint | Done |

Later phases (auth, templates, channels, delivery, retries, rate limiting) are intentionally deferred.

## Tech stack

- Java 25
- Spring Boot 4.1
- Spring Data JPA
- PostgreSQL
- Flyway
- Maven
- Bean Validation (`spring-boot-starter-validation`)
- Spring Actuator (health/info)
- Flyway via `spring-boot-starter-flyway` (required for Spring Boot 4 autoconfiguration)

## Prerequisites

- JDK 25+
- Maven 3.9+
- PostgreSQL 14+ listening on `localhost:5432`

### Database setup

```sql
CREATE USER notification WITH PASSWORD 'notification';
CREATE DATABASE notification_db OWNER notification;
GRANT ALL PRIVILEGES ON DATABASE notification_db TO notification;
```

Credentials are configured in `src/main/resources/application.yml` (local defaults for Phase 1).

## Run

```bash
./mvnw spring-boot:run
```

Application listens on `http://localhost:8080`.

### Health checks

- Application probe: `GET /api/v1/health`
- Actuator: `GET /actuator/health`

Example:

```bash
curl http://localhost:8080/api/v1/health
```

## Tests

Tests use an in-memory H2 database (`test` profile) so PostgreSQL is not required for CI-style local runs:

```bash
./mvnw test
```

## Project structure

```
com.multitenant.notification
├── auth/            # Phase 2
├── tenant/          # Phase 2
├── template/        # Phase 3
├── channel/         # Phase 4
├── notification/    # Phase 5+
├── delivery/        # Phase 5+
├── common/          # Shared responses & exceptions
├── config/          # Cross-cutting Spring configuration
└── health/          # Health API
```

## Assumptions (Phase 1)

1. Single deployable Spring Boot monolith (no microservices).
2. PostgreSQL is the system of record; Flyway owns schema evolution (`ddl-auto=validate`).
3. API responses use a consistent envelope (`ApiResponse` / `ErrorResponse`).
4. Package name uses `notification` (corrected from the initial Spring Initializr typo `notificatin`).
5. H2 is test-only; production/local runtime targets PostgreSQL.

## Agent guidance

See [AGENTS.md](AGENTS.md) for phased delivery rules and development conventions.
