#!/usr/bin/env bash
# Liquibase changeset の規約準拠を検証するスクリプト
# 使い方: cd backend && ./scripts/lint-changelog.sh
#
# 検証項目:
#   - ファイル名: NNN-<説明>.yaml
#   - author: "system"
#   - sql change type に rollback が存在すること

set -euo pipefail

MIGRATIONS_DIR="${MIGRATIONS_DIR:-src/main/resources/db/changelog/migrations}"
ERRORS=()

if [ ! -d "$MIGRATIONS_DIR" ]; then
  echo "Error: $MIGRATIONS_DIR not found" >&2
  exit 1
fi

for file in "$MIGRATIONS_DIR"/*.yaml; do
  [ -f "$file" ] || continue
  basename=$(basename "$file")

  # ファイル名規約: NNN-<説明>.yaml
  if [[ ! "$basename" =~ ^[0-9]{3}-[a-z0-9-]+\.yaml$ ]]; then
    ERRORS+=("$basename — ファイル名が規約違反。形式: NNN-<説明>.yaml（例: 001-create-users.yaml）")
  fi

  # author: "system"
  if ! grep -q 'author:.*"system"' "$file"; then
    ERRORS+=("$basename — author が \"system\" ではない。author: \"system\" に修正してください")
  fi

  # sql change type に rollback が存在すること
  if grep -q '^\s*- sql:' "$file" || grep -q '^\s*sql:' "$file"; then
    if ! grep -q 'rollback' "$file"; then
      ERRORS+=("$basename — sql change type に rollback が未定義。rollback を追加してください")
    fi
  fi
done

if [ ${#ERRORS[@]} -eq 0 ]; then
  echo "All changelog files pass lint checks."
  exit 0
else
  echo "Changelog lint errors:" >&2
  for err in "${ERRORS[@]}"; do
    echo "  - $err" >&2
  done
  exit 1
fi
