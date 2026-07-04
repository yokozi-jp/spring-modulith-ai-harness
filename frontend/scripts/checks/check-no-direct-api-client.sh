#!/usr/bin/env bash
# features/*/hooks/ 内で apiClient を直接使用していないか検証する
#
# Orval 生成 Hook を使わず apiClient や fetch を直接呼び出すのは禁止。
# このチェックは verify.sh から呼び出される。

set -euo pipefail

cd "$(dirname "$0")/../.."

errors=()

# features/*/hooks/ 内の .ts ファイルを検索（テストファイル除外）
while IFS= read -r -d '' file; do
  # apiClient を直接 import しているか
  if grep -qE 'from\s+["\x27]@/lib/api-client["\x27]' "$file"; then
    errors+=("$file: apiClient を直接 import しないでください。Orval 生成 Hook (@/api/*) を使ってください。")
  fi

  # 手書き API を import しているか
  if grep -qE 'from\s+["\x27]@/lib/[a-z]+-api["\x27]' "$file"; then
    errors+=("$file: 手書き API を import しないでください。Orval 生成 Hook (@/api/*) を使ってください。")
  fi

  # fetch を直接使用しているか
  if grep -qE 'await\s+fetch\s*\(' "$file"; then
    errors+=("$file: fetch を直接呼び出さないでください。Orval 生成 Hook (@/api/*) を使ってください。")
  fi
done < <(find src/features -path "*/hooks/*.ts" ! -name "*.test.*" -print0 2>/dev/null)

if [[ ${#errors[@]} -gt 0 ]]; then
  echo "❌ Orval 生成 Hook を使用してください（apiClient 直接使用禁止）:"
  echo ""
  for err in "${errors[@]}"; do
    echo "  $err"
  done
  echo ""
  echo "修正方法: @/api/<tag>/<tag>.ts から生成された Hook を import してください。"
  echo "例: import { useList2 } from \"@/api/category/category\";"
  exit 1
fi
