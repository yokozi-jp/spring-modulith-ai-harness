#!/usr/bin/env bash
# components/ui/ の手動編集を検出するスクリプト
# Shadcn/ui で生成されたファイルは原則編集しない。
# カスタマイズが必要な場合は components/features/ にラッパーを作る。
#
# 使い方:
#   ./scripts/check-ui-readonly.sh          # ステージング済みの変更をチェック
#   ./scripts/check-ui-readonly.sh --diff   # ワーキングツリーの変更をチェック

set -euo pipefail

cd "$(dirname "$0")/../.."

UI_DIR="src/components/ui"

if [[ "${1:-}" == "--diff" ]]; then
  changed=$(git diff --name-only -- "$UI_DIR" | grep -v '.gitkeep' || true)
else
  changed=$(git diff --cached --name-only -- "$UI_DIR" | grep -v '.gitkeep' || true)
fi

if [[ -n "$changed" ]]; then
  echo "ERROR: components/ui/ 内のファイルが変更されています。"
  echo "Shadcn/ui コンポーネントは直接編集しないでください。"
  echo "カスタマイズが必要な場合は components/ 直下または features/ 内に作成してください。"
  echo ""
  echo "変更されたファイル:"
  echo "$changed" | sed 's/^/  /'
  echo ""
  echo "もし shadcn add で新規追加した場合は、このチェックを無視してコミットしてください:"
  echo "  git commit --no-verify"
  exit 1
fi
