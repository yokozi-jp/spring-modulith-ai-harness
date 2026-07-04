#!/usr/bin/env bash
# フロントエンド全検証コマンド
#
# vp check（Lint + 型チェック + フォーマット）に加え、
# checks/ 内のカスタムチェックスクリプトも実行する。
#
# 使い方:
#   ./scripts/verify.sh        # 全チェック実行
#   ./scripts/verify.sh --fix  # 自動修正可能なものは修正
#
# pre-commit フックと同じチェックを実行する。

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
./scripts/checks/check-hook-location.sh

echo ""
echo "=== features/ 構造チェック ==="
./scripts/checks/check-features-structure.sh

echo ""
echo "=== components/ui/ 編集チェック ==="
./scripts/checks/check-ui-readonly.sh

echo ""
echo "=== src/api/ 編集チェック ==="
./scripts/checks/api-readonly.sh

echo ""
echo "=== テストファイル存在チェック ==="
./scripts/checks/check-test-exists.sh

echo ""
echo "✅ All checks passed."
