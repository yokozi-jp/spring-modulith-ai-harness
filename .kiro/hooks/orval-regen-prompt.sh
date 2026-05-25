#!/usr/bin/env bash
# Kiro CLI postToolUse hook: backend の Controller/Request/Response を変更した後、
# Orval 再生成を促す（API定義が変わった可能性があるため）。
#
# exit 0 で STDOUT を AI コンテキストに追加。

set -euo pipefail

EVENT=$(cat)

FILE_PATH=$(echo "$EVENT" | grep -oP '"path"\s*:\s*"[^"]*"' | head -1 | grep -oP '(?<=")[^"]+(?="$)' || true)

if [[ -z "$FILE_PATH" ]]; then
  exit 0
fi

# backend の presentation 層（Controller, Request, Response）のみ対象
if [[ "$FILE_PATH" != *backend/src/main/java/*/presentation/* ]]; then
  exit 0
fi

cat <<EOF
🔄 バックエンド API 定義が変更されました。

変更ファイル: $(basename "$FILE_PATH")

フロントエンドの API クライアント（型 + hooks）を最新化するには:
1. バックエンドが起動中であることを確認
2. cd frontend && npx orval

これにより src/api/ の型定義と React Query hooks が再生成されます。
バックエンドが起動していない場合は、実装完了後にまとめて実行してください。
EOF
