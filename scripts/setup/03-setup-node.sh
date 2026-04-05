#!/bin/bash
set -e
source ./versions.env

# nvm を現在のシェルで有効化
export NVM_DIR="$HOME/.nvm"
[ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"

# Node インストール
nvm install ${NODE_VERSION}
nvm alias default ${NODE_VERSION}

echo ""
echo "=== 完了 ==="
echo "Node version: $(node -v)"
echo "npm version: $(npm -v)"
echo ""
echo "次のステップ："
echo "  ./04-setup-shell.sh を実行"
