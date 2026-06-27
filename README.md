# Spring Boot API / Devops Integration

## Documentation

- [Application README](README.md)
- [DevOps Guide](docs/DEVOPS.md)

## Overview

This is a Spring Boot REST API for managing students, their tasks, and task notes.

The application uses:

- Java 21, Spring Boot 4.0.6, Maven
- Spring Web MVC, Spring Security, Spring Data JPA / Hibernate
- Thymeleaf for server-side UI rendering
- PostgreSQL (production) / H2 (development, test)
- Bean Validation (Jakarta Validation)
- Spring Profiles and externalized configuration
- Internationalization (i18n) for API error and validation messages (English, Georgian)
- Structured logging with SLF4J (Lombok `@Slf4j`) and Logback
- Swagger UI (springdoc-openapi)
- Code coverage with JaCoCo 0.8.12
- Custom `IRepository<T>` abstraction with `AbstractCrudService` base class and service interfaces
- Static helper utilities (`PaginationUtils`, `RepositoryUtils`, `SpecificationHelper`, `OwnershipValidator`)
- GitHub Actions CI with lint, test, dependency check, Trivy, and GitLeaks
- Docker / Docker Compose for containerized deployment
- Prometheus + Grafana for monitoring and alerting
- Loki + Promtail for centralized log aggregation
- OWASP Dependency Check, Trivy, GitLeaks for security scanning

## Architecture

The project follows a layered structure:

- `controller`: REST endpoints and Thymeleaf page controllers
- `service`: business logic with `AbstractCrudService` base class and per-entity interfaces (`IStudentService`, `ITaskService`, `INoteService`)
- `repository`: database access via `IRepository<T>` (extends `JpaRepository` + `JpaSpecificationExecutor`)
- `entity`: JPA entities (`Student`, `Task`, `Note`)
- `dto`: request and response models
- `exception`: centralized error handling via `GlobalExceptionHandler` with i18n support
- `helper`: reusable utilities (`PaginationUtils`, `RepositoryUtils`, `SpecificationHelper`, `OwnershipValidator`)
- `advice`: cross-cutting concerns (`NavigationControllerAdvice` for Thymeleaf navigation model attributes)

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

### Monitoring Endpoints (custom Actuator)

The application implements a custom actuator stack (no `spring-boot-starter-actuator`) under the `/actuator` prefix.

| Endpoint | Access | Description |
| --- | --- | --- |
| `GET /actuator/health` | Public | DB reachability, disk space, JVM memory — via `AppHealthIndicator` |
| `GET /actuator/info` | Public | App name, version, title, contact email, pagination limit, timestamp |
| `GET /actuator/metrics` | `ADMIN` only | JVM heap, thread count, system load, DB entity counts, custom counters |
| `GET /actuator/metrics/{name}` | `ADMIN` only | Single metric by name (returns 404 if unknown) |

- `AppHealthIndicator` checks DB connectivity via `studentRepository.count()` and reports JVM memory info.
- `MetricsService` exposes JVM metrics (`jvm.memory.heap.used`, `jvm.memory.heap.max`, `jvm.threads.live`, `system.load.average`), DB counts (`db.students.count`, `db.tasks.count`, `db.notes.count`), and custom in-memory counters.

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

Logging is split across layers using SLF4J through Lombok's `@Slf4j`:

| Layer | What gets logged |
| --- | --- |
| REST controllers | `INFO` for incoming create/update/delete API requests |
| Services | `DEBUG` for grid query results, `WARN` for not-found cases |
| `GlobalExceptionHandler` | `WARN` for validation/not-found, `ERROR` for data integrity violations |
| `DataInitializer` | `INFO` for dev seed data loading |

### Logging Filter

`LoggingFilter` (a `OncePerRequestFilter`) injects `requestId` (UUID) and `username` into the MDC for every request. These are available in log patterns via `%X{requestId}` and `%X{username}`.

### Logback Configuration

Log configuration is defined in `logback-spring.xml` with profile-conditional appenders:

| Profile | Appenders | Pattern |
| --- | --- | --- |
| `dev` | `CONSOLE` (stdout) + `ASYNC_FILE` (async wrapper) | Default Spring Boot console pattern |
| `prod` | `PROD_FILE` (rolling file only) | Structured: `%d \| %level \| [%thread] \| %X{requestId} \| %X{username} \| %logger \| %msg` |
| other | `CONSOLE` + `ASYNC_FILE` (same as `dev`) | Default Spring Boot console pattern |

Rolling policies:
- `dev`/other: `logs/app.log`, max 10 MB/file, 30-day history, 100 MB total cap
- `prod`: `logs/app.log`, max 10 MB/file, 60-day history, 500 MB total cap

Profile-based log levels:

| Logger | `dev` | `prod` | other (`test`, etc.) |
| --- | --- | --- | --- |
| `com.spring_midterm.midterm` | `DEBUG` | `INFO` | `INFO` |
| `org.springframework.security` | `DEBUG` | `WARN` | `INFO` |
| `org.hibernate.SQL` | `DEBUG` | `WARN` | `INFO` |
| Root | `INFO` | `WARN` | `INFO` |

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
GET /actuator/health
GET /actuator/info
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
GET /actuator/metrics
GET /actuator/metrics/{name}
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

## Run with Docker (full observability stack)

Start the app, PostgreSQL, and the full observability stack:

```bash
docker compose up --build -d
```

Services:
- App → http://localhost:8080
- Prometheus → http://localhost:9090
- Grafana → http://localhost:3000 (`admin` / `admin`)
- Loki → http://localhost:3100

The app runs with the `docker` profile (PostgreSQL, JSON logging, WARN level) and exposes `/actuator/prometheus` for Prometheus scraping.

Generate traffic to see metrics in Grafana:

```bash
./scripts/generate-traffic.sh 30
```

Trigger the CRITICAL alert:

```bash
./scripts/trigger-alert.sh
```

Check the alert at http://localhost:9090/alerts or in Grafana → Alerting.

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
- Docker Compose observability stack
- Security scanning (OWASP, Trivy, GitLeaks)

## Submission Requirements

| Requirement | Status |
| --- | --- |
| **README** | Comprehensive documentation covering all sections below |
| **Project description** | REST API for managing students, tasks, and notes with layered architecture |
| **Technologies** | Java 21, Spring Boot 4.0.6, Maven, Spring Web MVC, Spring Security, Spring Data JPA, H2/PostgreSQL, Thymeleaf, Swagger, JaCoCo |
| **Run instructions** | `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev` (dev), `-Dspring-boot.run.profiles=prod` (prod), or `docker compose up --build -d` (full stack) |
| **User credentials** | `user` / `user123` (USER), `admin` / `admin123` (ADMIN + USER) |
| **Testing instructions** | `./mvnw test` — 119 tests across unit, integration, validation, and slice test layers |
| **Monitoring endpoints** | `/actuator/health` (public), `/actuator/info` (public), `/actuator/metrics` (ADMIN), `/actuator/metrics/{name}` (ADMIN), `/actuator/prometheus` (public), plus simplified `/health` (public) — Prometheus scrapes `/actuator/prometheus` |
| **Logging configuration** | SLF4J / Logback with profile-conditional appenders (CONSOLE + ASYNC_FILE for dev, PROD_FILE for prod, JSON_CONSOLE for docker), MDC enrichment via `LoggingFilter` (`requestId`, `username`). Loki + Promtail for centralized log aggregation |
| **Profile configuration** | `dev` (H2, DEBUG, seeded data), `prod` (PostgreSQL, WARN), `test` (H2, INFO), `docker` (PostgreSQL via Docker Compose, JSON stdout, WARN) |
| **Monitoring / Observability** | Prometheus metrics via Micrometer (`app_requests_total`, `app_errors_total`, JVM metrics, HikariCP pool). Pre-loaded Grafana dashboard with app metrics, JVM heap, threads, CPU, DB pool, and Loki log panel. Custom actuator at `/actuator/*`, Spring Boot Actuator at `/manage/*` |
| **Alerting** | Prometheus alert rule: `increase(app_errors_total[1m]) > 5` → CRITICAL. Also configured in Grafana Unified Alerting. Severity levels: CRITICAL / WARNING / INFO with runbook |
| **Reliability** | Blue-green deployment, rollback script, health monitor daemon, incident response runbook, service availability objectives (uptime > 99.5%, error rate < 1%) |
| **Security scanning** | OWASP Dependency Check (CI), Trivy filesystem scan (CI), GitLeaks secrets scan (CI) |
| **Docker setup** | Multi-stage `Dockerfile`, `docker-compose.yml` with PostgreSQL, Prometheus, Grafana, Loki, Promtail. Run with `docker compose up --build -d` |
