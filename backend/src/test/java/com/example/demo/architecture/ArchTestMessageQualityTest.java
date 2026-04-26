package com.example.demo.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * アーキテクチャテストのエラーメッセージ品質を検証するメタテスト。
 *
 * <p>各テストが NG 時に「何が違反・どのクラス/ファイルで・どう直すか」を出力していることを保証する。
 *
 * <p>ソースコードを解析し、ArchUnit ルールに {@code .because()} が設定されていること、 violations メッセージに修正指示が含まれていることを検証する。
 */
class ArchTestMessageQualityTest {

  /** アーキテクチャテストのソースルート。 */
  private static final Path TEST_SRC = Path.of("src/test/java/com/example/demo/architecture");

  /** ArchUnit ルールを持つテストソース一覧。 */
  private static final List<Path> ARCH_RULE_SOURCES =
      List.of(
          TEST_SRC.resolve("custom/CustomArchRulesTest.java"),
          TEST_SRC.resolve("framework/ArchUnitBuiltInRulesTest.java"),
          TEST_SRC.resolve("framework/JMoleculesRulesTest.java"));

  /** violations メッセージに修正指示を含むべきテストソース一覧。 */
  private static final List<Path> VIOLATION_SOURCES =
      List.of(
          TEST_SRC.resolve("packageinfo/NullMarkedPackageInfoTest.java"),
          TEST_SRC.resolve("packageinfo/OnionRingAnnotationTest.java"),
          TEST_SRC.resolve("packageinfo/ModuleStructureTest.java"),
          TEST_SRC.resolve("modulith/ModularStructureTest.java"));

  /** {@code @ArchTest} フィールド名を抽出するパターン。 */
  private static final Pattern FIELD_PATTERN =
      Pattern.compile("static\\s+final\\s+ArchRule\\s+(\\w+)");

  /** 全 ArchUnit ルールソースで、各 {@code ArchRule} フィールド定義に {@code .because(} が含まれていることを検証する。 */
  @Test
  @SuppressWarnings("PMD.UnitTestContainsTooManyAsserts")
  void allArchRulesShouldHaveBecause() throws IOException {
    final List<String> violations = new ArrayList<>();

    for (final Path source : ARCH_RULE_SOURCES) {
      checkBecauseInSource(source, violations);
    }

    assertThat(violations)
        .as("全 ArchRule フィールドに .because(\"修正方法\") が必要です。" + " NG 時に AI が修正方法を把握できるようにしてください")
        .isEmpty();
  }

  /** violations メッセージを生成するテストソースに「修正:」キーワードが含まれていることを検証する。 */
  @Test
  void allViolationMessagesShouldContainFixInstruction() throws IOException {
    final List<String> violations = new ArrayList<>();

    for (final Path source : VIOLATION_SOURCES) {
      final String content = Files.readString(source);
      if (!content.contains("修正:")) {
        violations.add(
            source.getFileName()
                + " — violations メッセージに '修正:' が含まれていません。"
                + " NG 時に具体的な修正手順を出力してください");
      }
    }

    assertThat(violations).as("violations メッセージに修正指示が含まれていることの検証").isEmpty();
  }

  /**
   * ソースファイル内の各 ArchRule フィールド定義ブロックに {@code .because(} が含まれているか検証する。
   *
   * <p>フィールド定義の開始（{@code static final ArchRule FIELD_NAME}）からセミコロンまでを 1 ブロックとして抽出し、 {@code
   * .because(} の有無を確認する。
   */
  private void checkBecauseInSource(final Path source, final List<String> violations)
      throws IOException {
    final String content;
    try (Stream<String> lines = Files.lines(source)) {
      content = String.join("\n", lines.toList());
    }
    final String fileName = String.valueOf(source.getFileName());

    final Matcher fieldMatcher = FIELD_PATTERN.matcher(content);
    while (fieldMatcher.find()) {
      final String fieldName = fieldMatcher.group(1);
      final int start = fieldMatcher.start();
      final int semicolon = content.indexOf(';', start);
      if (semicolon < 0) {
        continue;
      }
      final String block = content.substring(start, semicolon);
      if (!block.contains(".because(")) {
        violations.add(
            fileName + "#" + fieldName + " — .because() が未設定です。" + " .because(\"修正方法\") を追加してください");
      }
    }
  }
}
