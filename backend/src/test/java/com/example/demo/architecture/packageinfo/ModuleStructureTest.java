package com.example.demo.architecture.packageinfo;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * 各モジュール内の存在するディレクトリに package-info.java があることを検証する。
 *
 * <p>ディレクトリの存在自体は強制しない（オンデマンド生成方式）。 ただし、ディレクトリが存在する場合は package-info.java が必須。
 */
class ModuleStructureTest {

  /** ソースルートパス。 */
  private static final Path SRC_ROOT = Path.of("src/main/java/com/example/demo");

  @Test
  void allExistingDirectoriesShouldHavePackageInfo() throws IOException {
    final List<String> violations = new ArrayList<>();

    try (Stream<Path> modules = Files.list(SRC_ROOT)) {
      modules
          .filter(Files::isDirectory)
          .filter(this::isModule)
          .forEach(mod -> checkAllSubDirs(mod, violations));
    }

    assertThat(violations).as("package-info.java 不足 — 各項目の修正指示に従ってください").isEmpty();
  }

  private boolean isModule(final Path dir) {
    return Files.exists(dir.resolve("package-info.java"));
  }

  private void checkAllSubDirs(final Path mod, final List<String> violations) {
    try (Stream<Path> dirs = Files.walk(mod)) {
      dirs.filter(Files::isDirectory)
          .filter(dir -> !Files.exists(dir.resolve("package-info.java")))
          .forEach(
              dir -> {
                final String relative = SRC_ROOT.relativize(dir).toString().replace('\\', '/');
                violations.add(
                    relative
                        + " — package-info.java が存在しない。"
                        + " 修正: "
                        + dir
                        + "/package-info.java を作成し、"
                        + "@NullMarked と適切な Onion アノテーションを付与してください");
              });
    } catch (IOException exception) {
      violations.add(mod + " — ディレクトリの走査に失敗: " + exception.getMessage());
    }
  }
}
