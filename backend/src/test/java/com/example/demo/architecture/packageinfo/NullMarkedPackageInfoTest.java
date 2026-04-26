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
    final List<String> violations = new ArrayList<>();

    try (Stream<Path> dirs = Files.walk(SRC_ROOT)) {
      dirs.filter(Files::isDirectory).forEach(dir -> checkNullMarked(dir, violations));
    }

    assertThat(violations).as("@NullMarked package-info.java の違反 — 各項目の修正指示に従ってください").isEmpty();
  }

  private void checkNullMarked(final Path dir, final List<String> violations) {
    final Path packageInfo = dir.resolve("package-info.java");
    final String relative = SRC_ROOT.relativize(dir).toString().replace('\\', '/');
    final String pkg =
        relative.isEmpty() ? "com.example.demo" : "com.example.demo." + relative.replace('/', '.');

    if (!Files.exists(packageInfo)) {
      violations.add(
          relative
              + " — package-info.java が存在しない。"
              + " 修正: "
              + dir
              + "/package-info.java を作成し、"
              + "内容は '@NullMarked package "
              + pkg
              + ";' + import org.jspecify.annotations.NullMarked; としてください");
      return;
    }
    try {
      final String content = Files.readString(packageInfo);
      if (!content.contains("@NullMarked")) {
        violations.add(
            relative
                + " — package-info.java に @NullMarked が未記述。"
                + " 修正: "
                + packageInfo
                + " に '@NullMarked' アノテーションと"
                + " 'import org.jspecify.annotations.NullMarked;' を追加してください");
      }
    } catch (IOException exception) {
      violations.add(relative + " — package-info.java の読み取りに失敗: " + exception.getMessage());
    }
  }
}
