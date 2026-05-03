#!/usr/bin/env bash
# テストクラス雛形生成スクリプト
# 使い方: cd backend && ./scripts/create-test.sh <module> <type> <target-class>
#
# type 一覧:
#   unit        — @ExtendWith(MockitoExtension.class) + @Mock / @InjectMocks
#   integration — @SpringBootTest + @Import(TestcontainersConfiguration.class)
#   controller  — @WebMvcTest + @MockitoBean + @WithMockUser
#
# 例:
#   ./scripts/create-test.sh order unit OrderCommandHandler
#   ./scripts/create-test.sh order integration OrderRepositoryImpl
#   ./scripts/create-test.sh order controller OrderController

set -euo pipefail

BASE_PKG="com.example.demo"
SRC_ROOT="src/main/java/com/example/demo"
TEST_ROOT="src/test/java/com/example/demo"

# === 引数解析 ===
if [ $# -ne 3 ]; then
  echo "Usage: cd backend && $0 <module> <type> <target-class>" >&2
  echo "Types: unit, integration, controller" >&2
  exit 1
fi

MODULE="$1"
TYPE="$2"
TARGET="$3"

# === バリデーション ===
if [[ ! "$MODULE" =~ ^[a-z][a-z0-9]*$ ]]; then
  echo "Error: Module name must be lowercase alphanumeric" >&2
  exit 1
fi

if [[ ! "$TARGET" =~ ^[A-Z][a-zA-Z0-9]*$ ]]; then
  echo "Error: Target class must be PascalCase" >&2
  exit 1
fi

if [[ "$TYPE" != "unit" && "$TYPE" != "integration" && "$TYPE" != "controller" ]]; then
  echo "Error: Type must be one of: unit, integration, controller" >&2
  exit 1
fi

# === 対象クラスのパッケージを検出 ===
TARGET_FILE=$(find "$SRC_ROOT/$MODULE" -name "${TARGET}.java" 2>/dev/null | head -1)
if [ -z "$TARGET_FILE" ]; then
  echo "Error: ${TARGET}.java not found in module '$MODULE'" >&2
  exit 1
fi

# パッケージを抽出
TARGET_PKG=$(grep "^package " "$TARGET_FILE" | sed 's/package //;s/;//')
# テスト用の相対パス
REL_PATH=$(echo "$TARGET_PKG" | tr '.' '/')
TEST_DIR="src/test/java/$REL_PATH"
TEST_FILE="$TEST_DIR/${TARGET}Test.java"

if [ -f "$TEST_FILE" ]; then
  echo "[SKIP] $TEST_FILE (already exists)"
  exit 0
fi

mkdir -p "$TEST_DIR"

# === テンプレート生成 ===
case "$TYPE" in
  unit)
    cat > "$TEST_FILE" << EOF
package $TARGET_PKG;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for {@link $TARGET}. */
@ExtendWith(MockitoExtension.class)
class ${TARGET}Test {

  // @Mock private Dependency dependency;

  // @InjectMocks private $TARGET sut;

  @Test
  void shouldDoSomething() {
    // TODO: implement test
  }
}
EOF
    ;;
  integration)
    cat > "$TEST_FILE" << EOF
package $TARGET_PKG;

import com.example.demo.TestcontainersConfiguration;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestConstructor;

/** Integration tests for {@link $TARGET}. */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class ${TARGET}Test {

  /** テスト対象。 */
  private final $TARGET sut;

  @Test
  void shouldDoSomething() {
    // TODO: implement test
  }
}
EOF
    ;;
  controller)
    cat > "$TEST_FILE" << EOF
package $TARGET_PKG;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;

/** Unit tests for {@link $TARGET}. */
@SuppressWarnings({"PMD.UnitTestShouldIncludeAssert", "PMD.AvoidDuplicateLiterals"})
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@WebMvcTest(controllers = {$TARGET.class})
class ${TARGET}Test {

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

echo "[CREATE] $TEST_FILE"
