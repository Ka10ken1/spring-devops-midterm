# Spring Boot Midterm API

## Documentation

- [Application README](README.md)
- [DevOps Guide](docs/DEVOPS.md)

## Overview

This is a Spring Boot REST API for managing students, their tasks, and task notes.

The application uses:

- Spring Boot
- Spring Web MVC
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
