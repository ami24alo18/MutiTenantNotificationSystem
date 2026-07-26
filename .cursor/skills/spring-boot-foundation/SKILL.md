---
name: spring-boot-foundation
description: Conventions for Spring Boot foundation setup in this repository (packages, Flyway, API envelopes, exception handling). Use when touching project bootstrap, common response types, or validation/error handling.
---

# Spring Boot Foundation Conventions

## Package layout

Feature packages under `com.multitenant.notification`:
`auth`, `tenant`, `template`, `channel`, `notification`, `delivery`, plus `common`, `config`, `health`.

## Persistence

- PostgreSQL for runtime
- Flyway migrations in `src/main/resources/db/migration`
- `spring.jpa.hibernate.ddl-auto=validate`
- H2 only for the `test` profile

## API consistency

- Success: `ApiResponse<T>`
- Failure: `ErrorResponse` via `GlobalExceptionHandler`
- Domain failures extend `ApiException`

## Validation

- `spring-boot-starter-validation` for request bodies
- `MethodValidationPostProcessor` for method-parameter constraints
