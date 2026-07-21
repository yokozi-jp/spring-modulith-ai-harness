#!/usr/bin/env bash
# Kiro CLI stop hook: フロントエンドのLint違反パターンを抽出し、
# ステアリング追加候補として出力する。
#
# AI の応答が frontend/ 配下のファイルに変更を加えた場合にのみ実行。
# exit 0 + STDOUT → AI のコンテキストに追加される。

set -euo pipefail

# hook は cwd がプロジェクトルートで実行される
SCRIPT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
FRONTEND_DIR="${SCRIPT_DIR}/frontend"

if [[ ! -d "$FRONTEND_DIR" ]]; then
  exit 0
fi

# stdin から hook event を読む（assistant_response が含まれる）
EVENT=$(cat)

# assistant_response に frontend 関連の変更が含まれるか簡易チェック
RESPONSE=$(echo "$EVENT" | grep -o '"assistant_response"' 2>/dev/null || true)
if [[ -z "$RESPONSE" ]]; then
  exit 0
fi

# git で frontend/ に変更があるか確認
CHANGED=$(git -C "$SCRIPT_DIR" diff --name-only -- frontend/ 2>/dev/null | head -20)
if [[ -z "$CHANGED" ]]; then
  exit 0
fi

# Lint + 型チェック実行（エラーがあっても続行）
LINT_OUTPUT=$(cd "$FRONTEND_DIR" && ./node_modules/.bin/vp check 2>&1 || true)

# エラーがなければ何も出力しない
if echo "$LINT_OUTPUT" | grep -q "Found no warnings"; then
  # フォーマットも通っているか確認
  if ! echo "$LINT_OUTPUT" | grep -q "Formatting issues\|error"; then
    exit 0
  fi
fi

# すべてpassならexit
if echo "$LINT_OUTPUT" | grep -qP "^pass:.*pass:" 2>/dev/null; then
  exit 0
fi
if [[ $(echo "$LINT_OUTPUT" | grep -c "^pass:") -ge 2 ]]; then
  exit 0
fi

# エラーからルール名を抽出して集計
VIOLATIONS=$(echo "$LINT_OUTPUT" | grep -oP '(?<=\()\S+(?=\))' | grep -v "ms\|threads\|files" | sort | uniq -c | sort -rn | head -10)

if [[ -z "$VIOLATIONS" ]]; then
  exit 0
fi

# ステアリング候補として出力
cat <<EOF
---
⚠️ Lint違反パターン検出（frontend）

以下のルールに違反するコードが生成されました。
ステアリング (.kiro/steering/frontend-lint-fix-guide.md) に修正方法が記載されていないルールがあれば追記を検討してください。

違反ルール（頻度順）:
${VIOLATIONS}

対応:
1. 上記の違反を修正してください
2. 繰り返し発生するルールは frontend-lint-fix-guide.md への追記を検討してください
3. 修正後 ./scripts/verify.sh で確認してください
---
EOF
