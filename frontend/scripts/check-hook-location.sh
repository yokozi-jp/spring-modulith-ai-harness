#!/usr/bin/env bash
# Hook の定義場所を検証するスクリプト
# - use-*.ts ファイル以外で export function use... を定義していたらエラー
# - use-*.ts ファイルは hooks/ または components/features/ 内にあること
#
# 使い方:
#   ./scripts/check-hook-location.sh

set -euo pipefail

cd "$(dirname "$0")/.."

errors=()

# use-*.ts 以外のファイルで export function use... を定義しているか検出
while IFS= read -r file; do
  # use- で始まるファイル名はOK
  basename=$(basename "$file")
  if [[ "$basename" == use-* ]]; then
    continue
  fi
  # routeTree.gen.ts は除外
  if [[ "$file" == *"routeTree.gen"* ]]; then
    continue
  fi
  # export function use... を含むか
  if grep -qE "^export function use[A-Z]" "$file" 2>/dev/null; then
    errors+=("$file: Hookの定義は use-*.ts ファイルで行ってください。")
  fi
done < <(find src -name "*.ts" -o -name "*.tsx" | grep -v node_modules)

# use-*.ts が正しいディレクトリにあるか検証
while IFS= read -r file; do
  if [[ "$file" != src/hooks/* && "$file" != src/components/features/* ]]; then
    errors+=("$file: Hookファイルは src/hooks/ または src/components/features/ 内に配置してください。")
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
