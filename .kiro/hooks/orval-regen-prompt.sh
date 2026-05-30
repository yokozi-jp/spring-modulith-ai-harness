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

フロントエンドがこの API を使用する場合は、API クライアントを再生成してください:
1. バックエンドが起動中であることを確認
2. cd frontend && npx orval
EOF
