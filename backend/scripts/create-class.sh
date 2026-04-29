#!/usr/bin/env bash
# クラス/record/interface 雛形生成スクリプト
# 使い方: cd backend && ./scripts/create-class.sh <module> <layer> <name> [--aggregate <Aggregate>]
#
# layer 一覧:
#   event, aggregate, entity, identifier, valueobject, repository, domainservice,
#   command, commandhandler, eventlistener, query, queryservice,
#   controller, request, response, repositoryimpl, queryimpl
#
# 例:
#   ./scripts/create-class.sh order aggregate Order
#   ./scripts/create-class.sh order entity OrderItem --aggregate Order
#   ./scripts/create-class.sh order event OrderCreated
#   ./scripts/create-class.sh order repository Order

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source=module-common.sh
source "$SCRIPT_DIR/module-common.sh"

# === オプション解析 ===
AGGREGATE=""
POSITIONAL=()

while [[ $# -gt 0 ]]; do
  case "$1" in
    --aggregate) AGGREGATE="$2"; shift 2 ;;
    -*) echo "Error: Unknown option '$1'" >&2; exit 1 ;;
    *) POSITIONAL+=("$1"); shift ;;
  esac
done

if [ "${#POSITIONAL[@]}" -ne 3 ]; then
  echo "Usage: cd backend && $0 <module> <layer> <name> [--aggregate <Aggregate>]" >&2
  echo "Layers: event aggregate entity identifier valueobject repository domainservice factory" >&2
  echo "        command commandhandler eventlistener query queryservice" >&2
  echo "        controller request response repositoryimpl queryimpl" >&2
  exit 1
fi

MODULE="${POSITIONAL[0]}"
LAYER="${POSITIONAL[1]}"
NAME="${POSITIONAL[2]}"

# === バリデーション ===
if [[ ! "$MODULE" =~ ^[a-z][a-z0-9]*$ ]]; then
  echo "Error: Module name must be lowercase alphanumeric (e.g., 'order')" >&2
  exit 1
fi

if [[ ! "$NAME" =~ ^[A-Z][a-zA-Z0-9]*$ ]]; then
  echo "Error: Name must be PascalCase (e.g., 'Order', 'OrderItem')" >&2
  exit 1
fi

MODULE_DIR="$SRC_ROOT/$MODULE"

if [ ! -d "$MODULE_DIR" ]; then
  echo "Error: Module '$MODULE' does not exist. Run create-module.sh first." >&2
  exit 1
fi

# === ファイル書き出し関数 ===
# 引数: $1=ファイルパス, $2=ディレクトリ相対パス（package-info 自動作成用）, $3=内容
write_file() {
  local file_path="$1"
  local dir_rel="$2"
  local content="$3"
  if [ -f "$file_path" ]; then
    echo "[SKIP] $file_path (already exists)"
    return 1
  fi
  # ディレクトリと package-info.java を自動作成
  ensure_package_info "$MODULE" "$dir_rel"
  mkdir -p "$(dirname "$file_path")"
  echo "$content" > "$file_path"
  echo "[CREATE] $file_path"
  return 0
}

# === パッケージ名を算出 ===
pkg_for() {
  local rel="$1"
  local sub
  sub=$(echo "$rel" | tr '/' '.')
  echo "$BASE_PKG.$MODULE.$sub"
}

# === 各レイヤーの生成関数 ===

gen_event() {
  local pkg
  pkg=$(pkg_for "event")
  local cls="${NAME}Event"
  write_file "$MODULE_DIR/event/${cls}.java" "event" "\
package $pkg;

import org.jmolecules.event.annotation.DomainEvent;

/** ${NAME} ドメインイベント。 */
@DomainEvent
public record ${cls}() {}"
}

gen_aggregate() {
  local pkg
  pkg=$(pkg_for "domain/model/aggregate")
  local id_cls="${NAME}Id"
  local id_pkg
  id_pkg=$(pkg_for "domain/model/valueobject/identifier")
  write_file "$MODULE_DIR/domain/model/aggregate/${NAME}.java" "domain/model/aggregate" "\
package $pkg;

import ${id_pkg}.${id_cls};
import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.jmolecules.ddd.types.AggregateRoot;

/** ${NAME} 集約ルート。 */
@Slf4j
@Getter
@EqualsAndHashCode
@ToString
public class ${NAME} implements AggregateRoot<${NAME}, ${id_cls}> {

  /** 識別子。 */
  private final ${id_cls} id;

  /** コンストラクタ。 */
  public ${NAME}(final ${id_cls} id) {
    Objects.requireNonNull(id, \"id must not be null\");
    this.id = id;
  }

  @Override
  public ${id_cls} getId() {
    return id;
  }
}"
  # 自動連鎖: Identifier
  gen_identifier_for "$NAME"
  # 自動連鎖: Factory
  gen_factory_for "$NAME" ""
}

gen_entity() {
  if [ -z "$AGGREGATE" ]; then
    echo "Error: --aggregate is required for entity layer" >&2
    exit 1
  fi
  local pkg
  pkg=$(pkg_for "domain/model/entity")
  local id_cls="${NAME}Id"
  local id_pkg
  id_pkg=$(pkg_for "domain/model/valueobject/identifier")
  local agg_pkg
  agg_pkg=$(pkg_for "domain/model/aggregate")
  write_file "$MODULE_DIR/domain/model/entity/${NAME}.java" "domain/model/entity" "\
package $pkg;

import ${agg_pkg}.${AGGREGATE};
import ${id_pkg}.${id_cls};
import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.jmolecules.ddd.types.Entity;

/** ${NAME} エンティティ。 */
@Slf4j
@Getter
@EqualsAndHashCode
@ToString
public class ${NAME} implements Entity<${AGGREGATE}, ${id_cls}> {

  /** 識別子。 */
  private final ${id_cls} id;

  /** コンストラクタ。 */
  public ${NAME}(final ${id_cls} id) {
    Objects.requireNonNull(id, \"id must not be null\");
    this.id = id;
  }

  @Override
  public ${id_cls} getId() {
    return id;
  }
}"
  # 自動連鎖: Identifier
  gen_identifier_for "$NAME"
  # 自動連鎖: Factory
  gen_factory_for "$NAME" "$AGGREGATE"
}

gen_factory_for() {
  local target="$1"
  local agg="$2"
  local pkg
  pkg=$(pkg_for "domain/service")
  local id_cls="${target}Id"
  local id_pkg
  id_pkg=$(pkg_for "domain/model/valueobject/identifier")
  local repo_pkg
  repo_pkg=$(pkg_for "domain/repository")
  # aggregate の場合: 自身の Repository を使う。entity の場合: 集約の Repository を使う
  local repo_target="${agg:-$target}"
  if [ -z "$agg" ]; then
    # aggregate 用ファクトリ
    local target_pkg
    target_pkg=$(pkg_for "domain/model/aggregate")
    write_file "$MODULE_DIR/domain/service/${target}Factory.java" "domain/service" "\
package $pkg;

import ${id_pkg}.${id_cls};
import ${repo_pkg}.${target}Repository;
import ${target_pkg}.${target};
import lombok.RequiredArgsConstructor;
import org.jmolecules.ddd.annotation.Factory;

/** ${target} ファクトリ。 */
@RequiredArgsConstructor
@Factory
public class ${target}Factory {

  /** リポジトリ。 */
  private final ${target}Repository repository;

  /** 新規生成。 */
  public ${target} create() {
    return new ${target}(repository.generateId());
  }
}"
  else
    # entity 用ファクトリ
    local target_pkg
    target_pkg=$(pkg_for "domain/model/entity")
    local gen_iface="${target}IdGenerator"
    local gen_pkg
    gen_pkg=$(pkg_for "domain/repository")
    write_file "$MODULE_DIR/domain/service/${target}Factory.java" "domain/service" "\
package $pkg;

import ${gen_pkg}.${gen_iface};
import ${target_pkg}.${target};
import lombok.RequiredArgsConstructor;
import org.jmolecules.ddd.annotation.Factory;

/** ${target} ファクトリ。 */
@RequiredArgsConstructor
@Factory
public class ${target}Factory {

  /** ID ジェネレータ。 */
  private final ${gen_iface} idGenerator;

  /** 新規生成。 */
  public ${target} create() {
    return new ${target}(idGenerator.generate());
  }
}"
    # 自動連鎖: IdGenerator + IdGeneratorImpl
    gen_id_generator_for "$target"
  fi
}

gen_id_generator_for() {
  local target="$1"
  local id_cls="${target}Id"
  local repo_pkg
  repo_pkg=$(pkg_for "domain/repository")
  local id_pkg
  id_pkg=$(pkg_for "domain/model/valueobject/identifier")
  local infra_pkg
  infra_pkg=$(pkg_for "infrastructure/db/repository")
  # interface
  write_file "$MODULE_DIR/domain/repository/${target}IdGenerator.java" "domain/repository" "\
package $repo_pkg;

import ${id_pkg}.${id_cls};

/** ${target} の ID ジェネレータ。 */
@SuppressWarnings(\"PMD.ImplicitFunctionalInterface\")
public interface ${target}IdGenerator {

  /** ID を生成する。 */
  ${id_cls} generate();
}"
  # impl
  write_file "$MODULE_DIR/infrastructure/db/repository/${target}IdGeneratorImpl.java" "infrastructure/db/repository" "\
package $infra_pkg;

import ${id_pkg}.${id_cls};
import ${repo_pkg}.${target}IdGenerator;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** ${target} の ID ジェネレータ実装。 */
@Component
public class ${target}IdGeneratorImpl implements ${target}IdGenerator {

  @Override
  public ${id_cls} generate() {
    return new ${id_cls}(UUID.randomUUID().toString());
  }
}"
}

gen_factory() {
  local agg="${AGGREGATE:-}"
  gen_factory_for "$NAME" "$agg"
}

gen_identifier_for() {
  local target_name="$1"
  local pkg
  pkg=$(pkg_for "domain/model/valueobject/identifier")
  local cls="${target_name}Id"
  write_file "$MODULE_DIR/domain/model/valueobject/identifier/${cls}.java" "domain/model/valueobject/identifier" "\
package $pkg;

import org.jmolecules.ddd.types.Identifier;

/** ${target_name} の識別子。 */
public record ${cls}(String value) implements Identifier {}"
}

gen_identifier() {
  gen_identifier_for "$NAME"
}

gen_valueobject() {
  local pkg
  pkg=$(pkg_for "domain/model/valueobject")
  write_file "$MODULE_DIR/domain/model/valueobject/${NAME}.java" "domain/model/valueobject" "\
package $pkg;

import org.jmolecules.ddd.types.ValueObject;

/** ${NAME} 値オブジェクト。 */
public record ${NAME}() implements ValueObject {}"
}

gen_repository() {
  local agg="${AGGREGATE:-$NAME}"
  local pkg
  pkg=$(pkg_for "domain/repository")
  local agg_pkg
  agg_pkg=$(pkg_for "domain/model/aggregate")
  local id_pkg
  id_pkg=$(pkg_for "domain/model/valueobject/identifier")
  write_file "$MODULE_DIR/domain/repository/${agg}Repository.java" "domain/repository" "\
package $pkg;

import ${agg_pkg}.${agg};
import ${id_pkg}.${agg}Id;
import org.jmolecules.ddd.types.Repository;

/** ${agg} リポジトリ。 */
@SuppressWarnings(\"PMD.ImplicitFunctionalInterface\")
public interface ${agg}Repository extends Repository<${agg}, ${agg}Id> {

  /** ID を生成する。 */
  ${agg}Id generateId();
}"
  # 自動連鎖: RepositoryImpl
  gen_repositoryimpl_for "$agg"
}

gen_repositoryimpl_for() {
  local agg="$1"
  local pkg
  pkg=$(pkg_for "infrastructure/db/repository")
  local repo_pkg
  repo_pkg=$(pkg_for "domain/repository")
  local id_pkg
  id_pkg=$(pkg_for "domain/model/valueobject/identifier")
  write_file "$MODULE_DIR/infrastructure/db/repository/${agg}RepositoryImpl.java" "infrastructure/db/repository" "\
package $pkg;

import ${id_pkg}.${agg}Id;
import ${repo_pkg}.${agg}Repository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jmolecules.ddd.annotation.Repository;

/** ${agg} リポジトリ実装。 */
@Slf4j
@RequiredArgsConstructor
@Repository
public class ${agg}RepositoryImpl implements ${agg}Repository {

  // private final DSLContext dsl; (import org.jooq.DSLContext)

  @Override
  public ${agg}Id generateId() {
    return new ${agg}Id(UUID.randomUUID().toString());
  }
}"
}

gen_repositoryimpl() {
  local agg="${AGGREGATE:-$NAME}"
  gen_repositoryimpl_for "$agg"
}

gen_domainservice() {
  local pkg
  pkg=$(pkg_for "domain/service")
  write_file "$MODULE_DIR/domain/service/${NAME}DomainService.java" "domain/service" "\
package $pkg;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jmolecules.ddd.annotation.Service;

/** ${NAME} ドメインサービス。 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ${NAME}DomainService {

  // DI フィールドをここに追加
}"
}

gen_command() {
  local pkg
  pkg=$(pkg_for "application/command/dto")
  write_file "$MODULE_DIR/application/command/dto/${NAME}Command.java" "application/command/dto" "\
package $pkg;

import org.jmolecules.architecture.cqrs.Command;

/** ${NAME} コマンド。 */
@Command
public record ${NAME}Command() {}"
}

gen_commandhandler() {
  local pkg
  pkg=$(pkg_for "application/command/handler")
  write_file "$MODULE_DIR/application/command/handler/${NAME}CommandHandler.java" "application/command/handler" "\
package $pkg;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jmolecules.architecture.cqrs.CommandHandler;

/** ${NAME} コマンドハンドラ。 */
@Slf4j
@RequiredArgsConstructor
public class ${NAME}CommandHandler {

  /** コマンドを処理する。 */
  @CommandHandler
  /* default */ void handle() {
    // コマンド処理
  }
}"
}

gen_eventlistener() {
  local pkg
  pkg=$(pkg_for "application/command/handler")
  write_file "$MODULE_DIR/application/command/handler/${NAME}EventListener.java" "application/command/handler" "\
package $pkg;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;

/** ${NAME} イベントリスナー。 */
@Slf4j
@RequiredArgsConstructor
public class ${NAME}EventListener {

  /** イベントを処理する。 */
  @ApplicationModuleListener
  /* default */ void handle() {
    // イベント処理
  }
}"
}

gen_query() {
  local pkg
  pkg=$(pkg_for "application/query/dto")
  write_file "$MODULE_DIR/application/query/dto/${NAME}Query.java" "application/query/dto" "\
package $pkg;

import org.jmolecules.architecture.cqrs.QueryModel;

/** ${NAME} クエリモデル。 */
@QueryModel
public record ${NAME}Query() {}"
}

gen_queryservice() {
  local pkg
  pkg=$(pkg_for "application/query/service")
  write_file "$MODULE_DIR/application/query/service/${NAME}QueryService.java" "application/query/service" "\
package $pkg;

/** ${NAME} クエリサービス。 */
public interface ${NAME}QueryService {}"
  # 自動連鎖: QueryServiceImpl
  gen_queryimpl_for "$NAME"
}

gen_queryimpl_for() {
  local target="$1"
  local pkg
  pkg=$(pkg_for "infrastructure/db/query")
  local svc_pkg
  svc_pkg=$(pkg_for "application/query/service")
  write_file "$MODULE_DIR/infrastructure/db/query/${target}QueryServiceImpl.java" "infrastructure/db/query" "\
package $pkg;

import ${svc_pkg}.${target}QueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** ${target} クエリサービス実装。 */
@Slf4j
@RequiredArgsConstructor
@Component
public class ${target}QueryServiceImpl implements ${target}QueryService {

  // private final DSLContext dsl; (import org.jooq.DSLContext)
}"
}

gen_queryimpl() {
  gen_queryimpl_for "$NAME"
}

gen_controller() {
  local pkg
  pkg=$(pkg_for "presentation/controller")
  # endpoint パスをモジュール名ベースで生成
  local path="/${MODULE}s"
  write_file "$MODULE_DIR/presentation/controller/${NAME}Controller.java" "presentation/controller" "\
package $pkg;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** ${NAME} コントローラ。 */
@RestController
@RequestMapping(\"${path}\")
@Slf4j
@RequiredArgsConstructor
public class ${NAME}Controller {

  // DI フィールドをここに追加
}"
}

gen_request() {
  local pkg
  pkg=$(pkg_for "presentation/request")
  write_file "$MODULE_DIR/presentation/request/${NAME}Request.java" "presentation/request" "\
package $pkg;

/** ${NAME} リクエスト。 */
public record ${NAME}Request() {}"
}

gen_response() {
  local pkg
  pkg=$(pkg_for "presentation/response")
  write_file "$MODULE_DIR/presentation/response/${NAME}Response.java" "presentation/response" "\
package $pkg;

/** ${NAME} レスポンス。 */
public record ${NAME}Response() {}"
}

# === メイン: layer に応じて生成関数を呼び出し ===
case "$LAYER" in
  event)          gen_event ;;
  aggregate)      gen_aggregate ;;
  entity)         gen_entity ;;
  identifier)     gen_identifier ;;
  valueobject)    gen_valueobject ;;
  repository)     gen_repository ;;
  repositoryimpl) gen_repositoryimpl ;;
  factory)        gen_factory ;;
  domainservice)  gen_domainservice ;;
  command)        gen_command ;;
  commandhandler) gen_commandhandler ;;
  eventlistener)  gen_eventlistener ;;
  query)          gen_query ;;
  queryservice)   gen_queryservice ;;
  queryimpl)      gen_queryimpl ;;
  controller)     gen_controller ;;
  request)        gen_request ;;
  response)       gen_response ;;
  *)
    echo "Error: Unknown layer '$LAYER'" >&2
    echo "Valid layers: event aggregate entity identifier valueobject repository domainservice factory" >&2
    echo "             command commandhandler eventlistener query queryservice" >&2
    echo "             controller request response repositoryimpl queryimpl" >&2
    exit 1
    ;;
esac
