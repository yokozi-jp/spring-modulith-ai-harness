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
    echo "  integration    @SpringBootTest + PostgresContainerConfig" >&2
    echo "  usecase        UseCase→Domain integration (full flow)" >&2
    echo "  moduletest     @ApplicationModuleTest (event publish/subscribe)" >&2
    echo "  jooqquery      @JooqTest + PostgresContainerConfig (SQL query)" >&2
    echo "" >&2
    echo "Types (src/test/ — @Tag(\"e2e\"), all containers):" >&2
    echo "  e2e            @SpringBootTest(RANDOM_PORT) + FullStackContainerConfig" >&2
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

  case "$type" in
    domain)
      cat > "$test_file" << EOF
package $target_pkg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link $target}. */
class ${test_class_name} {

  // TODO: テスト用定数を定義する（ID値、名前等）
  // private static final String ID_VALUE = "test-id";

  // TODO: 以下の観点でテストを実装する
  // - 正常系: 新規作成時の初期状態（ステータス、フィールド値）
  // - 正常系: 状態遷移メソッド（publish, confirm 等）
  // - 正常系: ドメインイベントの登録（getDomainEvents()）
  // - 異常系: null / blank 引数で例外
  // - 異常系: 不正な状態遷移で例外
  // - reconstitute で再構築した場合にイベントが登録されないこと

  /** TODO: 正常系テストを実装する。 */
  @Test
  void shouldCreateSuccessfully() {
    // TODO: new $target(...) or reconstitute(...) で生成し、状態を検証
    assertNotNull(null, "TODO: replace with actual assertion");
  }

  /** TODO: 異常系テストを実装する。 */
  @Test
  void shouldThrowWhenInvalidArgument() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          // TODO: 不正な引数で生成を試みる
        },
        "should throw on invalid argument");
  }
}
EOF
      ;;
    factory)
      cat > "$test_file" << EOF
package $target_pkg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for {@link $target}. */
@ExtendWith(MockitoExtension.class)
class ${test_class_name} {

  // TODO: Repository と Clock をモックする
  // @Mock private XxxRepository repository;
  // @Mock private java.time.Clock clock;

  /** テスト対象。 */
  @InjectMocks private $target sut;

  // TODO: 以下の観点でテストを実装する
  // - 生成された集約が正しい ID を持つこと（repository.generateId() をモック）
  // - 生成された集約が DRAFT ステータスであること
  // - 生成された集約にドメインイベントが登録されていること

  /** TODO: 集約が正しく生成されること。 */
  @Test
  void shouldCreateAggregate() {
    // TODO: when(repository.generateId()).thenReturn(new XxxId("gen-id"));
    //        final Xxx result = sut.create(...);
    //        assertEquals("gen-id", result.getId().value(), "id should match");
    assertNotNull(sut, "TODO: replace with actual assertion");
  }
}
EOF
      ;;
    handler)
      cat > "$test_file" << EOF
package $target_pkg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for {@link $target}. */
@ExtendWith(MockitoExtension.class)
class ${test_class_name} {

  // TODO: Factory / Repository をモックする
  // @Mock private XxxFactory factory;

  /** テスト対象。 */
  @InjectMocks private $target sut;

  // TODO: 以下の観点でテストを実装する
  // - handle() が Factory に委譲すること（verify）
  // - handle() が正しい ID を含む DTO を返すこと
  // - handle() の戻り値が null でないこと

  /** TODO: コマンドを処理して結果を返すこと。 */
  @Test
  void shouldHandleCommand() {
    // TODO: when(factory.create(...)).thenReturn(aggregate);
    //        final XxxDto result = sut.handle(new CreateXxxCommand(...));
    //        assertEquals("expected-id", result.id(), "id should match");
    assertNotNull(sut, "TODO: replace with actual assertion");
  }
}
EOF
      ;;
    exceptionhandler)
      cat > "$test_file" << EOF
package $target_pkg;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

/** Unit tests for {@link $target}. */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class ${test_class_name} {

  /** テスト対象。 */
  private final $target sut = new $target();

  /** 正常系: 適切なステータスコードを返すこと。 */
  @Test
  void shouldReturnExpectedStatus() {
    // TODO: call sut.handleXxx(new XxxException("id"))
    // final ProblemDetail result = sut.handleNotFound(...);
    // assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatus(), "status should be 404");
  }
}
EOF
      ;;
    response)
      cat > "$test_file" << EOF
package $target_pkg;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link $target}. */
class ${test_class_name} {

  /** from() が DTO のフィールドを正しく変換すること。 */
  @Test
  void shouldConvertFromDto() {
    // TODO: create DTO, call ${target}.from(dto), verify fields
    assertEquals("expected", "expected", "TODO: replace with actual assertion");
  }
}
EOF
      ;;
    exception)
      cat > "$test_file" << EOF
package $target_pkg;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link $target}. */
class ${test_class_name} {

  /** メッセージに引数が含まれること。 */
  @Test
  void shouldContainArgumentInMessage() {
    final $target ex = new $target("test-id");

    assertEquals("TODO: expected message", ex.getMessage(), "message should contain id");
  }
}
EOF
      ;;
    controller)
      cat > "$test_file" << EOF
package $target_pkg;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.example.demo.config.WebMvcConfig;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

/** Controller tests for {@link $target}. */
@SuppressWarnings("PMD.UnitTestShouldIncludeAssert")
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@WebMvcTest(
    controllers = {$target.class},
    excludeFilters =
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebMvcConfig.class))
class ${test_class_name} {

  /** MockMvc。 */
  private final MockMvc mockMvc;

  // TODO: Controller の依存を @MockitoBean で宣言する
  // @MockitoBean private XxxCommandHandler commandHandler;
  // @MockitoBean private XxxQueryService queryService;

  // TODO: 以下の観点でテストを実装する
  // - POST: 201 Created + Location ヘッダー（when + csrf()）
  // - POST: バリデーション違反で 400 Bad Request
  // - GET /{id}: 200 + JSON レスポンス（when + Optional.of）
  // - GET /{id}: 存在しない場合 404（when + Optional.empty）
  // - GET (一覧): 200 + Page レスポンス

  /** TODO: Controller テストを実装する。 */
  @Test
  @WithMockUser
  void shouldDoSomething() throws Exception {
    // TODO: mockMvc.perform(MockMvcRequestBuilders.get("/path"))
    //     .andExpect(MockMvcResultMatchers.status().isOk());
  }
}
EOF
      ;;
    security)
      cat > "$test_file" << EOF
package $target_pkg;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.example.demo.config.WebMvcConfig;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

/** Security tests for {@link $target}. */
@SuppressWarnings("PMD.UnitTestShouldIncludeAssert")
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@WebMvcTest(
    controllers = {$target.class},
    excludeFilters =
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebMvcConfig.class))
class ${test_class_name} {

  /** MockMvc。 */
  private final MockMvc mockMvc;

  // TODO: Controller の依存を @MockitoBean で宣言する
  // @MockitoBean private XxxCommandHandler commandHandler;
  // @MockitoBean private XxxQueryService queryService;

  // TODO: 以下の観点でテストを実装する
  // - @WithAnonymousUser: 未認証で 302 リダイレクト（OAuth2 Login）
  // - @WithMockUser: CSRF トークンなしで POST → 403 Forbidden
  // - @WithMockUser: 認証済みで正常アクセス → 200
  // - @WithMockUser(roles="WRONG"): 権限不足で 403（ロールベース認可がある場合）

  /** TODO: 未認証でリダイレクトされること。 */
  @Test
  @WithAnonymousUser
  void shouldRedirectWhenUnauthenticated() throws Exception {
    // TODO: mockMvc.perform(MockMvcRequestBuilders.get("/path"))
    //     .andExpect(MockMvcResultMatchers.status().is3xxRedirection());
  }

  /** TODO: CSRF トークンなしで 403 になること。 */
  @Test
  @WithMockUser
  void shouldReturn403WhenNoCsrfToken() throws Exception {
    // TODO: mockMvc.perform(MockMvcRequestBuilders.post("/path")
    //         .contentType(MediaType.APPLICATION_JSON)
    //         .content("{}"))
    //     .andExpect(MockMvcResultMatchers.status().isForbidden());
  }
}
EOF
      ;;
    integration)
      cat > "$test_file" << EOF
package $target_pkg;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.demo.testconfig.PostgresContainerConfig;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.annotation.Transactional;

/** Integration tests for {@link $target}. */
@Tag("integration")
@Import(PostgresContainerConfig.class)
@SpringBootTest
@Transactional
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class ${test_class_name} {

  /** テスト対象。 */
  private final $target sut;

  // TODO: 以下を検証するテストを実装する
  // - CRUD 操作（save / findById / delete）
  // - ID 生成（generateId が一意の値を返す）
  // - jOOQ マッピング（ドメインオブジェクト ↔ DB レコード変換）
  // - ページネーション（findAll + Pageable）

  /** 正常系: 基本動作を確認する。 */
  @Test
  void shouldDoSomething() {
    assertNotNull(sut, "sut should be injected");
    // TODO: implement integration test
  }
}
EOF
      ;;
    usecase)
      cat > "$test_file" << EOF
package $target_pkg;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.demo.testconfig.PostgresContainerConfig;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.annotation.Transactional;

/** UseCase integration tests for {@link $target}. */
@Tag("integration")
@Import(PostgresContainerConfig.class)
@SpringBootTest
@Transactional
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class ${test_class_name} {

  /** テスト対象。 */
  private final $target sut;

  // TODO: 以下の一連フローを検証するテストを実装する
  // - CommandHandler → Factory → Repository → DB の完全フロー
  // - コマンド実行後に DB にデータが永続化されていること
  // - 戻り値の DTO が正しい値を持つこと

  /** 正常系: コマンドが一連のフローを通じて正常に処理されること。 */
  @Test
  void shouldProcessCommandThroughFullFlow() {
    // TODO: sut.handle(new CreateXxxCommand(...)) を呼び出し、
    //        結果の ID が null でないことを検証する
    assertNotNull(sut, "sut should be injected");
  }
}
EOF
      ;;
    moduletest)
      cat > "$test_file" << EOF
package $target_pkg;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.demo.testconfig.PostgresContainerConfig;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;
import org.springframework.test.context.TestConstructor;

/** Module integration tests for {@link $target} using Spring Modulith. */
@Tag("integration")
@Import(PostgresContainerConfig.class)
@ApplicationModuleTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class ${test_class_name} {

  /** テスト対象。 */
  private final $target sut;

  // TODO: 以下を検証するテストを実装する
  // - ドメインイベントが正しく発行されること（Scenario.publish().andWaitForEventOfType()）
  // - イベントリスナーが副作用を実行すること
  // - モジュール境界を越えたイベント伝播

  /** 正常系: コマンド実行でドメインイベントが発行されること。 */
  @Test
  void shouldPublishDomainEvent(final Scenario scenario) {
    // TODO: scenario を使ってイベント発行を検証する
    // scenario.stimulate(() -> sut.handle(new CreateXxxCommand(...)))
    //     .andWaitForEventOfType(XxxCreated.class)
    //     .toArriveAndVerify(event -> assertNotNull(event.id()));
    assertNotNull(sut, "sut should be injected");
  }
}
EOF
      ;;
    jooqquery)
      cat > "$test_file" << EOF
package $target_pkg;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.demo.testconfig.PostgresContainerConfig;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jooq.JooqTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestConstructor;

/** jOOQ query tests for {@link $target}. */
@Tag("integration")
@Import(PostgresContainerConfig.class)
@JooqTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class ${test_class_name} {

  /** jOOQ DSLContext。 */
  private final DSLContext dsl;

  // TODO: 以下の観点でテストを実装する
  // - SELECT クエリが正しい結果を返すこと
  // - WHERE 条件が正しくフィルタすること
  // - ページネーション（LIMIT / OFFSET）が正しく動作すること
  // - JOIN が正しいデータを結合すること
  // - INSERT / UPDATE が正しく永続化すること

  /** TODO: クエリが正しく動作すること。 */
  @Test
  void shouldExecuteQuery() {
    assertNotNull(dsl, "DSLContext should be injected");
    // TODO: dsl.selectFrom(TABLE).where(...).fetch() を検証する
  }
}
EOF
      ;;
    e2e)
      cat > "$test_file" << EOF
package $target_pkg;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.demo.testconfig.FullStackContainerConfig;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestConstructor;

/** E2E tests for {@link $target}. */
@Tag("e2e")
@Import(FullStackContainerConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class ${test_class_name} {

  /** HTTP クライアント。 */
  private final TestRestTemplate restTemplate;

  // TODO: 以下の観点でテストを実装する
  // - GET /api/v1/xxx/{id}: 存在しない ID で 404
  // - POST /api/v1/xxx: 作成 → 201 + Location ヘッダー
  // - GET /api/v1/xxx/{id}: 作成後に取得 → 200 + 正しいレスポンス
  // - 認証が必要なエンドポイントへの未認証アクセス → 302/401
  // ※ E2E では WebMvcConfig が有効なため /api/v1/ プレフィックスが必要

  /** TODO: API エンドポイントにアクセスできること。 */
  @Test
  void shouldAccessEndpoint() {
    // TODO: final ResponseEntity<String> response =
    //     restTemplate.getForEntity("/api/v1/path/non-existent", String.class);
    // assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "status should be 404");
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
