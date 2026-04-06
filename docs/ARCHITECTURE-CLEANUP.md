# Dragon Ball Arena Backend Architecture and Cleanup

## Goals

- Preserve battle correctness while improving service boundaries.
- Reduce large multi-purpose classes into focused collaborators.
- Make matchmaking, turn execution, persistence, and transport responsibilities explicit.

## Current Observations

- `MatchService` is a large coordinator with many responsibilities (queueing, match creation, turn validation, turn execution, redis hydration, normalization, and websocket fan-out).
- `Player` model carries a large portion of battle rule execution logic.
- Root-level patch scripts are mixed with application code and build files.

## Target Backend Shape

Use package-by-feature with layered responsibilities inside each feature:

```text
com.pecodigos.dbarena
  user/
    api/
    application/
    domain/
    infrastructure/
  ingame/
    battle/
      api/
        websocket/
        rest/
      application/
        matchmaking/
        turn/
        matchstate/
      domain/
        model/
        rules/
        policies/
      infrastructure/
        redis/
        persistence/
        messaging/
```

## Responsibility Boundaries

- API layer
  - Controllers only.
  - No battle decision logic.
- Application layer
  - Orchestrates use-cases: search, cancel, get-match, end-turn, forfeit.
- Domain layer
  - Rule execution and invariants.
  - No framework dependencies.
- Infrastructure layer
  - Redis persistence, repositories, messaging adapters.

## Match Service Split Plan (Safe Sequence)

1. Extract queue and pairing logic into `MatchmakingService`.
2. Extract turn validation and execution into `TurnResolutionService`.
3. Extract redis cache/load operations into `MatchStateStore`.
4. Extract legacy normalization into `LegacyMatchNormalizer`.
5. Keep `MatchService` as application orchestrator/facade.

## Player Model Split Plan

1. Keep `Player` as state holder and simple state transitions.
2. Move heavy calculations to domain services:
   - `DamageResolutionPolicy`
   - `StatusEffectPolicy`
   - `EnergyConsumptionPolicy`
3. Unit test policies independently from transport and persistence.

## Script Hygiene

- Move temporary patch/fix scripts into `scripts/legacy-patches/`.
- Keep active migration helpers in `scripts/migrations/`.
- Enforce root hygiene with `./scripts/check-root-hygiene.sh`.
- Avoid keeping migration scripts at repo root after a stable release.

## Definition of Done for Refactor PRs

- Gameplay semantics unchanged unless explicitly requested.
- Existing tests pass (`./mvnw test`).
- New/changed behavior covered with focused tests.
- Service splits keep websocket payload contracts stable.
