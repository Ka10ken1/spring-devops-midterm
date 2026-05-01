# DevOps Guide

## Branches

The project should keep two active branches:

- `main`: stable production-ready branch
- `dev`: active development branch

Use descriptive commits, for example:

- `feat: add student task endpoints`
- `test: add API integration tests`
- `ci: run tests and lint on push`
- `deploy: add blue-green deployment scripts`

## CI

GitHub Actions workflow: `.github/workflows/ci.yml`

It runs automatically on every push and pull request:

1. `./scripts/lint.sh`
2. `./mvnw test`

## One-Command Environment Preparation

```bash
./scripts/prepare-env.sh
```

This checks required tools, creates the local production directories, and writes deployment configuration under:

```text
/tmp/midterm-production
```

Override location with:

```bash
PROD_ROOT=/path/to/prod ./scripts/prepare-env.sh
```

## Blue-Green Deployment

```bash
./scripts/deploy-blue-green.sh
```

The script builds and tests the app, deploys to the inactive environment, starts it on the inactive port, verifies `/health`, then switches active environment metadata.

Default ports:

- blue: `8081`
- green: `8082`

## Rollback

```bash
./scripts/rollback.sh
```

Rollback switches active environment metadata back to the previous healthy environment.

## Monitoring

```bash
./scripts/health-monitor.sh
```

It periodically checks the active environment `/health` endpoint and writes results to:

```text
/tmp/midterm-production/logs/health-check.log
```

Customize interval:

```bash
INTERVAL_SECONDS=10 ./scripts/health-monitor.sh
```
