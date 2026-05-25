#!/usr/bin/env bash
# フロントエンドのシェルコマンド規約を検証する
#
# ルール:
# 1. npm/pnpm/yarn の直接使用禁止（vp 経由）
# 2. vitest/oxlint/oxfmt の直接インストール禁止（Vite+ 内蔵）
# 3. npx ではなく vp dlx を使用（orval は例外）
#
# 使い方:
#   ./scripts/check-shell-rules.sh "COMMAND"

set -euo pipefail

if [[ $# -eq 0 ]]; then
  echo "Usage: $0 COMMAND" >&2
  exit 1
fi

COMMAND="$1"

# 1. npm/pnpm/yarn 直接使用禁止
if echo "$COMMAND" | grep -qE '^\s*(npm|pnpm|yarn)\s+(install|add|remove|run|exec)'; then
  echo "npm/pnpm/yarn を直接使わないでください。すべて 'vp' 経由で操作してください。"
  echo ""
  echo "代替コマンド:"
  echo "  vp install         # 依存インストール"
  echo "  vp install -D pkg  # devDependency追加"
  echo "  vp run <script>    # スクリプト実行"
  echo "  vp exec <cmd>      # node_modules/.bin 内のコマンド実行"
  exit 1
fi

# 2. vitest/oxlint/oxfmt 直接インストール禁止
if echo "$COMMAND" | grep -qE 'install.*\b(vitest|oxlint|oxfmt)\b'; then
  echo "vitest/oxlint/oxfmt を直接インストールしないでください。Vite+ に内蔵されています。"
  echo ""
  echo "正しい使い方:"
  echo "  vp test   # テスト実行 (Vitest内蔵)"
  echo "  vp lint   # Lint実行 (oxlint内蔵)"
  echo "  vp fmt    # フォーマット (oxfmt内蔵)"
  exit 1
fi

# 3. npx → vp dlx（orval は例外）
if echo "$COMMAND" | grep -qE '^\s*npx\s' && ! echo "$COMMAND" | grep -q 'orval'; then
  echo "npx ではなく 'vp dlx' を使用してください。"
  echo ""
  echo "例: vp dlx shadcn@latest add button"
  exit 1
fi
