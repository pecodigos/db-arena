#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "$0")/.." && pwd)"

offenders=()
while IFS= read -r -d '' file_path; do
  file_name="$(basename "$file_path")"

  if [[ "$file_name" =~ ^(patch|fix)[-_a-zA-Z0-9]*\.(js|ts|sh|bash|java)$ ]] \
    || [[ "$file_name" =~ ^update[-_a-zA-Z0-9]*\.(js|ts|sh|bash)$ ]]; then
    offenders+=("$file_name")
  fi
done < <(find "$repo_root" -maxdepth 1 -type f -print0)

if (( ${#offenders[@]} > 0 )); then
  printf '%s\n' "Repository hygiene check failed. Root-level patch/fix/update files found:" >&2
  printf '%s\n' "${offenders[@]}" | sort | sed 's/^/- /' >&2
  printf '%s\n' "Move these files to scripts/migrations/ or scripts/legacy-patches/." >&2
  exit 1
fi

printf '%s\n' "Repository hygiene check passed. No root-level patch/fix/update scripts found."
