#!/usr/bin/env bash
# Kiro CLI preToolUse hook: シェルコマンドの規約を検証。
# ルール実装は frontend/scripts/check-shell-rules.sh に集約。
#
# exit 0: 許可 / exit 2: 阻止（STDERR → AI）

set -euo pipefail

EVENT=$(cat)

COMMAND=$(echo "$EVENT" | grep -oP '"command"\s*:\s*"[^"]*"' | head -1 | grep -oP '(?<=")[^"]+(?="$)' || true)

if [[ -z "$COMMAND" ]]; then
  exit 0
fi

SCRIPTS_DIR="frontend/scripts"
if [[ ! -x "$SCRIPTS_DIR/check-shell-rules.sh" ]]; then
  exit 0
fi

RESULT=$("$SCRIPTS_DIR/check-shell-rules.sh" "$COMMAND" 2>&1) || {
  {
    echo "❌ コマンド規約違反"
    echo ""
    echo "$RESULT"
    echo ""
    echo "参照: .kiro/steering/frontend-rules.md"
  } >&2
  exit 2
}

exit 0
