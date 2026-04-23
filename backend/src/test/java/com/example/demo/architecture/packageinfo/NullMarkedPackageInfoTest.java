package com.example.demo.architecture.packageinfo;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * com.example.demo 配下の全パッケージに @NullMarked 付き package-info.java が存在することを検証する。
 *
 * <p>NullAway は JSpecifyMode=true で動作しており、@NullMarked がないパッケージは「未注釈（unannotated）」扱いとなり null
 * チェックが緩くなる。全パッケージで確実に non-null デフォルトを適用するため、package-info.java の存在と @NullMarked の記述を強制する。
 */
class NullMarkedPackageInfoTest {

  /** ソースルートパス。 */
  private static final Path SRC_ROOT = Path.of("src/main/java/com/example/demo");

  @Test
  void allPackagesShouldHaveNullMarkedPackageInfo() throws IOException {
    try (Stream<Path> dirs = Files.walk(SRC_ROOT)) {
      final List<Path> violations =
          dirs.filter(Files::isDirectory).filter(this::isMissingNullMarkedPackageInfo).toList();

      assertThat(violations)
          .as("These packages are missing @NullMarked package-info.java")
          .isEmpty();
    }
  }

  private boolean isMissingNullMarkedPackageInfo(final Path dir) {
    final Path packageInfo = dir.resolve("package-info.java");
    boolean missing = !Files.exists(packageInfo);
    if (!missing) {
      try {
        final String content = Files.readString(packageInfo);
        missing = !content.contains("@NullMarked");
      } catch (IOException exception) {
        missing = true;
      }
    }
    return missing;
  }
}
