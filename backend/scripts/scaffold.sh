#!/usr/bin/env bash
# 統合スキャフォールドスクリプト
# 使い方: cd backend && ./scripts/scaffold <subcommand> [options] [args]
#
# サブコマンド:
#   module <module-name>                          モジュール作成
#   class  <module> <layer> <name> [--aggregate]  クラス/record/interface 作成
#   test   <module> <type> <target-class>         テストクラス作成
#          全て src/test/java に配置。@Tag で種別を分離。
#
# グローバルオプション:
#   --dry-run   作成予定のファイルを表示するのみ（module/class で有効）
#   --no-test   作成後のアーキテクチャテスト自動実行をスキップ（module で有効）
#   --help      ヘルプを表示
#
# 例:
#   ./scripts/scaffold module order
#   ./scripts/scaffold class order aggregate Order
#   ./scripts/scaffold class order entity OrderItem --aggregate Order
#   ./scripts/scaffold test order unit OrderCommandHandler

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

# === ヘルプ ===
show_help() {
  cat << 'EOF'
Usage: cd backend && ./scripts/scaffold <subcommand> [options] [args]

Subcommands:
  module <module-name>
      Create a new module (package-info.java at module root).
      Options: --display-name <name>, --dry-run, --no-test

  class <module> <layer> <name> [--aggregate <Aggregate>]
      Create a class/record/interface in the specified layer.
      Layers: event exception aggregate entity identifier valueobject
              repository repositoryimpl factory domainservice
              command commandresult commandhandler eventlistener
              query param queryservice queryimpl
              controller exceptionhandler request response api

  test <module> <type> <target-class>
      Create a test skeleton for an existing class.
      Types (src/test/ — no external deps):
        domain, factory, handler, exceptionhandler,
        response, exception, controller, security
      Types (src/test/ — @Tag("integration"), PostgreSQL):
        integration, usecase, moduletest, jooqquery
      Types (src/test/ — @Tag("e2e"), all containers):
        e2e

Global Options:
  --dry-run   Preview files without creating them
  --no-test   Skip architecture test run after module creation
  --help      Show this help
EOF
}

# === グローバルオプション解析 ===
DRY_RUN=false
NO_TEST=false
ARGS=()

for arg in "$@"; do
  case "$arg" in
    --dry-run) DRY_RUN=true ;;
    --no-test) NO_TEST=true ;;
    --help|-h) show_help; exit 0 ;;
    *) ARGS+=("$arg") ;;
  esac
done

if [ "${#ARGS[@]}" -eq 0 ]; then
  show_help
  exit 1
fi

SUBCOMMAND="${ARGS[0]}"
ARGS=("${ARGS[@]:1}")

# === cmd_module: モジュール作成 ===
cmd_module() {
  # shellcheck source=module-common.sh
  source "$SCRIPT_DIR/module-common.sh"

  local display_name=""
  local positional=()
  while [[ $# -gt 0 ]]; do
    case "$1" in
      --display-name) display_name="$2"; shift 2 ;;
      *) positional+=("$1"); shift ;;
    esac
  done

  if [ "${#positional[@]}" -ne 1 ]; then
    echo "Usage: scaffold module <module-name> [--display-name <name>]" >&2
    exit 1
  fi
  local module="${positional[0]}"

  if [[ ! "$module" =~ ^[a-z][a-z0-9]*$ ]]; then
    echo "Error: Module name must start with a lowercase letter and contain only lowercase letters and digits." >&2
    exit 1
  fi

  local module_dir="$SRC_ROOT/$module"
  if [ -d "$module_dir" ]; then
    echo "Error: Module '$module' already exists at $module_dir" >&2
    exit 1
  fi

  if [ "$DRY_RUN" = true ]; then
    echo "[CREATE DIR]  $module_dir/"
    echo "[CREATE FILE] $module_dir/package-info.java"
    echo ""
    echo "Dry-run complete. No files were created."
    return
  fi

  mkdir -p "$module_dir"
  local local_pkg="$BASE_PKG.$module"
  generate_package_info "." "" "" "$local_pkg" "$display_name" > "$module_dir/package-info.java"

  echo "Module '$module' created at $module_dir"
  echo "  Created: $module_dir/package-info.java"
  echo ""
  echo "Use 'scaffold class' to add classes (directories are created automatically)."

  if [ "$NO_TEST" = true ]; then
    echo ""
    echo "Skipping architecture tests (--no-test)."
    return
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
}

# === cmd_test: テストクラス作成 ===
cmd_test() {
  local base_pkg="com.example.demo"
  local src_root="src/main/java/com/example/demo"

  if [ $# -lt 3 ]; then
    echo "Usage: scaffold test <module> <type> <target-class>" >&2
    echo "" >&2
    echo "Types (src/test/ — no external deps):" >&2
    echo "  domain         Aggregate/Entity/VO unit test (plain JUnit)" >&2
    echo "  factory        Factory unit test (Mockito)" >&2
    echo "  handler        CommandHandler unit test (Mockito)" >&2
    echo "  exceptionhandler  ExceptionHandler unit test (plain JUnit)" >&2
    echo "  response       Response record from() test (plain JUnit)" >&2
    echo "  exception      Exception message test (plain JUnit)" >&2
    echo "  controller     @WebMvcTest (MockMvc + @MockitoBean)" >&2
    echo "  security       @WebMvcTest security test (auth/CSRF)" >&2
    echo "" >&2
    echo "Types (src/test/ — @Tag(\"integration\"), PostgreSQL container):" >&2
    echo "  integration    @SpringBootTest + @Tag(\"integration\")" >&2
    echo "  usecase        UseCase→Domain integration (full flow)" >&2
    echo "  moduletest     @ApplicationModuleTest (event publish/subscribe)" >&2
    echo "  jooqquery      @JooqTest + @Tag(\"integration\") (SQL query)" >&2
    echo "" >&2
    echo "Types (src/test/ — @Tag(\"e2e\"), all containers):" >&2
    echo "  e2e            @SpringBootTest(RANDOM_PORT) + @Tag(\"e2e\")" >&2
    exit 1
  fi

  local module="$1" type="$2" target="$3"

  if [[ ! "$module" =~ ^[a-z][a-z0-9]*$ ]]; then
    echo "Error: Module name must be lowercase alphanumeric" >&2
    exit 1
  fi
  if [[ ! "$target" =~ ^[A-Z][a-zA-Z0-9]*$ ]]; then
    echo "Error: Target class must be PascalCase" >&2
    exit 1
  fi

  local valid_types="domain factory handler exceptionhandler response exception controller security integration usecase moduletest jooqquery e2e"
  if ! echo "$valid_types" | grep -qw "$type"; then
    echo "Error: Unknown type '$type'" >&2
    echo "Valid types: $valid_types" >&2
    exit 1
  fi

  # 全テストを src/test/java に配置（@Tag でフィルタ分離）
  local test_src_root="src/test/java"

  local target_file
  target_file=$(find "$src_root/$module" -name "${target}.java" 2>/dev/null | head -1)
  if [ -z "$target_file" ]; then
    echo "Error: ${target}.java not found in module '$module'" >&2
    exit 1
  fi

  local target_pkg
  target_pkg=$(grep "^package " "$target_file" | sed 's/package //;s/;//')
  local rel_path
  rel_path=$(echo "$target_pkg" | tr '.' '/')
  local test_dir="$test_src_root/$rel_path"

  # security type はクラス名に Security サフィックスを付ける
  # usecase type はクラス名に IntTest サフィックスを付ける
  # moduletest type はクラス名に ModuleTest サフィックスを付ける
  # jooqquery type はクラス名に QueryTest サフィックスを付ける
  local test_class_name="${target}Test"
  if [ "$type" = "security" ]; then
    test_class_name="${target}SecurityTest"
  elif [ "$type" = "usecase" ]; then
    test_class_name="${target}IntTest"
  elif [ "$type" = "moduletest" ]; then
    test_class_name="${target}ModuleTest"
  elif [ "$type" = "jooqquery" ]; then
    test_class_name="${target}QueryTest"
  fi
  local test_file="$test_dir/${test_class_name}.java"

  if [ -f "$test_file" ]; then
    echo "[SKIP] $test_file (already exists)"
    return
  fi

  if [ "$DRY_RUN" = true ]; then
    echo "[CREATE FILE] $test_file"
    echo ""
    echo "Dry-run complete. No files were created."
    return
  fi

  mkdir -p "$test_dir"

  # source module-common.sh for render_template (if not already sourced)
  if ! declare -f render_template > /dev/null 2>&1; then
    source "$SCRIPT_DIR/module-common.sh"
  fi

  # 共通プレースホルダー
  local common_args=("PKG=$target_pkg" "TARGET=$target" "TEST_CLS=$test_class_name")

  case "$type" in
    domain|factory|handler|exceptionhandler|response|exception|integration|usecase|moduletest|jooqquery|e2e)
      render_template "test/${type}.java.tmpl" "${common_args[@]}" > "$test_file"
      ;;
    controller|security)
      local agg_name="${target%Controller}"
      local handler_pkg="${base_pkg}.${module}.application.command.handler"
      local qry_svc_pkg="${base_pkg}.${module}.application.query.service"
      render_template "test/${type}.java.tmpl" "${common_args[@]}" \
        "AGG_NAME=$agg_name" "HANDLER_PKG=$handler_pkg" "QRY_SVC_PKG=$qry_svc_pkg" > "$test_file"
      ;;
    *)
      echo "Error: Unknown type '$type'" >&2
      exit 1
      ;;
  esac

  echo "[CREATE] $test_file"
}

# === cmd_class: クラス/record/interface 作成 ===
# (大きいため別途 source する)
cmd_class() {
  # shellcheck source=module-common.sh
  source "$SCRIPT_DIR/module-common.sh"

  # オプション解析（--aggregate は ARGS から除外済みなので再解析）
  local aggregate=""
  local positional=()

  while [[ $# -gt 0 ]]; do
    case "$1" in
      --aggregate) aggregate="$2"; shift 2 ;;
      *) positional+=("$1"); shift ;;
    esac
  done

  if [ "${#positional[@]}" -ne 3 ]; then
    echo "Usage: scaffold class <module> <layer> <name> [--aggregate <Aggregate>]" >&2
    echo "Layers: event exception aggregate entity identifier valueobject repository domainservice factory" >&2
    echo "        command commandresult commandhandler eventlistener query param queryservice" >&2
    echo "        controller exceptionhandler request response repositoryimpl queryimpl api" >&2
    exit 1
  fi

  local module="${positional[0]}" layer="${positional[1]}" name="${positional[2]}"

  if [[ ! "$module" =~ ^[a-z][a-z0-9]*$ ]]; then
    echo "Error: Module name must be lowercase alphanumeric (e.g., 'order')" >&2
    exit 1
  fi
  if [[ ! "$name" =~ ^[A-Z][a-zA-Z0-9]*$ ]]; then
    echo "Error: Name must be PascalCase (e.g., 'Order', 'OrderItem')" >&2
    exit 1
  fi

  MODULE="$module"
  MODULE_DIR="$SRC_ROOT/$module"
  NAME="$name"
  AGGREGATE="$aggregate"

  if [ ! -d "$MODULE_DIR" ]; then
    echo "Error: Module '$module' does not exist. Run 'scaffold module' first." >&2
    exit 1
  fi

  # source the class generation functions
  source "$SCRIPT_DIR/_class_generators.sh"

  case "$layer" in
    event)          gen_event ;;
    exception)      gen_exception ;;
    aggregate)      gen_aggregate ;;
    entity)         gen_entity ;;
    identifier)     gen_identifier ;;
    valueobject)    gen_valueobject ;;
    repository)     gen_repository ;;
    repositoryimpl) gen_repositoryimpl ;;
    factory)        gen_factory ;;
    domainservice)  gen_domainservice ;;
    command)        gen_command ;;
    commandresult)  gen_commandresult ;;
    commandhandler) gen_commandhandler ;;
    eventlistener)  gen_eventlistener ;;
    query)          gen_query ;;
    param)          gen_param ;;
    queryservice)   gen_queryservice ;;
    queryimpl)      gen_queryimpl ;;
    controller)        gen_controller ;;
    exceptionhandler)  gen_exceptionhandler ;;
    request)        gen_request ;;
    response)       gen_response ;;
    api)            gen_api ;;
    *)
      echo "Error: Unknown layer '$layer'" >&2
      echo "Valid layers: event exception aggregate entity identifier valueobject repository domainservice factory" >&2
      echo "             command commandresult commandhandler eventlistener query param queryservice" >&2
      echo "             controller exceptionhandler request response repositoryimpl queryimpl api" >&2
      exit 1
      ;;
  esac
}

# === サブコマンドディスパッチ ===
case "$SUBCOMMAND" in
  module) cmd_module "${ARGS[@]}" ;;
  class)  cmd_class "${ARGS[@]}" ;;
  test)   cmd_test "${ARGS[@]}" ;;
  *)
    echo "Error: Unknown subcommand '$SUBCOMMAND'" >&2
    echo "Run './scripts/scaffold --help' for usage." >&2
    exit 1
    ;;
esac
