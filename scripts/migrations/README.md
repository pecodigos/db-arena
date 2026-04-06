# Migration Scripts

Use this folder for temporary migration helpers that are still actively needed.

Naming convention:

- `YYYYMMDD-short-purpose.js`
- `YYYYMMDD-short-purpose.sh`
- `YYYYMMDD-short-purpose.sql`

Rules:

- Document purpose, inputs, and rollback behavior at the top of the script.
- Do not keep temporary migration helpers at repository root.
- Remove scripts once changes are merged and validated.
- If retention is required, move the script to `scripts/legacy-patches/`.
