# Legacy Patch Scripts (Archived)

This folder stores one-off patch and fix artifacts that were previously kept in repository root.

These files are not part of the backend runtime, Maven lifecycle, or production deployment path.

## Policy

- Do not add patch or fix artifacts to repository root.
- Prefer permanent source code changes with tests over patch scripts.
- If a temporary migration helper is required, place it under `scripts/migrations/` with date and purpose.
- Remove temporary migration scripts once behavior is merged and validated.

## Archived from Root

- `fix_player.js`
- `patch_match_service.js`
- `patch_scss.js`
- `patch_tooltips.js`
- `patch.java`
- `update_active_effect.js`
