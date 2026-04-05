#!/bin/bash
set -e
source ./versions.env
# nvm インストール
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v${NVM_VERSION}/install.sh | bash

echo ""
echo "=== 完了 ==="
echo "次のステップ："
echo "  ./02-setup-java.sh を実行"
