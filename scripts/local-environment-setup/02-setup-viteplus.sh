#!/bin/bash
set -e

curl -fsSL https://vite.plus | bash

echo ""
echo "=== 完了 ==="
echo "⚠️ プロンプトで Y を入力して Node.js バージョン管理を VITE+ に任せてください"
echo "インストール後、以下を実行してシェルを再読み込みしてください："
echo "  source ~/.bashrc"
echo ""
echo "次のステップ："
echo "  ./03-setup-pnpm.sh を実行"
