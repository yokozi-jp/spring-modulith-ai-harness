#!/usr/bin/env bash
# Kiro CLI stop hook: フロントエンド変更後に ./scripts/verify.sh を実行し、
# 失敗していれば AI に強く通知する。
#
# stop フックは exit code によるブロックができない（PreToolUse のみ可能）。
# そのため exit 0 を維持しつつ、STDOUT で失敗内容を明示し、
# AI が次のターンで確実に対応するよう仕向ける。
#
# AI の応答が frontend/ 配下のファイルに変更を加えた場合にのみ実行。

set -euo pipefail

# hook は cwd がプロジェクトルートで実行される
SCRIPT_DIR="$(pwd)"
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

# git で frontend/ に変更があるか確認（追跡済みの変更 + 新規ファイル）
CHANGED_TRACKED=$(git -C "$SCRIPT_DIR" diff --name-only -- frontend/ 2>/dev/null || true)
CHANGED_UNTRACKED=$(git -C "$SCRIPT_DIR" ls-files --others --exclude-standard -- frontend/ 2>/dev/null || true)
CHANGED=$(printf '%s\n%s\n' "$CHANGED_TRACKED" "$CHANGED_UNTRACKED" | grep -v '^$' | head -20)
if [[ -z "$CHANGED" ]]; then
  exit 0
fi

# verify.sh を実行（vp check + shell カスタムチェック5種すべて）
VERIFY_OUTPUT=$(cd "$FRONTEND_DIR" && ./scripts/verify.sh 2>&1) && VERIFY_EXIT=0 || VERIFY_EXIT=$?

# 全チェック成功なら何も出力しない
if [[ "$VERIFY_EXIT" -eq 0 ]]; then
  exit 0
fi

# 失敗内容をそのまま出力し、AI に対応を強く要求する
cat <<EOF
---
🛑 ./scripts/verify.sh が失敗しました（frontend）

以下の変更ファイルに対する検証でエラーが検出されました:
${CHANGED}

--- verify.sh 出力 ---
${VERIFY_OUTPUT}
--- 出力終わり ---

対応（次のターンで必ず実施）:
1. 上記のエラーをすべて修正する
2. 修正後、再度 ./scripts/verify.sh を実行し、"All checks passed." が
   表示されることを確認する
3. shell カスタムチェック（配置ルール・components/ui・src/api 誤編集・
   テスト未作成）の違反は vp check では検出されないため、
   このメッセージが唯一の検出機会になる場合がある

繰り返し発生する oxlint ルール違反は
.kiro/steering/frontend-lint-fix-guide.md への追記も検討すること。
---
EOF
