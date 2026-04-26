#!/usr/bin/env bash
# モジュール雛形生成スクリプト
# 使い方: cd backend && ./scripts/create-module.sh [OPTIONS] <module-name>
#
# オプション:
#   --dry-run   作成予定のディレクトリとファイルを表示するのみ（実際には作成しない）
#   --repair    既存モジュールの不足ディレクトリ・package-info.java を補完する
#   --no-test   作成後のアーキテクチャテスト自動実行をスキップする
#
# 例:
#   ./scripts/create-module.sh order
#   ./scripts/create-module.sh --dry-run order
#   ./scripts/create-module.sh --repair order
#   ./scripts/create-module.sh --repair --no-test order

set -euo pipefail

# === オプション解析 ===
DRY_RUN=false
REPAIR=false
NO_TEST=false

while [[ $# -gt 0 ]]; do
  case "$1" in
    --dry-run) DRY_RUN=true; shift ;;
    --repair)  REPAIR=true; shift ;;
    --no-test) NO_TEST=true; shift ;;
    -*)
      echo "Error: Unknown option '$1'" >&2
      echo "Usage: cd backend && $0 [--dry-run] [--repair] [--no-test] <module-name>" >&2
      exit 1
      ;;
    *) break ;;
  esac
done

if [ $# -ne 1 ]; then
  echo "Usage: cd backend && $0 [--dry-run] [--repair] [--no-test] <module-name>" >&2
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

BASE_PKG="com.example.demo"
SRC_ROOT="src/main/java/com/example/demo"
MODULE_DIR="$SRC_ROOT/$MODULE"

# 新規作成モード: 既存チェック
if [ "$REPAIR" = false ] && [ "$DRY_RUN" = false ] && [ -d "$MODULE_DIR" ]; then
  echo "Error: Module '$MODULE' already exists at $MODULE_DIR" >&2
  echo "  Use --repair to fix missing directories/files" >&2
  exit 1
fi

# repair モード: 存在チェック
if [ "$REPAIR" = true ] && [ ! -d "$MODULE_DIR" ]; then
  echo "Error: Module '$MODULE' does not exist at $MODULE_DIR (nothing to repair)" >&2
  echo "  Remove --repair to create a new module" >&2
  exit 1
fi

# ディレクトリ構成定義: "相対パス|Onionアノテーション|import文"
DIRS=(
  ".|"
  "event|@DomainModelRing|org.jmolecules.architecture.onion.classical.DomainModelRing"
  "domain|@DomainModelRing|org.jmolecules.architecture.onion.classical.DomainModelRing"
  "domain/model|@DomainModelRing|org.jmolecules.architecture.onion.classical.DomainModelRing"
  "domain/model/aggregate|@DomainModelRing|org.jmolecules.architecture.onion.classical.DomainModelRing"
  "domain/model/entity|@DomainModelRing|org.jmolecules.architecture.onion.classical.DomainModelRing"
  "domain/model/valueobject|@DomainModelRing|org.jmolecules.architecture.onion.classical.DomainModelRing"
  "domain/model/valueobject/identifier|@DomainModelRing|org.jmolecules.architecture.onion.classical.DomainModelRing"
  "domain/repository|@DomainModelRing|org.jmolecules.architecture.onion.classical.DomainModelRing"
  "domain/service|@DomainServiceRing|org.jmolecules.architecture.onion.classical.DomainServiceRing"
  "application|@ApplicationServiceRing|org.jmolecules.architecture.onion.classical.ApplicationServiceRing"
  "application/command|@ApplicationServiceRing|org.jmolecules.architecture.onion.classical.ApplicationServiceRing"
  "application/command/dto|@ApplicationServiceRing|org.jmolecules.architecture.onion.classical.ApplicationServiceRing"
  "application/command/handler|@ApplicationServiceRing|org.jmolecules.architecture.onion.classical.ApplicationServiceRing"
  "application/query|@ApplicationServiceRing|org.jmolecules.architecture.onion.classical.ApplicationServiceRing"
  "application/query/dto|@ApplicationServiceRing|org.jmolecules.architecture.onion.classical.ApplicationServiceRing"
  "application/query/service|@ApplicationServiceRing|org.jmolecules.architecture.onion.classical.ApplicationServiceRing"
  "presentation|@InfrastructureRing|org.jmolecules.architecture.onion.classical.InfrastructureRing"
  "presentation/controller|@InfrastructureRing|org.jmolecules.architecture.onion.classical.InfrastructureRing"
  "presentation/request|@InfrastructureRing|org.jmolecules.architecture.onion.classical.InfrastructureRing"
  "presentation/response|@InfrastructureRing|org.jmolecules.architecture.onion.classical.InfrastructureRing"
  "infrastructure|@InfrastructureRing|org.jmolecules.architecture.onion.classical.InfrastructureRing"
  "infrastructure/db|@InfrastructureRing|org.jmolecules.architecture.onion.classical.InfrastructureRing"
  "infrastructure/db/repository|@InfrastructureRing|org.jmolecules.architecture.onion.classical.InfrastructureRing"
  "infrastructure/db/query|@InfrastructureRing|org.jmolecules.architecture.onion.classical.InfrastructureRing"
)

# === package-info.java の内容を生成する関数 ===
generate_package_info() {
  local rel_path="$1"
  local annotation="$2"
  local import_stmt="$3"
  local pkg="$4"

  if [ -z "$annotation" ]; then
    # モジュールルート: @NullMarked のみ
    cat << EOF
@NullMarked
package $pkg;

import org.jspecify.annotations.NullMarked;
EOF
  elif [ "$rel_path" = "event" ]; then
    # event: @NamedInterface 追加
    cat << EOF
@NullMarked
@NamedInterface("event")
$annotation
package $pkg;

import $import_stmt;
import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.NamedInterface;
EOF
  else
    cat << EOF
@NullMarked
$annotation
package $pkg;

import $import_stmt;
import org.jspecify.annotations.NullMarked;
EOF
  fi
}

# === メイン処理 ===
created_dirs=0
created_files=0
skipped=0

for entry in "${DIRS[@]}"; do
  IFS='|' read -r rel_path annotation import_stmt <<< "$entry"

  if [ "$rel_path" = "." ]; then
    dir="$MODULE_DIR"
    pkg="$BASE_PKG.$MODULE"
  else
    dir="$MODULE_DIR/$rel_path"
    sub_pkg=$(echo "$rel_path" | tr '/' '.')
    pkg="$BASE_PKG.$MODULE.$sub_pkg"
  fi

  pkg_info="$dir/package-info.java"

  # dry-run モード
  if [ "$DRY_RUN" = true ]; then
    if [ ! -d "$dir" ]; then
      echo "[CREATE DIR]  $dir/"
    fi
    if [ ! -f "$pkg_info" ]; then
      echo "[CREATE FILE] $pkg_info"
    else
      echo "[SKIP]        $pkg_info (exists)"
    fi
    continue
  fi

  # repair モード: 既存はスキップ
  if [ "$REPAIR" = true ] && [ -d "$dir" ] && [ -f "$pkg_info" ]; then
    skipped=$((skipped + 1))
    continue
  fi

  # ディレクトリ作成
  if [ ! -d "$dir" ]; then
    mkdir -p "$dir"
    created_dirs=$((created_dirs + 1))
  fi

  # package-info.java 作成
  if [ ! -f "$pkg_info" ]; then
    generate_package_info "$rel_path" "$annotation" "$import_stmt" "$pkg" > "$pkg_info"
    created_files=$((created_files + 1))
  fi
done

# === 結果表示 ===
if [ "$DRY_RUN" = true ]; then
  echo ""
  echo "Dry-run complete. No files were created."
  exit 0
fi

if [ "$REPAIR" = true ]; then
  echo "Repair complete for module '$MODULE':"
  echo "  Created: $created_dirs directories, $created_files package-info.java files"
  echo "  Skipped: $skipped (already exist)"
else
  echo "Module '$MODULE' created at $MODULE_DIR"
  echo ""
  echo "Directories created:"
  find "$MODULE_DIR" -type d | sort | sed "s|$SRC_ROOT/||"
fi

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
