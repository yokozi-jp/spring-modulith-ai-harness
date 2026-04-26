#!/usr/bin/env bash
# 共有関数: ディレクトリ構成定義と package-info.java 生成
# create-module.sh / create-class.sh から source される

BASE_PKG="com.example.demo"
SRC_ROOT="src/main/java/com/example/demo"

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

# package-info.java の内容を生成する
generate_package_info() {
  local rel_path="$1"
  local annotation="$2"
  local import_stmt="$3"
  local pkg="$4"

  if [ -z "$annotation" ]; then
    cat << EOF
@NullMarked
package $pkg;

import org.jspecify.annotations.NullMarked;
EOF
  elif [ "$rel_path" = "event" ]; then
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

# 指定ディレクトリの package-info.java を作成する（既存ならスキップ）
# 途中の親ディレクトリも再帰的に作成する
# 引数: $1=モジュール名, $2=ディレクトリ相対パス（例: "domain/model/aggregate"）
ensure_package_info() {
  local module="$1"
  local target_rel="$2"
  local module_dir="$SRC_ROOT/$module"

  # 途中のディレクトリも含めて作成が必要なパスを収集
  # 例: "domain/model/aggregate" → "domain", "domain/model", "domain/model/aggregate"
  local parts=""
  local IFS_BAK="$IFS"
  IFS='/'
  for part in $target_rel; do
    if [ -z "$parts" ]; then
      parts="$part"
    else
      parts="$parts/$part"
    fi
    _ensure_single_package_info "$module" "$parts" "$module_dir"
  done
  IFS="$IFS_BAK"
}

_ensure_single_package_info() {
  local module="$1"
  local rel="$2"
  local module_dir="$3"
  local dir="$module_dir/$rel"
  local pkg_info="$dir/package-info.java"

  # 既にファイルがあればスキップ
  if [ -f "$pkg_info" ]; then
    return
  fi

  # DIRS 配列からアノテーション情報を検索
  local found=false
  for entry in "${DIRS[@]}"; do
    local entry_rel entry_ann entry_imp
    IFS='|' read -r entry_rel entry_ann entry_imp <<< "$entry"
    if [ "$entry_rel" = "$rel" ]; then
      local sub_pkg
      sub_pkg=$(echo "$rel" | tr '/' '.')
      local pkg="$BASE_PKG.$module.$sub_pkg"
      mkdir -p "$dir"
      generate_package_info "$rel" "$entry_ann" "$entry_imp" "$pkg" > "$pkg_info"
      echo "[CREATE] $pkg_info"
      found=true
      break
    fi
  done

  if [ "$found" = false ]; then
    echo "Warning: No DIRS entry for '$rel', skipping package-info.java" >&2
  fi
}
