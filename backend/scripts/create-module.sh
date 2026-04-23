#!/usr/bin/env bash
# モジュール雛形生成スクリプト
# 使い方: cd backend && ./scripts/create-module.sh <module-name>
# 例:     cd /home/projects/spring-modulith-ai-harness/backend && ./scripts/create-module.sh order

set -euo pipefail

if [ $# -ne 1 ]; then
  echo "Usage: cd backend && $0 <module-name>" >&2
  echo "Example: cd /home/projects/spring-modulith-ai-harness/backend && ./scripts/create-module.sh order" >&2
  exit 1
fi

MODULE="$1"
BASE_PKG="com.example.demo"
SRC_ROOT="src/main/java/com/example/demo"
MODULE_DIR="$SRC_ROOT/$MODULE"

if [ -d "$MODULE_DIR" ]; then
  echo "Error: Module '$MODULE' already exists at $MODULE_DIR" >&2
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

  mkdir -p "$dir"

  if [ -z "$annotation" ]; then
    # モジュールルート: @NullMarked のみ
    cat > "$dir/package-info.java" << EOF
@NullMarked
package $pkg;

import org.jspecify.annotations.NullMarked;
EOF
  elif [ "$rel_path" = "event" ]; then
    # event: @NamedInterface 追加
    cat > "$dir/package-info.java" << EOF
@NullMarked
@NamedInterface("event")
$annotation
package $pkg;

import $import_stmt;
import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.NamedInterface;
EOF
  else
    cat > "$dir/package-info.java" << EOF
@NullMarked
$annotation
package $pkg;

import $import_stmt;
import org.jspecify.annotations.NullMarked;
EOF
  fi
done

echo "Module '$MODULE' created at $MODULE_DIR"
echo ""
echo "Directories created:"
find "$MODULE_DIR" -type d | sort | sed "s|$SRC_ROOT/||"
