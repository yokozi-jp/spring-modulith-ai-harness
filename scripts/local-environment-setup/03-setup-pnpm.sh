#!/bin/bash
set -e
source ./versions.env

# pnpm インストール
corepack enable
corepack prepare pnpm@${PNPM_VERSION} --activate

echo ""
echo "=== 完了 ==="
echo "pnpm version: $(pnpm -v)"
echo ""
echo "次のステップ："
echo "  ./04-setup-shell.sh を実行"
