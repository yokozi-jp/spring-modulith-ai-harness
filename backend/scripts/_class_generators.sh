#!/usr/bin/env bash
# クラス生成関数群（scaffold から source される）
# 前提: MODULE, MODULE_DIR, NAME, AGGREGATE, DRY_RUN が設定済み
#       module-common.sh が source 済み（BASE_PKG, SRC_ROOT, generate_package_info, ensure_package_info）

# === ファイル書き出し関数 ===
write_file() {
  local file_path="$1"
  local dir_rel="$2"
  local content="$3"
  if [ -f "$file_path" ]; then
    echo "[SKIP] $file_path (already exists)"
    return 1
  fi
  if [ "$DRY_RUN" = true ]; then
    echo "[CREATE FILE] $file_path"
    return 0
  fi
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

# === 生成関数 ===

gen_event() {
  local pkg; pkg=$(pkg_for "event")
  local cls="${NAME}Event"
  write_file "$MODULE_DIR/event/${cls}.java" "event" "\
package $pkg;

import org.jmolecules.event.annotation.DomainEvent;

/** ${NAME} ドメインイベント。 */
@DomainEvent
public record ${cls}() {}"
}

gen_exception() {
  local pkg; pkg=$(pkg_for "exception")
  local cls="${NAME}Exception"
  write_file "$MODULE_DIR/exception/${cls}.java" "exception" "\
package $pkg;

/** ${NAME} 例外。 */
public class ${cls} extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /** メッセージを指定して例外を生成する。 */
  public ${cls}(final String message) {
    super(message);
  }
}"
  gen_exceptionhandler_for_exception "$NAME"
}

infer_http_status() {
  local name="$1"
  case "$name" in
    *NotFound)      echo "404|NOT_FOUND|Not Found" ;;
    *AlreadyExists) echo "409|CONFLICT|Conflict" ;;
    *Conflict)      echo "409|CONFLICT|Conflict" ;;
    *)              echo "400|BAD_REQUEST|Bad Request" ;;
  esac
}

gen_exceptionhandler_for_exception() {
  local exc_name="$1"
  local exc_cls="${exc_name}Exception"
  local exc_pkg; exc_pkg=$(pkg_for "exception")
  local ctrl_pkg; ctrl_pkg=$(pkg_for "presentation/controller")
  local module_cap; module_cap="$(echo "${MODULE:0:1}" | tr '[:lower:]' '[:upper:]')${MODULE:1}"
  local handler_cls="${module_cap}ExceptionHandler"
  local handler_file="$MODULE_DIR/presentation/controller/${handler_cls}.java"

  if [ -f "$handler_file" ]; then
    echo "[SKIP] $handler_file (already exists — add @ExceptionHandler for ${exc_cls} manually)"
    return
  fi

  local status_info; status_info=$(infer_http_status "$exc_name")
  local status_code status_enum status_title
  IFS='|' read -r status_code status_enum status_title <<< "$status_info"
  local method_name="handle${exc_name}"

  write_file "$handler_file" "presentation/controller" "\
package $ctrl_pkg;

import ${exc_pkg}.${exc_cls};
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** ${module_cap} モジュールの例外ハンドラ。 */
@Slf4j
@RestControllerAdvice(basePackages = \"$BASE_PKG.$MODULE\")
public class ${handler_cls} {

  /** ${exc_cls} を処理する。 */
  @ExceptionHandler(${exc_cls}.class)
  /* default */ ProblemDetail ${method_name}(final ${exc_cls} ex) {
    log.warn(\"${exc_cls}: {}\", ex.getMessage());
    final ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.${status_enum});
    problem.setTitle(\"${status_title}\");
    problem.setDetail(ex.getMessage());
    problem.setType(URI.create(\"about:blank\"));
    return problem;
  }
}"
}

gen_identifier_for() {
  local target_name="$1"
  local pkg; pkg=$(pkg_for "domain/model/valueobject/identifier")
  local cls="${target_name}Id"
  write_file "$MODULE_DIR/domain/model/valueobject/identifier/${cls}.java" "domain/model/valueobject/identifier" "\
package $pkg;

import org.jmolecules.ddd.types.Identifier;

/** ${target_name} の識別子。 */
public record ${cls}(String value) implements Identifier {}"
}

gen_identifier() { gen_identifier_for "$NAME"; }

gen_valueobject() {
  local pkg; pkg=$(pkg_for "domain/model/valueobject")
  write_file "$MODULE_DIR/domain/model/valueobject/${NAME}.java" "domain/model/valueobject" "\
package $pkg;

import org.jmolecules.ddd.types.ValueObject;

/** ${NAME} 値オブジェクト。 */
public record ${NAME}() implements ValueObject {}"
}

gen_aggregate() {
  local pkg; pkg=$(pkg_for "domain/model/aggregate")
  local id_cls="${NAME}Id"
  local id_pkg; id_pkg=$(pkg_for "domain/model/valueobject/identifier")
  write_file "$MODULE_DIR/domain/model/aggregate/${NAME}.java" "domain/model/aggregate" "\
package $pkg;

import ${id_pkg}.${id_cls};
import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jmolecules.ddd.types.AggregateRoot;
import org.springframework.data.domain.AbstractAggregateRoot;

/** ${NAME} 集約ルート。 */
@Getter
@EqualsAndHashCode(of = \"id\", callSuper = false)
@ToString
public class ${NAME} extends AbstractAggregateRoot<${NAME}> implements AggregateRoot<${NAME}, ${id_cls}> {

  /** 識別子。 */
  private final ${id_cls} id;

  /** 新規作成用コンストラクタ（Factory から呼び出す）。 */
  /* default */ ${NAME}(final ${id_cls} id) {
    super();
    Objects.requireNonNull(id, \"id must not be null\");
    this.id = id;
  }

  /** 永続化データから集約を再構築する。 */
  public static ${NAME} reconstitute(final ${id_cls} id) {
    return new ${NAME}(id);
  }

  @Override
  public ${id_cls} getId() {
    return id;
  }
}"
  gen_identifier_for "$NAME"
  gen_factory_for "$NAME" ""
  gen_repository
}

gen_entity() {
  if [ -z "$AGGREGATE" ]; then
    echo "Error: --aggregate is required for entity layer" >&2
    exit 1
  fi
  local pkg; pkg=$(pkg_for "domain/model/entity")
  local id_cls="${NAME}Id"
  local id_pkg; id_pkg=$(pkg_for "domain/model/valueobject/identifier")
  local agg_pkg; agg_pkg=$(pkg_for "domain/model/aggregate")
  write_file "$MODULE_DIR/domain/model/entity/${NAME}.java" "domain/model/entity" "\
package $pkg;

import ${agg_pkg}.${AGGREGATE};
import ${id_pkg}.${id_cls};
import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jmolecules.ddd.types.Entity;

/** ${NAME} エンティティ。 */
@Getter
@EqualsAndHashCode(of = \"id\")
@ToString
public class ${NAME} implements Entity<${AGGREGATE}, ${id_cls}> {

  /** 識別子。 */
  private final ${id_cls} id;

  /** コンストラクタ。 */
  /* default */ ${NAME}(final ${id_cls} id) {
    Objects.requireNonNull(id, \"id must not be null\");
    this.id = id;
  }

  /** 永続化データからエンティティを再構築する。 */
  public static ${NAME} reconstitute(final ${id_cls} id) {
    return new ${NAME}(id);
  }

  @Override
  public ${id_cls} getId() {
    return id;
  }
}"
  gen_identifier_for "$NAME"
  gen_factory_for "$NAME" "$AGGREGATE"
}

gen_factory_for() {
  local target="$1" agg="$2"
  local pkg; pkg=$(pkg_for "domain/service")
  local id_cls="${target}Id"
  local id_pkg; id_pkg=$(pkg_for "domain/model/valueobject/identifier")

  if [ -z "$agg" ]; then
    local target_pkg; target_pkg=$(pkg_for "domain/model/aggregate")
    local repo_pkg; repo_pkg=$(pkg_for "domain/repository")
    write_file "$MODULE_DIR/domain/service/${target}Factory.java" "domain/service" "\
package $pkg;

import ${id_pkg}.${id_cls};
import ${repo_pkg}.${target}Repository;
import ${target_pkg}.${target};
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.jmolecules.ddd.annotation.Factory;

/** ${target} ファクトリ。 */
@RequiredArgsConstructor
@Factory
public class ${target}Factory {

  /** リポジトリ。 */
  private final ${target}Repository repository;

  /** 時計。 */
  private final Clock clock;

  /** 新規生成。 */
  public ${target} create() {
    return ${target}.reconstitute(repository.generateId());
  }
}"
  else
    local target_pkg; target_pkg=$(pkg_for "domain/model/entity")
    local gen_pkg; gen_pkg=$(pkg_for "domain/repository")
    local gen_iface="${target}IdGenerator"
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
    return ${target}.reconstitute(idGenerator.generate());
  }
}"
    gen_id_generator_for "$target"
  fi
}

gen_factory() { gen_factory_for "$NAME" "${AGGREGATE:-}"; }

gen_id_generator_for() {
  local target="$1"
  local id_cls="${target}Id"
  local repo_pkg; repo_pkg=$(pkg_for "domain/repository")
  local id_pkg; id_pkg=$(pkg_for "domain/model/valueobject/identifier")
  local infra_pkg; infra_pkg=$(pkg_for "infrastructure/db/repository")

  write_file "$MODULE_DIR/domain/repository/${target}IdGenerator.java" "domain/repository" "\
package $repo_pkg;

import ${id_pkg}.${id_cls};

/** ${target} の ID ジェネレータ。 */
@SuppressWarnings(\"PMD.ImplicitFunctionalInterface\")
public interface ${target}IdGenerator {

  /** ID を生成する。 */
  ${id_cls} generate();
}"

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

gen_repository() {
  local agg="${AGGREGATE:-$NAME}"
  local pkg; pkg=$(pkg_for "domain/repository")
  local agg_pkg; agg_pkg=$(pkg_for "domain/model/aggregate")
  local id_pkg; id_pkg=$(pkg_for "domain/model/valueobject/identifier")
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
  gen_repositoryimpl_for "$agg"
}

gen_repositoryimpl_for() {
  local agg="$1"
  local pkg; pkg=$(pkg_for "infrastructure/db/repository")
  local repo_pkg; repo_pkg=$(pkg_for "domain/repository")
  local id_pkg; id_pkg=$(pkg_for "domain/model/valueobject/identifier")
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

  @Override
  public ${agg}Id generateId() {
    return new ${agg}Id(UUID.randomUUID().toString());
  }
}"
}

gen_repositoryimpl() { gen_repositoryimpl_for "${AGGREGATE:-$NAME}"; }

gen_domainservice() {
  local pkg; pkg=$(pkg_for "domain/service")
  write_file "$MODULE_DIR/domain/service/${NAME}DomainService.java" "domain/service" "\
package $pkg;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jmolecules.ddd.annotation.Service;

/** ${NAME} ドメインサービス。 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ${NAME}DomainService {}"
}

gen_command() {
  local pkg; pkg=$(pkg_for "application/command/command")
  write_file "$MODULE_DIR/application/command/command/${NAME}Command.java" "application/command/command" "\
package $pkg;

import org.jmolecules.architecture.cqrs.Command;

/** ${NAME} コマンド。 */
@Command
public record ${NAME}Command() {}"
}

gen_commandresult() {
  local pkg; pkg=$(pkg_for "application/command/dto")
  write_file "$MODULE_DIR/application/command/dto/${NAME}Dto.java" "application/command/dto" "\
package $pkg;

import com.example.demo.annotation.CommandResult;

/** ${NAME} コマンド実行結果。 */
@CommandResult
public record ${NAME}Dto() {}"
}

gen_param() {
  local pkg; pkg=$(pkg_for "application/query/param")
  write_file "$MODULE_DIR/application/query/param/${NAME}Param.java" "application/query/param" "\
package $pkg;

import com.example.demo.annotation.QueryParam;

/** ${NAME} クエリパラメータ。 */
@QueryParam
public record ${NAME}Param() {}"
}

gen_commandhandler() {
  local pkg; pkg=$(pkg_for "application/command/handler")
  write_file "$MODULE_DIR/application/command/handler/${NAME}CommandHandler.java" "application/command/handler" "\
package $pkg;

import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.cqrs.CommandHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** ${NAME} コマンドハンドラ。 */
@RequiredArgsConstructor
@Component
public class ${NAME}CommandHandler {

  /** コマンドを処理する。 */
  @Transactional
  @CommandHandler
  public void handle() {
    // TODO: 引数にコマンド型を追加すること。
  }
}"
}

gen_eventlistener() {
  local pkg; pkg=$(pkg_for "application/command/handler")
  write_file "$MODULE_DIR/application/command/handler/${NAME}EventListener.java" "application/command/handler" "\
package $pkg;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/** ${NAME} イベントリスナー。 */
@Slf4j
@RequiredArgsConstructor
@Component
public class ${NAME}EventListener {

  /** イベントを処理する。 */
  @ApplicationModuleListener
  /* default */ void handle() {
    // TODO: 引数にイベント型を追加すること。
  }
}"
}

gen_query() {
  local pkg; pkg=$(pkg_for "application/query/dto")
  write_file "$MODULE_DIR/application/query/dto/${NAME}Query.java" "application/query/dto" "\
package $pkg;

import org.jmolecules.architecture.cqrs.QueryModel;

/** ${NAME} クエリモデル。 */
@QueryModel
public record ${NAME}Query() {}"
}

gen_queryservice() {
  local pkg; pkg=$(pkg_for "application/query/service")
  write_file "$MODULE_DIR/application/query/service/${NAME}QueryService.java" "application/query/service" "\
package $pkg;

/** ${NAME} クエリサービス。 */
public interface ${NAME}QueryService {}"
  gen_queryimpl_for "$NAME"
}

gen_queryimpl_for() {
  local target="$1"
  local pkg; pkg=$(pkg_for "infrastructure/db/query")
  local svc_pkg; svc_pkg=$(pkg_for "application/query/service")
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
public class ${target}QueryServiceImpl implements ${target}QueryService {}"
}

gen_queryimpl() { gen_queryimpl_for "$NAME"; }

gen_controller() {
  local pkg; pkg=$(pkg_for "presentation/controller")
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
public class ${NAME}Controller {}"
}

gen_exceptionhandler() {
  local pkg; pkg=$(pkg_for "presentation/controller")
  write_file "$MODULE_DIR/presentation/controller/${NAME}ExceptionHandler.java" "presentation/controller" "\
package $pkg;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** ${NAME} モジュールの例外ハンドラ。 */
@Slf4j
@RestControllerAdvice(basePackages = \"$BASE_PKG.$MODULE\")
public class ${NAME}ExceptionHandler {}"
}

gen_request() {
  local pkg; pkg=$(pkg_for "presentation/request")
  write_file "$MODULE_DIR/presentation/request/${NAME}Request.java" "presentation/request" "\
package $pkg;

/** ${NAME} リクエスト。 */
public record ${NAME}Request() {}"
}

gen_response() {
  local pkg; pkg=$(pkg_for "presentation/response")
  write_file "$MODULE_DIR/presentation/response/${NAME}Response.java" "presentation/response" "\
package $pkg;

/** ${NAME} レスポンス。 */
public record ${NAME}Response() {}"
}

gen_api() {
  # aggregate が存在するか確認
  local agg_file="$MODULE_DIR/domain/model/aggregate/${NAME}.java"
  if [ ! -f "$agg_file" ]; then
    echo "Error: Aggregate '${NAME}' not found at $agg_file" >&2
    echo "Run 'scaffold class $MODULE aggregate $NAME' first." >&2
    exit 1
  fi

  local module_cap; module_cap="$(echo "${MODULE:0:1}" | tr '[:lower:]' '[:upper:]')${MODULE:1}"
  local id_cls="${NAME}Id"
  local id_pkg; id_pkg=$(pkg_for "domain/model/valueobject/identifier")

  # --- presentation/request ---
  local req_pkg; req_pkg=$(pkg_for "presentation/request")
  write_file "$MODULE_DIR/presentation/request/Create${NAME}Request.java" "presentation/request" "\
package $req_pkg;

// import jakarta.validation.constraints.NotBlank;

/** ${NAME} 作成リクエスト。 */
public record Create${NAME}Request(
    // @NotBlank(message = \"FIXME: バリデーションメッセージを設定\")
    // String name // FIXME: フィールドを定義する
) {}"

  # --- presentation/response (Summary + Detail) ---
  local res_pkg; res_pkg=$(pkg_for "presentation/response")
  local summary_dto_pkg; summary_dto_pkg=$(pkg_for "application/query/dto")
  local detail_dto_pkg; detail_dto_pkg=$(pkg_for "application/query/dto")

  write_file "$MODULE_DIR/presentation/response/${NAME}SummaryResponse.java" "presentation/response" "\
package $res_pkg;

import ${summary_dto_pkg}.${NAME}SummaryDto;

/** ${NAME} 一覧レスポンス。 */
public record ${NAME}SummaryResponse(String id) {

  /** DTO から変換する。 */
  public static ${NAME}SummaryResponse from(final ${NAME}SummaryDto dto) {
    return new ${NAME}SummaryResponse(dto.id());
  }
}"

  write_file "$MODULE_DIR/presentation/response/${NAME}DetailResponse.java" "presentation/response" "\
package $res_pkg;

import ${detail_dto_pkg}.${NAME}DetailDto;

/** ${NAME} 詳細レスポンス。 */
public record ${NAME}DetailResponse(String id) {

  /** DTO から変換する。 */
  public static ${NAME}DetailResponse from(final ${NAME}DetailDto dto) {
    return new ${NAME}DetailResponse(dto.id());
  }
}"

  # --- application/command/command ---
  local cmd_pkg; cmd_pkg=$(pkg_for "application/command/command")
  write_file "$MODULE_DIR/application/command/command/Create${NAME}Command.java" "application/command/command" "\
package $cmd_pkg;

import org.jmolecules.architecture.cqrs.Command;

/** ${NAME} 作成コマンド。 */
@Command
public record Create${NAME}Command(
    // FIXME: フィールドを定義する
) {}"

  # --- application/command/dto (CommandResult) ---
  local cmd_dto_pkg; cmd_dto_pkg=$(pkg_for "application/command/dto")
  write_file "$MODULE_DIR/application/command/dto/Created${NAME}Dto.java" "application/command/dto" "\
package $cmd_dto_pkg;

import com.example.demo.annotation.CommandResult;

/** ${NAME} 作成結果。 */
@CommandResult
public record Created${NAME}Dto(String id) {}"

  # --- application/command/handler ---
  local handler_pkg; handler_pkg=$(pkg_for "application/command/handler")
  local factory_pkg; factory_pkg=$(pkg_for "domain/service")
  local agg_pkg; agg_pkg=$(pkg_for "domain/model/aggregate")
  local repo_pkg; repo_pkg=$(pkg_for "domain/repository")
  write_file "$MODULE_DIR/application/command/handler/${NAME}CommandHandler.java" "application/command/handler" "\
package $handler_pkg;

import ${cmd_dto_pkg}.Created${NAME}Dto;
import ${cmd_pkg}.Create${NAME}Command;
import ${factory_pkg}.${NAME}Factory;
import ${agg_pkg}.${NAME};
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.cqrs.CommandHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** ${NAME} コマンドハンドラ。 */
@RequiredArgsConstructor
@Component
public class ${NAME}CommandHandler {

  /** ファクトリ。 */
  private final ${NAME}Factory factory;

  /** 作成コマンドを処理する。 */
  @SuppressWarnings(\"PMD.LawOfDemeter\")
  @Transactional
  @CommandHandler
  public Created${NAME}Dto handle(final Create${NAME}Command command) {
    final ${NAME} aggregate = factory.create();
    final String id = aggregate.getId().value();
    return new Created${NAME}Dto(id);
  }
}"

  # --- application/query/param ---
  local param_pkg; param_pkg=$(pkg_for "application/query/param")
  write_file "$MODULE_DIR/application/query/param/${NAME}ListParam.java" "application/query/param" "\
package $param_pkg;

import com.example.demo.annotation.QueryParam;

/** ${NAME} 一覧検索パラメータ。 */
@QueryParam
public record ${NAME}ListParam(
    // FIXME: フィルタ条件を定義する
) {}"

  # --- application/query/dto (QueryModel) ---
  local qry_dto_pkg; qry_dto_pkg=$(pkg_for "application/query/dto")
  write_file "$MODULE_DIR/application/query/dto/${NAME}SummaryDto.java" "application/query/dto" "\
package $qry_dto_pkg;

import org.jmolecules.architecture.cqrs.QueryModel;

/** ${NAME} 一覧用クエリモデル。 */
@QueryModel
public record ${NAME}SummaryDto(String id) {}"

  write_file "$MODULE_DIR/application/query/dto/${NAME}DetailDto.java" "application/query/dto" "\
package $qry_dto_pkg;

import org.jmolecules.architecture.cqrs.QueryModel;

/** ${NAME} 詳細クエリモデル。 */
@QueryModel
public record ${NAME}DetailDto(String id) {}"

  # --- application/query/service ---
  local qry_svc_pkg; qry_svc_pkg=$(pkg_for "application/query/service")
  write_file "$MODULE_DIR/application/query/service/${NAME}QueryService.java" "application/query/service" "\
package $qry_svc_pkg;

import ${param_pkg}.${NAME}ListParam;
import ${qry_dto_pkg}.${NAME}DetailDto;
import ${qry_dto_pkg}.${NAME}SummaryDto;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** ${NAME} クエリサービス。 */
public interface ${NAME}QueryService {

  /** 一覧取得。 */
  Page<${NAME}SummaryDto> findAll(${NAME}ListParam param, Pageable pageable);

  /** ID で取得。 */
  Optional<${NAME}DetailDto> findById(String id);
}"

  # --- infrastructure/db/query ---
  local infra_qry_pkg; infra_qry_pkg=$(pkg_for "infrastructure/db/query")
  write_file "$MODULE_DIR/infrastructure/db/query/${NAME}QueryServiceImpl.java" "infrastructure/db/query" "\
package $infra_qry_pkg;

import ${param_pkg}.${NAME}ListParam;
import ${qry_dto_pkg}.${NAME}DetailDto;
import ${qry_dto_pkg}.${NAME}SummaryDto;
import ${qry_svc_pkg}.${NAME}QueryService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/** ${NAME} クエリサービス実装。 */
@Slf4j
@RequiredArgsConstructor
@Component
public class ${NAME}QueryServiceImpl implements ${NAME}QueryService {

  @Override
  public Page<${NAME}SummaryDto> findAll(final ${NAME}ListParam param, final Pageable pageable) {
    // FIXME: jOOQ でクエリを実装する
    return new PageImpl<>(java.util.List.of(), pageable, 0);
  }

  @Override
  public Optional<${NAME}DetailDto> findById(final String id) {
    // FIXME: jOOQ でクエリを実装する
    return Optional.empty();
  }
}"

  # --- exception ---
  local exc_pkg; exc_pkg=$(pkg_for "exception")
  local exc_cls="${NAME}NotFoundException"
  write_file "$MODULE_DIR/exception/${exc_cls}.java" "exception" "\
package $exc_pkg;

/** ${NAME} が見つからない場合の例外。 */
public class ${exc_cls} extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /** ID を指定して例外を生成する。 */
  public ${exc_cls}(final String id) {
    super(\"${NAME} not found: \" + id);
  }
}"

  # --- presentation/controller (ExceptionHandler) ---
  local ctrl_pkg; ctrl_pkg=$(pkg_for "presentation/controller")
  local handler_cls="${module_cap}ExceptionHandler"
  write_file "$MODULE_DIR/presentation/controller/${handler_cls}.java" "presentation/controller" "\
package $ctrl_pkg;

import ${exc_pkg}.${exc_cls};
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** ${module_cap} モジュールの例外ハンドラ。 */
@Slf4j
@RestControllerAdvice(basePackages = \"$BASE_PKG.$MODULE\")
public class ${handler_cls} {

  /** ${exc_cls} を処理する。 */
  @ExceptionHandler(${exc_cls}.class)
  /* default */ ProblemDetail handleNotFound(final ${exc_cls} ex) {
    log.warn(\"${exc_cls}: {}\", ex.getMessage());
    final ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
    problem.setTitle(\"Not Found\");
    problem.setDetail(ex.getMessage());
    problem.setType(URI.create(\"about:blank\"));
    return problem;
  }
}"

  # --- presentation/controller (main controller) ---
  local path="/${MODULE}s"
  write_file "$MODULE_DIR/presentation/controller/${NAME}Controller.java" "presentation/controller" "\
package $ctrl_pkg;

import ${cmd_dto_pkg}.Created${NAME}Dto;
import ${cmd_pkg}.Create${NAME}Command;
import ${exc_pkg}.${exc_cls};
import ${handler_pkg}.${NAME}CommandHandler;
import ${param_pkg}.${NAME}ListParam;
import ${qry_svc_pkg}.${NAME}QueryService;
import ${req_pkg}.Create${NAME}Request;
import ${res_pkg}.${NAME}DetailResponse;
import ${res_pkg}.${NAME}SummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** ${NAME} コントローラ。 */
@Tag(name = \"${NAME}\", description = \"FIXME: ${NAME} API の説明を記述する\")
@RestController
@RequestMapping(\"${path}\")
@Slf4j
@RequiredArgsConstructor
public class ${NAME}Controller {

  /** コマンドハンドラ。 */
  private final ${NAME}CommandHandler commandHandler;

  /** クエリサービス。 */
  private final ${NAME}QueryService queryService;

  /** ${NAME} を作成する。 */
  @Operation(summary = \"FIXME: ${NAME} 作成の説明を記述する\")
  @ApiResponse(responseCode = \"201\", description = \"作成成功\")
  @PostMapping
  public ResponseEntity<Void> create(@RequestBody @Valid final Create${NAME}Request request) {
    final Created${NAME}Dto result = commandHandler.handle(
        new Create${NAME}Command());
    final URI location = ServletUriComponentsBuilder.fromCurrentRequest()
        .path(\"/{id}\").buildAndExpand(result.id()).toUri();
    return ResponseEntity.created(location).build();
  }

  /** ${NAME} 一覧を取得する。 */
  @Operation(summary = \"FIXME: ${NAME} 一覧取得の説明を記述する\")
  @ApiResponse(responseCode = \"200\", description = \"取得成功\")
  @GetMapping
  public Page<${NAME}SummaryResponse> list(
      final ${NAME}ListParam param, final Pageable pageable) {
    return queryService.findAll(param, pageable).map(${NAME}SummaryResponse::from);
  }

  /** ${NAME} 詳細を取得する。 */
  @Operation(summary = \"FIXME: ${NAME} 詳細取得の説明を記述する\")
  @ApiResponse(responseCode = \"200\", description = \"取得成功\")
  @ApiResponse(responseCode = \"404\", description = \"見つからない\")
  @GetMapping(\"/{id}\")
  public ${NAME}DetailResponse findById(@PathVariable final String id) {
    return queryService.findById(id)
        .map(${NAME}DetailResponse::from)
        .orElseThrow(() -> new ${exc_cls}(id));
  }
}"
}
