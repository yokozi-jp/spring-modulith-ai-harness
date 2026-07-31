#!/bin/bash
# postToolUse hook: E2E テストファイル書き込み後に ESLint を即時実行する。
# lint 違反があれば exit ≠ 0 で STDERR を AI に返し、修正を促す。
#
# 前提:
#   - E2E コンテナが起動済み（make e2e-up）
#   - jq がホストにインストール済み
#
# 対象ファイル:
#   - e2e/tests/**
#   - e2e/pages/**

set -euo pipefail

# hook event を STDIN から読み取り
EVENT=$(cat)

# 書き込み先パスを取得
PATH_WRITTEN=$(echo "$EVENT" | jq -r '.tool_input.path // empty')

# パスが空なら何もしない
if [[ -z "$PATH_WRITTEN" ]]; then
  exit 0
fi

# e2e/tests/ または e2e/pages/ 以外は対象外
if [[ "$PATH_WRITTEN" != e2e/tests/* && "$PATH_WRITTEN" != e2e/pages/* ]]; then
  exit 0
fi

# TypeScript ファイルのみ対象
if [[ "$PATH_WRITTEN" != *.ts && "$PATH_WRITTEN" != *.tsx ]]; then
  exit 0
fi

# コンテナ内で lint 実行
# compose.e2e.yaml のサービスが起動していない場合は警告して終了
if ! docker compose -f compose.yaml -f compose.e2e.yaml ps --status running e2e-runner >/dev/null 2>&1; then
  # コンテナ未起動の場合は run --rm で一時起動して lint
  docker compose -f compose.yaml -f compose.e2e.yaml run --rm e2e-runner \
    npx eslint "$PATH_WRITTEN" 2>&1
else
  docker compose -f compose.yaml -f compose.e2e.yaml exec e2e-runner \
    npx eslint "$PATH_WRITTEN" 2>&1
fi
