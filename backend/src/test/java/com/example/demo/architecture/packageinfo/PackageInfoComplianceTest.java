package com.example.demo.architecture.packageinfo;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * package-info.java の存在・{@code @NullMarked}・Onion Architecture アノテーションを一括検証する。
 *
 * <p>3つの独立した関心事を1クラスに統合し、ファイルシステム走査ロジックを共有する。
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class PackageInfoComplianceTest {

  /** ソースルートパス。 */
  private static final Path SRC_ROOT = Path.of("src/main/java/com/example/demo");

  /** Onion アノテーション名の定数。 */
  private static final String DOMAIN_MODEL = "@DomainModelRing";

  /** Onion アノテーション名の定数。 */
  private static final String DOMAIN_SVC = "@DomainServiceRing";

  /** Onion アノテーション名の定数。 */
  private static final String APP_SVC = "@ApplicationServiceRing";

  /** Onion アノテーション名の定数。 */
  private static final String INFRA = "@InfrastructureRing";

  /** アノテーション名から FQCN を導出するマッピング。 */
  private static final Map<String, String> IMPORT_MAP =
      Map.of(
          DOMAIN_MODEL,
          "org.jmolecules.architecture.onion.classical.DomainModelRing",
          DOMAIN_SVC,
          "org.jmolecules.architecture.onion.classical.DomainServiceRing",
          APP_SVC,
          "org.jmolecules.architecture.onion.classical.ApplicationServiceRing",
          INFRA,
          "org.jmolecules.architecture.onion.classical.InfrastructureRing");

  /** パッケージ末尾名と期待する Onion アノテーションの対応。具体的なパスから先にマッチさせる。 */
  private static final List<Map.Entry<String, String>> LAYER_RULES =
      List.of(
          Map.entry("event", DOMAIN_MODEL),
          Map.entry("domain/model/aggregate", DOMAIN_MODEL),
          Map.entry("domain/model/entity", DOMAIN_MODEL),
          Map.entry("domain/model/valueobject/identifier", DOMAIN_MODEL),
          Map.entry("domain/model/valueobject", DOMAIN_MODEL),
          Map.entry("domain/model", DOMAIN_MODEL),
          Map.entry("domain/repository", DOMAIN_MODEL),
          Map.entry("domain/service", DOMAIN_SVC),
          Map.entry("domain", DOMAIN_MODEL),
          Map.entry("application/command/dto", APP_SVC),
          Map.entry("application/command/handler", APP_SVC),
          Map.entry("application/command", APP_SVC),
          Map.entry("application/query/dto", APP_SVC),
          Map.entry("application/query/service", APP_SVC),
          Map.entry("application/query", APP_SVC),
          Map.entry("application", APP_SVC),
          Map.entry("presentation/controller", INFRA),
          Map.entry("presentation/request", INFRA),
          Map.entry("presentation/response", INFRA),
          Map.entry("presentation", INFRA),
          Map.entry("infrastructure/db/repository", INFRA),
          Map.entry("infrastructure/db/query", INFRA),
          Map.entry("infrastructure/db", INFRA),
          Map.entry("infrastructure", INFRA));

  /** モジュール配下の全ディレクトリに package-info.java が存在することを検証する。 */
  @Test
  void allModuleDirectoriesShouldHavePackageInfo() throws IOException {
    final List<String> violations =
        walkModuleDirectories(
            (dir, list) -> {
              if (!Files.exists(dir.resolve("package-info.java"))) {
                final String relative = SRC_ROOT.relativize(dir).toString().replace('\\', '/');
                list.add(
                    relative
                        + " — package-info.java が存在しない。"
                        + " 修正: "
                        + dir
                        + "/package-info.java を作成し、"
                        + "@NullMarked と適切な Onion アノテーションを付与してください");
              }
            });

    assertThat(violations).as("package-info.java 不足 — 各項目の修正指示に従ってください").isEmpty();
  }

  /** 全パッケージの package-info.java に {@code @NullMarked} が記述されていることを検証する。 */
  @Test
  void allPackagesShouldHaveNullMarked() throws IOException {
    final List<String> violations =
        walkAllDirectories(
            (dir, list) -> {
              final Path packageInfo = dir.resolve("package-info.java");
              final String relative = SRC_ROOT.relativize(dir).toString().replace('\\', '/');
              final String pkg =
                  relative.isEmpty()
                      ? "com.example.demo"
                      : "com.example.demo." + relative.replace('/', '.');

              if (!Files.exists(packageInfo)) {
                list.add(
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
                  list.add(
                      relative
                          + " — package-info.java に @NullMarked が未記述。"
                          + " 修正: "
                          + packageInfo
                          + " に '@NullMarked' アノテーションと"
                          + " 'import org.jspecify.annotations.NullMarked;' を追加してください");
                }
              } catch (IOException exception) {
                list.add(relative + " — package-info.java の読み取りに失敗: " + exception.getMessage());
              }
            });

    assertThat(violations).as("@NullMarked package-info.java の違反 — 各項目の修正指示に従ってください").isEmpty();
  }

  /** レイヤーパッケージに正しい Onion Architecture アノテーションが付与されていることを検証する。 */
  @Test
  void allLayerPackagesShouldHaveCorrectOnionAnnotation() throws IOException {
    final List<String> violations =
        walkAllDirectories(
            (dir, list) -> {
              final String relativePath = SRC_ROOT.relativize(dir).toString().replace('\\', '/');
              for (final Map.Entry<String, String> rule : LAYER_RULES) {
                if (relativePath.endsWith(rule.getKey())) {
                  checkOnionAnnotation(dir, rule.getValue(), list);
                  break;
                }
              }
            });

    assertThat(violations)
        .as("Onion Architecture annotation violations — 各項目の修正指示に従ってください")
        .isEmpty();
  }

  // === 共有ウォーカー ===

  /**
   * SRC_ROOT 配下の全ディレクトリを走査し、各ディレクトリに対してチェックを実行する。
   *
   * @return 検出された違反のリスト
   */
  private List<String> walkAllDirectories(final BiConsumer<Path, List<String>> check)
      throws IOException {
    final List<String> violations = new ArrayList<>();
    try (Stream<Path> dirs = Files.walk(SRC_ROOT)) {
      dirs.filter(Files::isDirectory).forEach(dir -> check.accept(dir, violations));
    }
    return violations;
  }

  /**
   * モジュールルートが存在するディレクトリ配下のみを走査し、各ディレクトリに対してチェックを実行する。
   *
   * @return 検出された違反のリスト
   */
  private List<String> walkModuleDirectories(final BiConsumer<Path, List<String>> check)
      throws IOException {
    final List<String> violations = new ArrayList<>();
    try (Stream<Path> modules = Files.list(SRC_ROOT)) {
      modules
          .filter(Files::isDirectory)
          .filter(this::isModule)
          .forEach(mod -> walkModuleSubDirs(mod, check, violations));
    }
    return violations;
  }

  private boolean isModule(final Path dir) {
    return Files.exists(dir.resolve("package-info.java"));
  }

  private void walkModuleSubDirs(
      final Path mod, final BiConsumer<Path, List<String>> check, final List<String> violations) {
    try (Stream<Path> dirs = Files.walk(mod)) {
      dirs.filter(Files::isDirectory).forEach(dir -> check.accept(dir, violations));
    } catch (IOException exception) {
      violations.add(mod + " — ディレクトリの走査に失敗: " + exception.getMessage());
    }
  }

  // === Onion アノテーションチェック ===

  private void checkOnionAnnotation(
      final Path dir, final String expected, final List<String> violations) {
    final Path packageInfo = dir.resolve("package-info.java");
    final String relative = SRC_ROOT.relativize(dir).toString().replace('\\', '/');
    final String pkg = "com.example.demo." + relative.replace('/', '.');
    final String fqcn = IMPORT_MAP.getOrDefault(expected, "");

    if (!Files.exists(packageInfo)) {
      violations.add(
          relative
              + " — package-info.java が存在しない(期待: "
              + expected
              + ")。"
              + " 修正: "
              + dir
              + "/package-info.java を作成してください。"
              + " 内容例: "
              + expected
              + " @NullMarked package "
              + pkg
              + "; (import: "
              + fqcn
              + ")");
      return;
    }
    try {
      final String content = Files.readString(packageInfo);
      if (!content.contains(expected)) {
        violations.add(
            relative
                + " — package-info.java に "
                + expected
                + " が未記述。"
                + " 修正: "
                + packageInfo
                + " に "
                + expected
                + " を追加してください。"
                + " 内容例: "
                + expected
                + " @NullMarked package "
                + pkg
                + "; (import: "
                + fqcn
                + ")");
      }
    } catch (IOException exception) {
      violations.add(relative + " — package-info.java の読み取りに失敗: " + exception.getMessage());
    }
  }
}
