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
- PostgreSQL for the main application database
- H2 for automated tests and local production simulation
- Bean Validation
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

Health check:

```http
GET /health
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

The main application uses PostgreSQL:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/spring_boot_db
spring.datasource.username=postgres
spring.datasource.password=admin
```

Create the database before running:

```sql
CREATE DATABASE spring_boot_db;
```

Adjust `spring.datasource.username` and `spring.datasource.password` in `src/main/resources/application.properties` if your local PostgreSQL credentials are different.

## Run Locally

```bash
./mvnw spring-boot:run
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
