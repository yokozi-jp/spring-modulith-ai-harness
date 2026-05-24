#!/usr/bin/env bash
# クラス生成関数群（scaffold から source される）
# 前提: MODULE, MODULE_DIR, NAME, AGGREGATE, DRY_RUN が設定済み
#       module-common.sh が source 済み（BASE_PKG, SRC_ROOT, generate_package_info, ensure_package_info, render_template）

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

# === PascalCase を kebab-case 複数形パスに変換 ===
to_kebab_path() {
  local name="$1"
  local kebab
  kebab=$(echo "$name" | sed 's/\([a-z]\)\([A-Z]\)/\1-\2/g' | tr '[:upper:]' '[:lower:]')
  echo "/${kebab}s"
}

# === 生成関数 ===

gen_event() {
  local pkg; pkg=$(pkg_for "event")
  local cls="${NAME}Event"
  local content; content=$(render_template "class/event.java.tmpl" "PKG=$pkg" "NAME=$NAME")
  write_file "$MODULE_DIR/event/${cls}.java" "event" "$content"
}

gen_exception() {
  local pkg; pkg=$(pkg_for "exception")
  local cls="${NAME}Exception"
  local content; content=$(render_template "class/exception.java.tmpl" "PKG=$pkg" "NAME=$NAME")
  write_file "$MODULE_DIR/exception/${cls}.java" "exception" "$content"
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
  local handler_cls="${NAME}ExceptionHandler"
  local handler_file="$MODULE_DIR/presentation/controller/${handler_cls}.java"

  if [ -f "$handler_file" ]; then
    echo "[SKIP] $handler_file (already exists — add @ExceptionHandler for ${exc_cls} manually)"
    return
  fi

  local status_info; status_info=$(infer_http_status "$exc_name")
  local status_code status_enum status_title
  IFS='|' read -r status_code status_enum status_title <<< "$status_info"
  local method_name="handle${exc_name}"

  local content; content=$(render_template "class/exceptionhandler.java.tmpl" \
    "CTRL_PKG=$ctrl_pkg" "EXC_PKG=$exc_pkg" "EXC_CLS=$exc_cls" \
    "NAME=$NAME" "BASE_PKG=$BASE_PKG" "MODULE=$MODULE" \
    "HANDLER_CLS=$handler_cls" "METHOD_NAME=$method_name" \
    "STATUS_ENUM=$status_enum" "STATUS_TITLE=$status_title")
  write_file "$handler_file" "presentation/controller" "$content"
}

gen_identifier_for() {
  local target_name="$1"
  local pkg; pkg=$(pkg_for "domain/model/valueobject/identifier")
  local cls="${target_name}Id"
  local content; content=$(render_template "class/identifier.java.tmpl" "PKG=$pkg" "TARGET_NAME=$target_name")
  write_file "$MODULE_DIR/domain/model/valueobject/identifier/${cls}.java" "domain/model/valueobject/identifier" "$content"
}

gen_identifier() { gen_identifier_for "$NAME"; }

gen_valueobject() {
  local pkg; pkg=$(pkg_for "domain/model/valueobject")
  local content; content=$(render_template "class/valueobject.java.tmpl" "PKG=$pkg" "NAME=$NAME")
  write_file "$MODULE_DIR/domain/model/valueobject/${NAME}.java" "domain/model/valueobject" "$content"
}

gen_aggregate() {
  local pkg; pkg=$(pkg_for "domain/model/aggregate")
  local id_cls="${NAME}Id"
  local id_pkg; id_pkg=$(pkg_for "domain/model/valueobject/identifier")
  local content; content=$(render_template "class/aggregate.java.tmpl" "PKG=$pkg" "NAME=$NAME" "ID_CLS=$id_cls" "ID_PKG=$id_pkg")
  write_file "$MODULE_DIR/domain/model/aggregate/${NAME}.java" "domain/model/aggregate" "$content"
  gen_identifier_for "$NAME"
  gen_factory_for "$NAME" ""
  gen_repository

  # --- テスト連鎖生成 ---
  if [ "$DRY_RUN" != true ]; then
    echo ""
    echo "Generating test skeletons..."
    ./scripts/scaffold.sh test "$MODULE" domain "$NAME" || true
    ./scripts/scaffold.sh test "$MODULE" domain "${NAME}Id" || true
    ./scripts/scaffold.sh test "$MODULE" factory "${NAME}Factory" || true
    ./scripts/scaffold.sh test "$MODULE" integration "${NAME}RepositoryImpl" || true
  fi
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
  local content; content=$(render_template "class/entity.java.tmpl" \
    "PKG=$pkg" "NAME=$NAME" "ID_CLS=$id_cls" "ID_PKG=$id_pkg" \
    "AGG_PKG=$agg_pkg" "AGGREGATE=$AGGREGATE")
  write_file "$MODULE_DIR/domain/model/entity/${NAME}.java" "domain/model/entity" "$content"
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
    local content; content=$(render_template "class/factory-aggregate.java.tmpl" \
      "PKG=$pkg" "ID_CLS=$id_cls" "ID_PKG=$id_pkg" \
      "REPO_PKG=$repo_pkg" "TARGET_PKG=$target_pkg" "TARGET=$target")
    write_file "$MODULE_DIR/domain/service/${target}Factory.java" "domain/service" "$content"
  else
    local target_pkg; target_pkg=$(pkg_for "domain/model/entity")
    local gen_pkg; gen_pkg=$(pkg_for "domain/repository")
    local content; content=$(render_template "class/factory-entity.java.tmpl" \
      "PKG=$pkg" "GEN_PKG=$gen_pkg" "TARGET_PKG=$target_pkg" "TARGET=$target")
    write_file "$MODULE_DIR/domain/service/${target}Factory.java" "domain/service" "$content"
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

  local content; content=$(render_template "class/idgenerator.java.tmpl" \
    "PKG=$repo_pkg" "ID_PKG=$id_pkg" "ID_CLS=$id_cls" "TARGET=$target")
  write_file "$MODULE_DIR/domain/repository/${target}IdGenerator.java" "domain/repository" "$content"

  local content2; content2=$(render_template "class/idgenerator-impl.java.tmpl" \
    "PKG=$infra_pkg" "ID_PKG=$id_pkg" "ID_CLS=$id_cls" "GEN_PKG=$repo_pkg" "TARGET=$target")
  write_file "$MODULE_DIR/infrastructure/db/repository/${target}IdGeneratorImpl.java" "infrastructure/db/repository" "$content2"
}

gen_repository() {
  local agg="${AGGREGATE:-$NAME}"
  local pkg; pkg=$(pkg_for "domain/repository")
  local agg_pkg; agg_pkg=$(pkg_for "domain/model/aggregate")
  local id_pkg; id_pkg=$(pkg_for "domain/model/valueobject/identifier")
  local content; content=$(render_template "class/repository.java.tmpl" \
    "PKG=$pkg" "AGG_PKG=$agg_pkg" "ID_PKG=$id_pkg" "AGG=$agg")
  write_file "$MODULE_DIR/domain/repository/${agg}Repository.java" "domain/repository" "$content"
  gen_repositoryimpl_for "$agg"
}

gen_repositoryimpl_for() {
  local agg="$1"
  local pkg; pkg=$(pkg_for "infrastructure/db/repository")
  local repo_pkg; repo_pkg=$(pkg_for "domain/repository")
  local id_pkg; id_pkg=$(pkg_for "domain/model/valueobject/identifier")
  local content; content=$(render_template "class/repository-impl.java.tmpl" \
    "PKG=$pkg" "ID_PKG=$id_pkg" "REPO_PKG=$repo_pkg" "AGG=$agg")
  write_file "$MODULE_DIR/infrastructure/db/repository/${agg}RepositoryImpl.java" "infrastructure/db/repository" "$content"
}

gen_repositoryimpl() { gen_repositoryimpl_for "${AGGREGATE:-$NAME}"; }

gen_domainservice() {
  local pkg; pkg=$(pkg_for "domain/service")
  local content; content=$(render_template "class/domainservice.java.tmpl" "PKG=$pkg" "NAME=$NAME")
  write_file "$MODULE_DIR/domain/service/${NAME}DomainService.java" "domain/service" "$content"
}

gen_command() {
  local pkg; pkg=$(pkg_for "application/command/command")
  local content; content=$(render_template "class/command.java.tmpl" "PKG=$pkg" "NAME=$NAME")
  write_file "$MODULE_DIR/application/command/command/${NAME}Command.java" "application/command/command" "$content"
}

gen_commandresult() {
  local pkg; pkg=$(pkg_for "application/command/dto")
  local content; content=$(render_template "class/commandresult.java.tmpl" "PKG=$pkg" "NAME=$NAME")
  write_file "$MODULE_DIR/application/command/dto/${NAME}Dto.java" "application/command/dto" "$content"
}

gen_param() {
  local pkg; pkg=$(pkg_for "application/query/param")
  local content; content=$(render_template "class/param.java.tmpl" "PKG=$pkg" "NAME=$NAME")
  write_file "$MODULE_DIR/application/query/param/${NAME}Param.java" "application/query/param" "$content"
}

gen_commandhandler() {
  local pkg; pkg=$(pkg_for "application/command/handler")
  local content; content=$(render_template "class/commandhandler.java.tmpl" "PKG=$pkg" "NAME=$NAME")
  write_file "$MODULE_DIR/application/command/handler/${NAME}CommandHandler.java" "application/command/handler" "$content"
}

gen_eventlistener() {
  local pkg; pkg=$(pkg_for "application/command/handler")
  local content; content=$(render_template "class/eventlistener.java.tmpl" "PKG=$pkg" "NAME=$NAME")
  write_file "$MODULE_DIR/application/command/handler/${NAME}EventListener.java" "application/command/handler" "$content"
}

gen_query() {
  local pkg; pkg=$(pkg_for "application/query/dto")
  local content; content=$(render_template "class/query.java.tmpl" "PKG=$pkg" "NAME=$NAME")
  write_file "$MODULE_DIR/application/query/dto/${NAME}Query.java" "application/query/dto" "$content"
}

gen_queryservice() {
  local pkg; pkg=$(pkg_for "application/query/service")
  local content; content=$(render_template "class/queryservice.java.tmpl" "PKG=$pkg" "NAME=$NAME")
  write_file "$MODULE_DIR/application/query/service/${NAME}QueryService.java" "application/query/service" "$content"
  gen_queryimpl_for "$NAME"
}

gen_queryimpl_for() {
  local target="$1"
  local pkg; pkg=$(pkg_for "infrastructure/db/query")
  local svc_pkg; svc_pkg=$(pkg_for "application/query/service")
  local content; content=$(render_template "class/queryservice-impl.java.tmpl" \
    "PKG=$pkg" "SVC_PKG=$svc_pkg" "TARGET=$target")
  write_file "$MODULE_DIR/infrastructure/db/query/${target}QueryServiceImpl.java" "infrastructure/db/query" "$content"
}

gen_queryimpl() { gen_queryimpl_for "$NAME"; }

gen_controller() {
  local pkg; pkg=$(pkg_for "presentation/controller")
  local path; path=$(to_kebab_path "$NAME")
  local content; content=$(render_template "class/controller.java.tmpl" "PKG=$pkg" "NAME=$NAME" "PATH=$path")
  write_file "$MODULE_DIR/presentation/controller/${NAME}Controller.java" "presentation/controller" "$content"
}

gen_exceptionhandler() {
  local pkg; pkg=$(pkg_for "presentation/controller")
  local content; content=$(render_template "class/exceptionhandler-empty.java.tmpl" \
    "PKG=$pkg" "NAME=$NAME" "BASE_PKG=$BASE_PKG" "MODULE=$MODULE")
  write_file "$MODULE_DIR/presentation/controller/${NAME}ExceptionHandler.java" "presentation/controller" "$content"
}

gen_request() {
  local pkg; pkg=$(pkg_for "presentation/request")
  local content; content=$(render_template "class/request.java.tmpl" "PKG=$pkg" "NAME=$NAME")
  write_file "$MODULE_DIR/presentation/request/${NAME}Request.java" "presentation/request" "$content"
}

gen_response() {
  local pkg; pkg=$(pkg_for "presentation/response")
  local content; content=$(render_template "class/response.java.tmpl" "PKG=$pkg" "NAME=$NAME")
  write_file "$MODULE_DIR/presentation/response/${NAME}Response.java" "presentation/response" "$content"
}

gen_api() {
  # aggregate が存在するか確認
  local agg_file="$MODULE_DIR/domain/model/aggregate/${NAME}.java"
  if [ ! -f "$agg_file" ]; then
    echo "Error: Aggregate '${NAME}' not found at $agg_file" >&2
    echo "Run 'scaffold class $MODULE aggregate $NAME' first." >&2
    exit 1
  fi

  local id_cls="${NAME}Id"
  local id_pkg; id_pkg=$(pkg_for "domain/model/valueobject/identifier")

  # --- presentation/request ---
  local req_pkg; req_pkg=$(pkg_for "presentation/request")
  local content; content=$(render_template "class/api-create-request.java.tmpl" "PKG=$req_pkg" "NAME=$NAME")
  write_file "$MODULE_DIR/presentation/request/Create${NAME}Request.java" "presentation/request" "$content"

  # --- presentation/response (Summary + Detail) ---
  local res_pkg; res_pkg=$(pkg_for "presentation/response")
  local qry_dto_pkg; qry_dto_pkg=$(pkg_for "application/query/dto")

  content=$(render_template "class/api-summary-response.java.tmpl" \
    "PKG=$res_pkg" "NAME=$NAME" "SUMMARY_DTO_PKG=$qry_dto_pkg")
  write_file "$MODULE_DIR/presentation/response/${NAME}SummaryResponse.java" "presentation/response" "$content"

  content=$(render_template "class/api-detail-response.java.tmpl" \
    "PKG=$res_pkg" "NAME=$NAME" "DETAIL_DTO_PKG=$qry_dto_pkg")
  write_file "$MODULE_DIR/presentation/response/${NAME}DetailResponse.java" "presentation/response" "$content"

  # --- application/command/command ---
  local cmd_pkg; cmd_pkg=$(pkg_for "application/command/command")
  content=$(render_template "class/api-create-command.java.tmpl" "PKG=$cmd_pkg" "NAME=$NAME")
  write_file "$MODULE_DIR/application/command/command/Create${NAME}Command.java" "application/command/command" "$content"

  # --- application/command/dto (CommandResult) ---
  local cmd_dto_pkg; cmd_dto_pkg=$(pkg_for "application/command/dto")
  content=$(render_template "class/api-created-dto.java.tmpl" "PKG=$cmd_dto_pkg" "NAME=$NAME")
  write_file "$MODULE_DIR/application/command/dto/Created${NAME}Dto.java" "application/command/dto" "$content"

  # --- application/command/handler ---
  local handler_pkg; handler_pkg=$(pkg_for "application/command/handler")
  local factory_pkg; factory_pkg=$(pkg_for "domain/service")
  local agg_pkg; agg_pkg=$(pkg_for "domain/model/aggregate")
  content=$(render_template "class/api-commandhandler.java.tmpl" \
    "PKG=$handler_pkg" "CMD_DTO_PKG=$cmd_dto_pkg" "CMD_PKG=$cmd_pkg" \
    "FACTORY_PKG=$factory_pkg" "AGG_PKG=$agg_pkg" "NAME=$NAME")
  write_file "$MODULE_DIR/application/command/handler/${NAME}CommandHandler.java" "application/command/handler" "$content"

  # --- application/query/param ---
  local param_pkg; param_pkg=$(pkg_for "application/query/param")
  content=$(render_template "class/api-list-param.java.tmpl" "PKG=$param_pkg" "NAME=$NAME")
  write_file "$MODULE_DIR/application/query/param/${NAME}ListParam.java" "application/query/param" "$content"

  # --- application/query/dto (QueryModel) ---
  content=$(render_template "class/api-summary-dto.java.tmpl" "PKG=$qry_dto_pkg" "NAME=$NAME")
  write_file "$MODULE_DIR/application/query/dto/${NAME}SummaryDto.java" "application/query/dto" "$content"

  content=$(render_template "class/api-detail-dto.java.tmpl" "PKG=$qry_dto_pkg" "NAME=$NAME")
  write_file "$MODULE_DIR/application/query/dto/${NAME}DetailDto.java" "application/query/dto" "$content"

  # --- application/query/service ---
  local qry_svc_pkg; qry_svc_pkg=$(pkg_for "application/query/service")
  content=$(render_template "class/api-queryservice.java.tmpl" \
    "PKG=$qry_svc_pkg" "PARAM_PKG=$param_pkg" "QRY_DTO_PKG=$qry_dto_pkg" "NAME=$NAME")
  write_file "$MODULE_DIR/application/query/service/${NAME}QueryService.java" "application/query/service" "$content"

  # --- infrastructure/db/query ---
  local infra_qry_pkg; infra_qry_pkg=$(pkg_for "infrastructure/db/query")
  content=$(render_template "class/api-queryservice-impl.java.tmpl" \
    "PKG=$infra_qry_pkg" "PARAM_PKG=$param_pkg" "QRY_DTO_PKG=$qry_dto_pkg" \
    "QRY_SVC_PKG=$qry_svc_pkg" "NAME=$NAME")
  write_file "$MODULE_DIR/infrastructure/db/query/${NAME}QueryServiceImpl.java" "infrastructure/db/query" "$content"

  # --- exception ---
  local exc_pkg; exc_pkg=$(pkg_for "exception")
  local exc_cls="${NAME}NotFoundException"
  content=$(render_template "class/exception-notfound.java.tmpl" "PKG=$exc_pkg" "NAME=$NAME")
  write_file "$MODULE_DIR/exception/${exc_cls}.java" "exception" "$content"

  # --- presentation/controller (ExceptionHandler) ---
  local ctrl_pkg; ctrl_pkg=$(pkg_for "presentation/controller")
  local handler_cls="${NAME}ExceptionHandler"
  content=$(render_template "class/exceptionhandler.java.tmpl" \
    "CTRL_PKG=$ctrl_pkg" "EXC_PKG=$exc_pkg" "EXC_CLS=$exc_cls" \
    "NAME=$NAME" "BASE_PKG=$BASE_PKG" "MODULE=$MODULE" \
    "HANDLER_CLS=$handler_cls" "METHOD_NAME=handleNotFound" \
    "STATUS_ENUM=NOT_FOUND" "STATUS_TITLE=Not Found")
  write_file "$MODULE_DIR/presentation/controller/${handler_cls}.java" "presentation/controller" "$content"

  # --- presentation/controller (main controller) ---
  local path; path=$(to_kebab_path "$NAME")
  content=$(render_template "class/api-controller.java.tmpl" \
    "CTRL_PKG=$ctrl_pkg" "CMD_DTO_PKG=$cmd_dto_pkg" "CMD_PKG=$cmd_pkg" \
    "EXC_PKG=$exc_pkg" "HANDLER_PKG=$handler_pkg" "PARAM_PKG=$param_pkg" \
    "QRY_SVC_PKG=$qry_svc_pkg" "REQ_PKG=$req_pkg" "RES_PKG=$res_pkg" \
    "NAME=$NAME" "PATH=$path")
  write_file "$MODULE_DIR/presentation/controller/${NAME}Controller.java" "presentation/controller" "$content"

  # --- テスト連鎖生成 ---
  if [ "$DRY_RUN" != true ]; then
    echo ""
    echo "Generating test skeletons..."
    ./scripts/scaffold.sh test "$MODULE" handler "${NAME}CommandHandler" || true
    ./scripts/scaffold.sh test "$MODULE" exceptionhandler "${handler_cls}" || true
    ./scripts/scaffold.sh test "$MODULE" response "${NAME}DetailResponse" || true
    ./scripts/scaffold.sh test "$MODULE" response "${NAME}SummaryResponse" || true
    ./scripts/scaffold.sh test "$MODULE" exception "${exc_cls}" || true
    ./scripts/scaffold.sh test "$MODULE" controller "${NAME}Controller" || true
    ./scripts/scaffold.sh test "$MODULE" security "${NAME}Controller" || true
    ./scripts/scaffold.sh test "$MODULE" integration "${NAME}QueryServiceImpl" || true
    ./scripts/scaffold.sh test "$MODULE" usecase "${NAME}CommandHandler" || true
    ./scripts/scaffold.sh test "$MODULE" moduletest "${NAME}CommandHandler" || true
    ./scripts/scaffold.sh test "$MODULE" jooqquery "${NAME}QueryServiceImpl" || true
    ./scripts/scaffold.sh test "$MODULE" e2e "${NAME}Controller" || true
  fi
}
