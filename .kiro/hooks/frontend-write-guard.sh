#!/usr/bin/env bash
# Kiro CLI preToolUse hook: frontend/ へのファイル書き込み前に規約を検証。
# ルール実装はすべて frontend/scripts/ に集約。このフックは呼び出すだけ。
#
# exit 0: 許可 / exit 2: 阻止（STDERR → AI）

set -euo pipefail

EVENT=$(cat)

FILE_PATH=$(echo "$EVENT" | grep -oP '"path"\s*:\s*"[^"]*"' | head -1 | grep -oP '(?<=")[^"]+(?="$)' || true)

if [[ -z "$FILE_PATH" ]]; then
  exit 0
fi

if [[ "$FILE_PATH" != *frontend/src/* ]]; then
  exit 0
fi

# scripts ディレクトリを解決（hook は cwd=プロジェクトルートで実行される）
SCRIPTS_DIR="frontend/scripts"
if [[ ! -d "$SCRIPTS_DIR" ]]; then
  exit 0
fi

errors=()

# 汎用ファイルルール（kebab-case, ui禁止, api禁止, routes制限）
RESULT=$("$SCRIPTS_DIR/check-file-rules.sh" "$FILE_PATH" 2>&1) || true
if [[ -n "$RESULT" ]]; then
  while IFS= read -r line; do errors+=("$line"); done <<< "$RESULT"
fi

# features/ 構造ルール
if [[ "$FILE_PATH" == *src/features/* ]]; then
  RESULT=$("$SCRIPTS_DIR/check-features-structure.sh" --file "$FILE_PATH" 2>&1) || true
  if [[ -n "$RESULT" ]]; then
    while IFS= read -r line; do errors+=("$line"); done <<< "$RESULT"
  fi
fi

# hook 配置ルール
FILENAME=$(basename "$FILE_PATH")
if [[ "$FILENAME" == use-*.ts ]]; then
  RESULT=$("$SCRIPTS_DIR/check-hook-location.sh" --file "$FILE_PATH" 2>&1) || true
  if [[ -n "$RESULT" ]]; then
    while IFS= read -r line; do errors+=("$line"); done <<< "$RESULT"
  fi
fi

# 結果
if [[ ${#errors[@]} -gt 0 ]]; then
  {
    echo "❌ フロントエンド規約違反: $FILE_PATH"
    echo ""
    for err in "${errors[@]}"; do
      echo "  • $err"
    done
    echo ""
    echo "参照: .kiro/steering/frontend-rules.md"
  } >&2
  exit 2
fi

exit 0
