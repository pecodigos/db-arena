# DB-Arena backend

This is the Spring Boot backend for Dragon Ball Arena.

## Setup

The backend requires a database and Redis. You can start the necessary infrastructure using Docker Compose:

```bash
docker compose up -d
```

## Run

To start the Spring Boot application:

```bash
./mvnw spring-boot:run
```

Default local endpoints:
- API: `http://localhost:8080`
- WebSocket: `ws://localhost:8080/ws`

## Build

To build the executable JAR:

```bash
./mvnw clean package -DskipTests
```

## Test

To run unit and integration tests:

```bash
./mvnw test
```
Tests use the `test` Spring profile with an in-memory H2 database.

## Configuration

The application can be configured via environment variables:

- `DB_ARENA_DATASOURCE_URL` (default: local PostgreSQL)
- `DB_ARENA_DATASOURCE_USERNAME`
- `DB_ARENA_DATASOURCE_PASSWORD`
- `DB_ARENA_REDIS_HOST`
- `DB_ARENA_REDIS_PORT`
- `DB_ARENA_FRONTEND_ORIGIN`
- `DB_ARENA_JWT_SECRET`
- `DB_ARENA_JWT_EXPIRATION`
- `DB_ARENA_JPA_DDL_AUTO`

If none are provided, development defaults are used. Note that `DB_ARENA_JWT_SECRET` falls back to a built-in development secret, but must be explicitly set for production environments.
