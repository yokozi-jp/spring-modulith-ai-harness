#!/usr/bin/env bash
# 単一ファイルの配置ルールを検証する
#
# frontend/src/ 配下の全ファイルに適用される汎用ルール:
# 1. ファイル名は kebab-case
# 2. components/ui/ は編集禁止
# 3. api/ は編集禁止
# 4. routes/ は .tsx のみ
#
# 使い方:
#   ./scripts/check-file-rules.sh PATH

set -euo pipefail

cd "$(dirname "$0")/.."

if [[ $# -eq 0 ]]; then
  echo "Usage: $0 PATH" >&2
  exit 1
fi

FILE_PATH="$1"

# src/ 配下でなければ対象外
if [[ "$FILE_PATH" != *src/* ]]; then
  exit 0
fi

REL_PATH="${FILE_PATH##*src/}"
FILENAME=$(basename "$FILE_PATH")

if [[ "$FILENAME" == ".gitkeep" ]]; then
  exit 0
fi

errors=()

# 1. kebab-case
if [[ "$FILENAME" =~ [A-Z] ]]; then
  errors+=("ファイル名は kebab-case にしてください: '$FILENAME'")
fi

# 2. components/ui/ 編集禁止
if [[ "$REL_PATH" == components/ui/* ]]; then
  errors+=("components/ui/ は Shadcn/ui の自動生成ファイルです。直接編集しないでください。カスタマイズは components/ 直下または features/<feature>/components/ に配置してください。")
fi

# 3. api/ 編集禁止
if [[ "$REL_PATH" == api/* ]]; then
  errors+=("src/api/ は Orval による自動生成ディレクトリです。手動で編集しないでください。'npx orval' で再生成してください。")
fi

# 4. routes/ は .tsx のみ
if [[ "$REL_PATH" == routes/* && "$FILENAME" != *.tsx ]]; then
  errors+=("routes/ 内は .tsx ファイルのみ配置してください。")
fi

if [[ ${#errors[@]} -gt 0 ]]; then
  for err in "${errors[@]}"; do
    echo "$err"
  done
  exit 1
fi
