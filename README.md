# DB-Arena backend

Spring Boot backend for Dragon Ball Arena.

## Quick start

### Start infra

```bash
docker compose up -d
```

### Run the app

```bash
./mvnw spring-boot:run
```

Default local endpoints:
- API: http://localhost:8080
- WebSocket: http://localhost:8080/ws

## Tests

```bash
./mvnw test
```

Tests use the `test` Spring profile with an in-memory H2 database.

## Config

The app supports environment variables:

- `DB_ARENA_DATASOURCE_URL`
- `DB_ARENA_DATASOURCE_USERNAME`
- `DB_ARENA_DATASOURCE_PASSWORD`
- `DB_ARENA_REDIS_HOST`
- `DB_ARENA_REDIS_PORT`
- `DB_ARENA_FRONTEND_ORIGIN`
- `DB_ARENA_JWT_SECRET`
- `DB_ARENA_JWT_EXPIRATION`
- `DB_ARENA_JPA_DDL_AUTO`

If not set, it defaults to local development values.
`DB_ARENA_JWT_SECRET` now falls back to a built-in localhost-only development secret and still fails fast outside local development, so shared and production environments must continue to set it explicitly.
