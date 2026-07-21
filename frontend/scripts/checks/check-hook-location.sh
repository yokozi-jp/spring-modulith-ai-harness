#!/usr/bin/env bash
# Hook ファイルの配置場所を検証するスクリプト
#
# use-*.ts ファイルは hooks/ ディレクトリ内にあること。
# Hook 定義の検出（use-*.ts 以外での定義禁止）は oxlint カスタムルールで行う。
#
# 使い方:
#   ./scripts/checks/check-hook-location.sh              # 全体スキャン
#   ./scripts/checks/check-hook-location.sh --file PATH  # 単一ファイル検証（staged 用）

set -euo pipefail

cd "$(dirname "$0")/../.."

# --- 単一ファイル検証モード ---
if [[ "${1:-}" == "--file" ]]; then
  FILE_PATH="$2"
  FILENAME=$(basename "$FILE_PATH")

  # src/ 配下でなければ対象外
  if [[ "$FILE_PATH" != *src/* ]]; then
    exit 0
  fi

  REL_PATH="${FILE_PATH##*src/}"

  # use-*.ts ファイルが正しいディレクトリにあるか
  if [[ "$FILENAME" == use-*.ts ]]; then
    if [[ "$REL_PATH" != hooks/* && "$REL_PATH" != features/*/hooks/* ]]; then
      echo "Hookファイルは src/hooks/ または src/features/<feature>/hooks/ 内に配置してください。"
      exit 1
    fi
  fi
  exit 0
fi

# --- 全体スキャンモード ---

errors=()

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
