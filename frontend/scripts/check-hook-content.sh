#!/usr/bin/env bash
# Hook ファイルの内容を検証する
#
# features/*/hooks/ 内のファイルが Orval 生成 Hook を使わず
# apiClient や fetch を直接呼び出していないかチェック。
#
# 使い方:
#   ./scripts/check-hook-content.sh PATH CONTENT
#   echo "CONTENT" | ./scripts/check-hook-content.sh PATH -

set -euo pipefail

cd "$(dirname "$0")/.."

if [[ $# -lt 2 ]]; then
  echo "Usage: $0 PATH CONTENT" >&2
  echo "       echo CONTENT | $0 PATH -" >&2
  exit 1
fi

FILE_PATH="$1"
CONTENT="$2"

# stdin から読む場合
if [[ "$CONTENT" == "-" ]]; then
  CONTENT=$(cat)
fi

# features/*/hooks/ 内のファイルのみ対象
if [[ "$FILE_PATH" != *src/features/*/hooks/* ]]; then
  exit 0
fi

# テストファイルは除外
if [[ "$FILE_PATH" == *.test.* ]]; then
  exit 0
fi

errors=()

# 禁止パターン1: apiClient を直接 import
if echo "$CONTENT" | grep -qE 'from\s+["\x27]@/lib/api-client["\x27]'; then
  errors+=("apiClient を直接 import しないでください。Orval 生成 Hook (@/api/*) を使ってください。")
fi

# 禁止パターン2: 手書き API を import
if echo "$CONTENT" | grep -qE 'from\s+["\x27]@/lib/[a-z]+-api["\x27]'; then
  errors+=("手書き API を import しないでください。Orval 生成 Hook (@/api/*) を使ってください。")
fi

# 禁止パターン3: fetch を直接使用
if echo "$CONTENT" | grep -qE 'await\s+fetch\s*\('; then
  errors+=("fetch を直接呼び出さないでください。Orval 生成 Hook (@/api/*) を使ってください。")
fi

if [[ ${#errors[@]} -gt 0 ]]; then
  for err in "${errors[@]}"; do
    echo "$err"
  done
  exit 1
fi
