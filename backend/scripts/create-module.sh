#!/usr/bin/env bash
# モジュール雛形生成スクリプト（モジュールルートの package-info.java のみ作成）
# 使い方: cd backend && ./scripts/create-module.sh [OPTIONS] <module-name>
#
# オプション:
#   --dry-run   作成予定のファイルを表示するのみ
#   --no-test   作成後のアーキテクチャテスト自動実行をスキップする
#
# 例:
#   ./scripts/create-module.sh order
#   ./scripts/create-module.sh --dry-run order

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source=module-common.sh
source "$SCRIPT_DIR/module-common.sh"

# === オプション解析 ===
DRY_RUN=false
NO_TEST=false

while [[ $# -gt 0 ]]; do
  case "$1" in
    --dry-run) DRY_RUN=true; shift ;;
    --no-test) NO_TEST=true; shift ;;
    -*)
      echo "Error: Unknown option '$1'" >&2
      echo "Usage: cd backend && $0 [--dry-run] [--no-test] <module-name>" >&2
      exit 1
      ;;
    *) break ;;
  esac
done

if [ $# -ne 1 ]; then
  echo "Usage: cd backend && $0 [--dry-run] [--no-test] <module-name>" >&2
  echo "Example: ./scripts/create-module.sh order" >&2
  exit 1
fi

MODULE="$1"

# === モジュール名バリデーション ===
if [[ ! "$MODULE" =~ ^[a-z][a-z0-9]*$ ]]; then
  echo "Error: Module name must start with a lowercase letter and contain only lowercase letters and digits." >&2
  echo "  Valid:   order, order2, shipping" >&2
  echo "  Invalid: Order, order-management, 2order, order_mgmt" >&2
  exit 1
fi

MODULE_DIR="$SRC_ROOT/$MODULE"

if [ -d "$MODULE_DIR" ]; then
  echo "Error: Module '$MODULE' already exists at $MODULE_DIR" >&2
  exit 1
fi

# === dry-run モード ===
if [ "$DRY_RUN" = true ]; then
  echo "[CREATE DIR]  $MODULE_DIR/"
  echo "[CREATE FILE] $MODULE_DIR/package-info.java"
  echo ""
  echo "Dry-run complete. No files were created."
  exit 0
fi

# === モジュールルートの package-info.java のみ作成 ===
mkdir -p "$MODULE_DIR"
local_pkg="$BASE_PKG.$MODULE"
generate_package_info "." "" "" "$local_pkg" > "$MODULE_DIR/package-info.java"

echo "Module '$MODULE' created at $MODULE_DIR"
echo "  Created: $MODULE_DIR/package-info.java"
echo ""
echo "Use create-class.sh to add classes (directories are created automatically)."

# === アーキテクチャテスト自動実行 ===
if [ "$NO_TEST" = true ]; then
  echo ""
  echo "Skipping architecture tests (--no-test)."
  exit 0
fi

echo ""
echo "Running architecture tests..."
if ./gradlew test --tests "com.example.demo.architecture.packageinfo.*" --tests "com.example.demo.architecture.modulith.*" --quiet 2>&1; then
  echo "All architecture tests passed."
else
  echo ""
  echo "Architecture tests FAILED. Check the output above for details." >&2
  exit 1
fi
