#!/usr/bin/env bash
# src/api/ ディレクトリの手動編集を禁止する
#
# src/api/ は Orval による自動生成ディレクトリ。
# .gitkeep 以外のファイルが手動で追加・編集されていないかチェック。
#
# 注意: このチェックは git diff で変更を検出するため、
# npx orval 実行直後は変更として検出される。
# orval 実行後は git add src/api/ してからこのチェックを実行すること。

set -euo pipefail

cd "$(dirname "$0")/../.."

# src/api/ 内で .gitkeep 以外のファイルが git 管理外で変更されているか
# （Orval 生成直後はステージングされていないので検出される）

# git status でトラック外の新規ファイルを検出
untracked=$(git ls-files --others --exclude-standard src/api/ 2>/dev/null | grep -v '.gitkeep' || true)

if [[ -n "$untracked" ]]; then
  echo "❌ src/api/ に手動でファイルを追加しないでください:"
  echo ""
  echo "$untracked" | while read -r f; do
    echo "  $f"
  done
  echo ""
  echo "src/api/ は Orval による自動生成ディレクトリです。"
  echo "'npx orval' で再生成してください。"
  echo ""
  echo "Orval 実行後の場合は 'git add src/api/' でステージングしてください。"
  exit 1
fi
