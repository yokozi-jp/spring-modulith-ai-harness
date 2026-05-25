#!/usr/bin/env bash
# Hook の定義場所を検証するスクリプト
# - use-*.ts ファイル以外で export function use... を定義していたらエラー
# - use-*.ts ファイルは hooks/ 内にあること
#
# 使い方:
#   ./scripts/check-hook-location.sh              # 全体スキャン
#   ./scripts/check-hook-location.sh --file PATH  # 単一ファイル検証（フック用）

set -euo pipefail

cd "$(dirname "$0")/.."

# --- 単一ファイル検証モード ---
if [[ "${1:-}" == "--file" ]]; then
  FILE_PATH="$2"
  FILENAME=$(basename "$FILE_PATH")

  # src/ 配下でなければ対象外
  if [[ "$FILE_PATH" != *src/* ]]; then
    exit 0
  fi

  REL_PATH="${FILE_PATH##*src/}"

  errors=()

  # use-*.ts ファイルが正しいディレクトリにあるか
  if [[ "$FILENAME" == use-*.ts ]]; then
    if [[ "$REL_PATH" != hooks/* && "$REL_PATH" != features/*/hooks/* ]]; then
      errors+=("Hookファイルは src/hooks/ または src/features/<feature>/hooks/ 内に配置してください。")
    fi
  fi

  if [[ ${#errors[@]} -gt 0 ]]; then
    for err in "${errors[@]}"; do
      echo "$err"
    done
    exit 1
  fi
  exit 0
fi

# --- 全体スキャンモード ---

errors=()

# use-*.ts 以外のファイルで export function use... を定義しているか検出
while IFS= read -r file; do
  basename=$(basename "$file")
  if [[ "$basename" == use-* ]]; then
    continue
  fi
  if [[ "$file" == *"routeTree.gen"* ]]; then
    continue
  fi
  if grep -qE "^export function use[A-Z]" "$file" 2>/dev/null; then
    errors+=("$file: Hookの定義は use-*.ts ファイルで行ってください。")
  fi
done < <(find src -name "*.ts" -o -name "*.tsx" | grep -v node_modules)

# use-*.ts が正しいディレクトリにあるか検証
while IFS= read -r file; do
  if [[ "$file" != src/hooks/* && "$file" != src/features/*/hooks/* ]]; then
    errors+=("$file: Hookファイルは src/hooks/ または src/features/<feature>/hooks/ 内に配置してください。")
  fi
done < <(find src -name "use-*.ts" | grep -v node_modules)

if [[ ${#errors[@]} -gt 0 ]]; then
  echo "ERROR: Hook の配置ルール違反が見つかりました。"
  echo ""
  for err in "${errors[@]}"; do
    echo "  $err"
  done
  exit 1
fi
