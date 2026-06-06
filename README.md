# Spring Boot API / Devops Integration

## Documentation

- [Application README](README.md)
- [DevOps Guide](docs/DEVOPS.md)

## Overview

This is a Spring Boot REST API for managing students, their tasks, and task notes.

The application uses:

- Spring Boot
- Spring Web MVC
- Spring Security
- Thymeleaf
- Spring Data JPA / Hibernate
- PostgreSQL for the production profile
- H2 for the dev profile, automated tests, and local development
- Bean Validation
- Spring Profiles and externalized configuration
- Internationalization (i18n) for API error and validation messages
- Structured logging with SLF4J (Lombok `@Slf4j`) and Logback
- Swagger UI
- GitHub Actions CI

## Architecture

The project follows a layered structure:

- `controller`: REST endpoints
- `service`: business logic
- `repository`: database access
- `entity`: JPA entities
- `dto`: request and response models
- `exception`: API error handling

## Domain Model

Relationships:

- `Student` has many `Task` records
- `Task` has many `Note` records

Notes are scoped under tasks, and tasks are scoped under students.

## API Paths

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

Health and metadata:

```http
GET /health
```

The `/health` endpoint is public and returns application status plus values from `AppSettings` (`title`, `contactEmail`, `paginationLimit`).

## Profiles And Configuration

The application separates environment-specific settings using Spring Profiles.

| Profile | Database | Purpose |
| --- | --- | --- |
| `dev` | H2 in-memory | Local development with SQL logging and seeded test data |
| `prod` | PostgreSQL | Production-like settings with schema validation |
| `test` | H2 in-memory | Automated tests |

Run with a profile:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

### Custom Configuration (`AppSettings`)

Application settings are externalized in `application.properties` under the `app.settings` prefix and loaded into `AppSettings` with `@ConfigurationProperties`.

| Property | Description | Validation |
| --- | --- | --- |
| `app.settings.title` | Application title | `@NotBlank` |
| `app.settings.pagination-limit` | Default pagination limit | `@Min(1)` |
| `app.settings.contact-email` | Support contact email | `@NotBlank`, `@Email` |

Profile-specific overrides:

- `dev`: `pagination-limit=10`
- `prod`: `pagination-limit=50`

`HealthController` injects `AppSettings` and exposes the values through `/health`.

The `dev` profile also runs `DataInitializer`, which seeds sample students and tasks on startup.

## Internationalization (i18n)

API error and validation messages are localized using message bundles:

- `messages.properties` (default)
- `messages_en.properties` (English)
- `messages_ka.properties` (Georgian)

Locale is resolved from the `Accept-Language` request header via `AcceptHeaderLocaleResolver`.

Examples:

```bash
curl -s -u user:user123 \
  -H "Accept-Language: ka" \
  http://localhost:8080/api/students/999
```

```bash
curl -s -u admin:admin123 \
  -H "Accept-Language: ka" \
  -H "Content-Type: application/json" \
  -X POST http://localhost:8080/api/students \
  -d '{"firstName":"","lastName":"","email":""}'
```

Localized messages are returned in API response bodies through `GlobalExceptionHandler`. DTO validation annotations reference bundle keys such as `{validation.firstname.notblank}`.

Log output remains in English for operational messages; localization applies to client-facing API responses.

## Structured Logging

Logging uses SLF4J through Lombok's `@Slf4j` in services, the exception handler, and dev data initialization.

| Component | Examples |
| --- | --- |
| `StudentService`, `TaskService`, `NoteService` | `INFO` for create/update/delete, `DEBUG` for grid queries, `WARN` for not-found cases |
| `GlobalExceptionHandler` | `WARN` for validation/not-found, `ERROR` for data integrity violations |
| `DataInitializer` | `INFO` for dev seed data loading |

Log configuration is defined in `logback-spring.xml`:

- Console and file appenders
- File output: `logs/app.log`
- Rolling policy: daily rotation with 10 MB size limit, 30-day retention
- Profile-based levels:
  - `dev`: `DEBUG`
  - `prod`: `WARN`
  - other profiles (including `test`): `INFO`

Watch logs while the app is running:

```bash
tail -f logs/app.log
```

## Security

The application uses Spring Security with a Thymeleaf login page, HTTP Basic authentication for API clients, BCrypt password hashing, and in-memory users.

Login page:

```text
http://localhost:8080/login
```

Logout endpoint:

```http
POST /logout
```

Test credentials:

| Username | Password | Roles |
| --- | --- | --- |
| `user` | `user123` | `USER` |
| `admin` | `admin123` | `ADMIN`, `USER` |

Role behavior:

- `USER` can sign in, view existing students, and create/assign tasks for students.
- `ADMIN` can do everything a user can do, plus add, update, and delete students.

Public endpoints:

```http
GET /
GET /health
GET /swagger-ui.html
GET /swagger-ui/**
GET /v3/api-docs/**
```

Protected endpoints:

```http
GET /profile
GET /students
GET /students/{id}
GET /students/{studentId}/tasks/{taskId}
/api/**
```

ADMIN-only functionality:

```http
GET /admin
POST /admin/students
POST /admin/students/{id}/delete
POST /api/students
PUT /api/students/{id}
DELETE /api/students/{id}
```

Method-level security is enabled with `@EnableMethodSecurity`. The `/admin` page plus `StudentService.create` and `StudentService.delete` methods are restricted with `@PreAuthorize("hasRole('ADMIN')")`.

CSRF protection is enabled for the Thymeleaf UI forms. `SecurityConfig` ignores CSRF only for `/api/**` because those JSON endpoints are intended for API clients such as Swagger, curl, and integration tests.

Students:

```http
POST   /api/students
POST   /api/students/grid/paged
GET    /api/students/{id}
PUT    /api/students/{id}
DELETE /api/students/{id}
```

Tasks:

```http
POST   /api/students/{studentId}/tasks
POST   /api/students/{studentId}/tasks/grid/paged
GET    /api/students/{studentId}/tasks/{taskId}
PUT    /api/students/{studentId}/tasks/{taskId}
DELETE /api/students/{studentId}/tasks/{taskId}
```

Notes:

```http
POST   /api/tasks/{taskId}/notes
POST   /api/tasks/{taskId}/notes/grid/paged
GET    /api/tasks/{taskId}/notes/{noteId}
PUT    /api/tasks/{taskId}/notes/{noteId}
DELETE /api/tasks/{taskId}/notes/{noteId}
```

## Grid Request

Grid endpoints support paging, optional filters, and optional sorting.

```json
{
  "page_index": 0,
  "page_size": 10,
  "filters": {
    "name": "example"
  },
  "sorting": {
    "direction": 1,
    "sortingname": "id"
  }
}
```

If `page_size` is omitted or `null`, the grid returns all matching records.

## Database

Database settings depend on the active profile.

**dev profile (H2 in-memory):**

```properties
spring.datasource.url=jdbc:h2:mem:midterm
spring.jpa.hibernate.ddl-auto=create-drop
```

H2 console (dev only): `http://localhost:8080/h2-console`

**prod profile (PostgreSQL):**

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/spring_boot_db
spring.datasource.username=postgres
spring.datasource.password=admin
spring.jpa.hibernate.ddl-auto=validate
```

Create the database before running with the `prod` profile:

```sql
CREATE DATABASE spring_boot_db;
```

Adjust credentials in `src/main/resources/application-prod.properties` if your local PostgreSQL settings differ.

## Run Locally

Development (H2, seeded data, DEBUG logging):

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Production-like (PostgreSQL, WARN logging):

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

## Run Tests

Tests use the `test` profile with H2.

```bash
./mvnw test
```

## Lint

```bash
./scripts/lint.sh
```

## CI/CD And Operations

See the separate [DevOps Guide](docs/DEVOPS.md) for:

- Git branch workflow
- GitHub Actions CI
- one-command environment preparation
- blue-green deployment
- rollback
- health monitoring
