#!/usr/bin/env bash
# フロントエンド全検証コマンド
# vp check（Lint + 型チェック + フォーマット）に加え、
# カスタムチェックスクリプトも実行する。
#
# 使い方:
#   ./scripts/verify.sh        # 全チェック実行
#   ./scripts/verify.sh --fix  # 自動修正可能なものは修正

set -euo pipefail

cd "$(dirname "$0")/.."

FIX_FLAG=""
if [[ "${1:-}" == "--fix" ]]; then
  FIX_FLAG="--fix"
fi

echo "=== vp check ${FIX_FLAG} ==="
if [[ -n "$FIX_FLAG" ]]; then
  vp check --fix
else
  vp fmt src --check --ignore-path .oxfmtignore
  vp check --no-fmt
fi

echo ""
echo "=== Hook 配置チェック ==="
./scripts/check-hook-location.sh

echo ""
echo "=== features/ 構造チェック ==="
./scripts/check-features-structure.sh

echo ""
echo "=== components/ui/ 編集チェック ==="
./scripts/check-ui-readonly.sh

echo ""
echo "✅ All checks passed."
