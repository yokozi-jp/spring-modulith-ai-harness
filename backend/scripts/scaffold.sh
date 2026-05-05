#!/usr/bin/env bash
# 統合スキャフォールドスクリプト
# 使い方: cd backend && ./scripts/scaffold <subcommand> [options] [args]
#
# サブコマンド:
#   module <module-name>                          モジュール作成
#   class  <module> <layer> <name> [--aggregate]  クラス/record/interface 作成
#   test   <module> <type> <target-class>         テストクラス作成
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
      Options: --dry-run, --no-test

  class <module> <layer> <name> [--aggregate <Aggregate>]
      Create a class/record/interface in the specified layer.
      Layers: event exception aggregate entity identifier valueobject
              repository repositoryimpl factory domainservice
              command commandhandler eventlistener query queryservice queryimpl
              controller exceptionhandler request response

  test <module> <type> <target-class>
      Create a test skeleton for an existing class.
      Types: unit, integration, controller

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

  if [ $# -ne 1 ]; then
    echo "Usage: scaffold module <module-name>" >&2
    exit 1
  fi
  local module="$1"

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
  generate_package_info "." "" "" "$local_pkg" > "$module_dir/package-info.java"

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
  local test_root="src/test/java/com/example/demo"

  if [ $# -ne 3 ]; then
    echo "Usage: scaffold test <module> <type> <target-class>" >&2
    echo "Types: unit, integration, controller" >&2
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
  if [[ "$type" != "unit" && "$type" != "integration" && "$type" != "controller" ]]; then
    echo "Error: Type must be one of: unit, integration, controller" >&2
    exit 1
  fi

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
  local test_dir="src/test/java/$rel_path"
  local test_file="$test_dir/${target}Test.java"

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

  case "$type" in
    unit)
      cat > "$test_file" << EOF
package $target_pkg;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for {@link $target}. */
@ExtendWith(MockitoExtension.class)
class ${target}Test {

  // @Mock private Dependency dependency;

  // @InjectMocks private $target sut;

  @Test
  void shouldDoSomething() {
    // TODO: implement test
  }
}
EOF
      ;;
    integration)
      cat > "$test_file" << EOF
package $target_pkg;

import com.example.demo.TestcontainersConfiguration;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestConstructor;

/** Integration tests for {@link $target}. */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class ${target}Test {

  /** テスト対象。 */
  private final $target sut;

  @Test
  void shouldDoSomething() {
    // TODO: implement test
  }
}
EOF
      ;;
    controller)
      cat > "$test_file" << EOF
package $target_pkg;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;

/** Unit tests for {@link $target}. */
@SuppressWarnings({"PMD.UnitTestShouldIncludeAssert", "PMD.AvoidDuplicateLiterals"})
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@WebMvcTest(controllers = {$target.class})
class ${target}Test {

  /** MockMvc。 */
  private final MockMvc mockMvc;

  // @MockitoBean private SomeDependency dependency;

  @Test
  @WithMockUser
  void shouldDoSomething() throws Exception {
    // TODO: implement test
  }
}
EOF
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
    echo "        command commandhandler eventlistener query queryservice" >&2
    echo "        controller exceptionhandler request response repositoryimpl queryimpl" >&2
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
    commandhandler) gen_commandhandler ;;
    eventlistener)  gen_eventlistener ;;
    query)          gen_query ;;
    queryservice)   gen_queryservice ;;
    queryimpl)      gen_queryimpl ;;
    controller)        gen_controller ;;
    exceptionhandler)  gen_exceptionhandler ;;
    request)        gen_request ;;
    response)       gen_response ;;
    *)
      echo "Error: Unknown layer '$layer'" >&2
      echo "Valid layers: event exception aggregate entity identifier valueobject repository domainservice factory" >&2
      echo "             command commandhandler eventlistener query queryservice" >&2
      echo "             controller exceptionhandler request response repositoryimpl queryimpl" >&2
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
