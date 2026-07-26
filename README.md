# Multi-Tenant Notification System

A scalable, multi-tenant notification service built with Spring Boot. It supports multiple channels (email, SMS, push, in-app), tenant-defined templates, scheduled sends, rate limiting, and automatic retries with exponential backoff.

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [API Documentation](#api-documentation)
- [Configuration](#configuration)
- [Setup and Usage](#setup-and-usage)
- [Testing](#testing)

## Features

- **Multi-Tenancy**: Isolated data and configuration for each tenant.
- **Multiple Channels**: Supports Email, SMS, Push, and In-App notifications.
- **Template Management**: Tenants can create and manage their own notification templates with dynamic variable substitution.
- **Immediate & Scheduled Notifications**: APIs for both immediate and scheduled delivery.
- **Asynchronous Processing**: A bounded worker pool processes notifications concurrently for high throughput.
- **Retry Mechanism**: Automatic retries for transient failures with exponential backoff.
- **Rate Limiting**: Per-tenant rate limiting to ensure fair usage and prevent system abuse.
- **Delivery Reports & Audit Trail**: Detailed tracking of each delivery attempt for reporting and auditing.
- **Role-Based Access Control (RBAC)**: `PLATFORM_ADMIN` and `TENANT_ADMIN` roles with scoped permissions.

## Tech Stack

- **Java 25**
- **Spring Boot 4.1**
- **Spring Data JPA** & **Hibernate**
- **Spring Security** & **JWT**
- **PostgreSQL**: Primary data store.
- **Flyway**: For database schema migrations.
- **Maven**: Build automation.
- **Guava**: For rate limiting.
- **JUnit 5 & Mockito**: For unit and integration testing.

## Architecture

The system is designed as a monolithic Spring Boot application following clean architecture principles.

1.  **Controllers**: Expose REST APIs, handle input validation, and delegate to services.
2.  **Services**: Contain the core business logic, orchestrating repositories, and other components.
3.  **Repositories**: Manage data persistence using Spring Data JPA.
4.  **Entities**: JPA entities representing the database schema.
5.  **Async Processing**: The `NotificationController` publishes a `NotificationEvent`. An `@Async` `NotificationEventListener` picks up the event and processes the notification in a separate thread from a bounded pool, ensuring the API remains responsive.
6.  **Retry Logic**: If a notification fails, the `RetryService` schedules it for a future attempt with an exponentially increasing delay. A `@Scheduled` task periodically scans for and re-triggers these retries.
7.  **Rate Limiting**: The `RateLimitingService` uses an in-memory Guava `RateLimiter` for each tenant to control the rate of incoming notification requests.

## Project Structure

```
com.multitenant.notification
├── auth/            # JWT, security, user management
├── channel/         # Channel configuration APIs
├── common/          # Shared exceptions and response models
├── config/          # Spring Boot and application configuration
├── delivery/        # Delivery and DeliveryAttempt entities and repositories
├── dto/             # Data Transfer Objects for API requests/responses
├── event/           # Application events (e.g., NotificationEvent)
├── health/          # Health check endpoint
├── listener/        # Asynchronous event listeners
├── report/          # Delivery report APIs
├── service/         # Business logic services
├── template/        # Template management APIs
└── tenant/          # Tenant management APIs
```

## API Documentation

All APIs are prefixed with `/api/v1`.

### Authentication

| Method | Path                | Description      | Access |
| ------ | ------------------- | ---------------- | ------ |
| POST   | `/auth/login`       | Authenticate     | Public |

### Notifications

| Method | Path                   | Description                  | Access       |
| ------ | ---------------------- | ---------------------------- | ------------ |
| POST   | `/notifications/send`  | Send a notification          | Tenant Admin |
| POST   | `/notifications/schedule`| Schedule a notification      | Tenant Admin |

### Delivery Reports

| Method | Path                | Description                       | Access       |
| ------ | ------------------- | --------------------------------- | ------------ |
| GET    | `/reports/deliveries` | Get delivery reports with filters | Tenant Admin |

**Query Parameters for `/reports/deliveries`**:
- `tenantId` (Long)
- `status` (PENDING, SENT, FAILED, etc.)
- `startDate` (ISO 8601 DateTime)
- `endDate` (ISO 8601 DateTime)

### Other APIs
(Previously defined APIs for Tenants, Users, Templates, and Channels remain available.)

## Configuration

Key settings in `application.properties`:

```properties
# Rate Limiting
notification.rate-limiting.enabled=true
notification.rate-limiting.limit=100
notification.rate-limiting.window-in-seconds=60

# Retry Mechanism
notification.retry.max-attempts=3
notification.retry.initial-delay-ms=1000
notification.retry.max-delay-ms=5000
```

## Setup and Usage

### Prerequisites

- JDK 25+
- Maven 3.9+
- PostgreSQL 14+

### Database Setup

Connect to PostgreSQL and run:
```sql
CREATE USER user WITH PASSWORD 'password';
CREATE DATABASE notification_system OWNER user;
```

### Running the Application

```bash
./mvnw spring-boot:run
```
The application will be available at `http://localhost:8080`.

## Testing

Run all unit and integration tests:
```bash
./mvnw test
```
Tests use an in-memory H2 database and run in a `test` profile.
