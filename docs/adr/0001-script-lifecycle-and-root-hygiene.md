# ADR 0001: Script Lifecycle and Root Hygiene

- Status: Accepted
- Date: 2026-04-06

## Context

The repository accumulated one-off patch and fix artifacts in root, which reduced architectural clarity and made maintenance workflows inconsistent.

## Decision

1. Root-level patch/fix/update artifacts are not allowed.
2. Active temporary migration helpers must live in `scripts/migrations/`.
3. Archived one-off scripts must live in `scripts/legacy-patches/`.
4. Root hygiene is enforced by `./scripts/check-root-hygiene.sh` and by CI.
5. Permanent source changes plus tests are preferred over ad-hoc patch artifacts.

## Consequences

- Cleaner repository root and clearer module boundaries.
- Predictable location for active and historical script artifacts.
- Automatic guardrails through CI and local checks.
- Better long-term maintainability and onboarding.
