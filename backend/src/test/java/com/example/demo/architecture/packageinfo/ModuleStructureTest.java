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
 * 各モジュールに必須ディレクトリ構成が存在することを検証する。
 *
 * <p>新しいモジュールを追加した際にディレクトリ構成の漏れを CI で検出する。
 */
class ModuleStructureTest {

  /** ソースルートパス。 */
  private static final Path SRC_ROOT = Path.of("src/main/java/com/example/demo");

  /** モジュール内に必須のサブディレクトリ一覧（相対パス）。 */
  private static final List<String> REQUIRED_DIRS =
      List.of(
          "event",
          "domain",
          "domain/model",
          "domain/model/aggregate",
          "domain/model/entity",
          "domain/model/valueobject",
          "domain/model/valueobject/identifier",
          "domain/repository",
          "domain/service",
          "application",
          "application/command",
          "application/command/dto",
          "application/command/handler",
          "application/query",
          "application/query/dto",
          "application/query/service",
          "presentation",
          "presentation/controller",
          "presentation/request",
          "presentation/response",
          "infrastructure",
          "infrastructure/db",
          "infrastructure/db/repository",
          "infrastructure/db/query");

  @Test
  void allModulesShouldHaveRequiredStructure() throws IOException {
    final List<String> violations = new ArrayList<>();

    try (Stream<Path> modules = Files.list(SRC_ROOT)) {
      modules
          .filter(Files::isDirectory)
          .filter(this::isModule)
          .forEach(mod -> checkModule(mod, violations));
    }

    assertThat(violations).as("Module structure violations").isEmpty();
  }

  private boolean isModule(final Path dir) {
    // package-info.java を持つ直下ディレクトリをモジュールとみなす
    return Files.exists(dir.resolve("package-info.java"));
  }

  private void checkModule(final Path mod, final List<String> violations) {
    final Path fileName = mod.getFileName();
    if (fileName == null) {
      return;
    }
    final String name = fileName.toString();
    for (final String required : REQUIRED_DIRS) {
      final Path target = mod.resolve(required);
      if (Files.isDirectory(target) && Files.exists(target.resolve("package-info.java"))) {
        continue;
      }
      if (Files.isDirectory(target)) {
        violations.add(name + "/" + required + " — package-info.java が存在しない");
      } else {
        violations.add(name + "/" + required + " — ディレクトリが存在しない");
      }
    }
  }
}
