#!/usr/bin/env bash
# features/ ディレクトリ構造を検証するスクリプト
#
# ルール:
# 1. features/<feature>/ 直下に許可されるのは components/, hooks/, types/ のみ
# 2. hooks/ 内のファイルは use-*.ts のみ
# 3. ファイル名は kebab-case のみ（oxlint でも強制されるが二重チェック）
#
# 使い方:
#   ./scripts/check-features-structure.sh              # 全体スキャン
#   ./scripts/check-features-structure.sh --file PATH  # 単一ファイル検証（フック用）

set -euo pipefail

cd "$(dirname "$0")/.."

FEATURES_DIR="src/features"

# --- 単一ファイル検証モード ---
if [[ "${1:-}" == "--file" ]]; then
  FILE_PATH="$2"

  # features/ 配下でなければ対象外
  REL_PATH="${FILE_PATH##*src/features/}"
  if [[ "$REL_PATH" == "$FILE_PATH" ]]; then
    exit 0
  fi

  FILENAME=$(basename "$FILE_PATH")
  if [[ "$FILENAME" == ".gitkeep" ]]; then
    exit 0
  fi

  errors=()

  # パス構成を分解: <feature>/<subdir>/...
  IFS='/' read -ra PARTS <<< "$REL_PATH"
  FEATURE="${PARTS[0]:-}"
  SUBDIR="${PARTS[1]:-}"
  DEPTH=${#PARTS[@]}

  # features/<feature>/ 直下のファイル（depth=2: feature/filename）
  if [[ $DEPTH -eq 2 && -n "$FEATURE" ]]; then
    errors+=("features/<feature>/ 直下にファイルを配置しないでください。components/, hooks/, types/ 内に配置してください。")
  fi

  # 許可ディレクトリチェック（depth >= 3 の場合のみ: feature/subdir/file）
  if [[ $DEPTH -ge 3 && -n "$SUBDIR" && "$SUBDIR" != "components" && "$SUBDIR" != "hooks" && "$SUBDIR" != "types" ]]; then
    errors+=("features/<feature>/ 配下に許可されるディレクトリは components/, hooks/, types/ のみです。'$SUBDIR/' は使えません。")
  fi

  # hooks/ 内のファイル名チェック
  if [[ "$SUBDIR" == "hooks" && ! "$FILENAME" =~ ^use-.*\.ts$ ]]; then
    errors+=("hooks/ 内のファイルは use-*.ts 形式にしてください。")
  fi

  if [[ ${#errors[@]} -gt 0 ]]; then
    for err in "${errors[@]}"; do
      echo "$err"
    done
    exit 1
  fi
  exit 0
fi

# --- 全体スキャンモード（verify.sh から呼ばれる） ---

# features/ が空（.gitkeep のみ）なら検証不要
if [[ ! -d "$FEATURES_DIR" ]] || [[ -z "$(find "$FEATURES_DIR" -mindepth 1 -not -name '.gitkeep' -print -quit)" ]]; then
  exit 0
fi

errors=()

# 1. features/<feature>/ 直下に許可されないディレクトリがないか
allowed_dirs="components hooks types"
while IFS= read -r dir; do
  dirname=$(basename "$dir")
  if [[ ! " $allowed_dirs " =~ " $dirname " ]]; then
    errors+=("$dir: features/<feature>/ 直下に許可されるディレクトリは components/, hooks/, types/ のみです。")
  fi
done < <(find "$FEATURES_DIR" -mindepth 2 -maxdepth 2 -type d)

# 2. features/<feature>/ 直下にファイルがないか
while IFS= read -r file; do
  basename=$(basename "$file")
  if [[ "$basename" == ".gitkeep" ]]; then
    continue
  fi
  errors+=("$file: features/<feature>/ 直下にファイルを配置しないでください。components/, hooks/, types/ 内に配置してください。")
done < <(find "$FEATURES_DIR" -mindepth 2 -maxdepth 2 -type f)

# 3. hooks/ 内のファイルが use-*.ts パターンか
while IFS= read -r file; do
  basename=$(basename "$file")
  if [[ "$basename" == ".gitkeep" ]]; then
    continue
  fi
  if [[ ! "$basename" =~ ^use-.*\.ts$ ]]; then
    errors+=("$file: hooks/ 内のファイルは use-*.ts 形式にしてください。")
  fi
done < <(find "$FEATURES_DIR" -path "*/hooks/*" -type f 2>/dev/null)

# 4. ファイル名が kebab-case か
while IFS= read -r file; do
  basename=$(basename "$file")
  if [[ "$basename" == ".gitkeep" ]]; then
    continue
  fi
  if [[ "$basename" =~ [A-Z] ]]; then
    errors+=("$file: ファイル名は kebab-case にしてください（大文字禁止）。")
  fi
done < <(find "$FEATURES_DIR" -type f)

if [[ ${#errors[@]} -gt 0 ]]; then
  echo "ERROR: features/ ディレクトリ構造ルール違反が見つかりました。"
  echo ""
  for err in "${errors[@]}"; do
    echo "  $err"
  done
  exit 1
fi
