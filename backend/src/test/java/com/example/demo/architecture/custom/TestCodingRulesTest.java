package com.example.demo.architecture.custom;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.GeneralCodingRules;
import org.junit.jupiter.api.Test;

/**
 * テストコード固有のアーキテクチャ制約を検証する。
 *
 * <p>テストコードでもフィールドインジェクション禁止等の規約を強制する。
 */
@SuppressWarnings("PMD.TestClassWithoutTestCases")
@AnalyzeClasses(packages = "com.example.demo", importOptions = ImportOption.OnlyIncludeTests.class)
class TestCodingRulesTest {

  /** テストコードでもフィールドインジェクション禁止。 */
  @ArchTest
  /* default */ static final ArchRule NO_FIELD_DI_IN_TESTS =
      GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION
          .because(
              "テストでも @Autowired フィールドを使わず、" + "コンストラクタインジェクション（@RequiredArgsConstructor）を使用してください")
          .allowEmptyShould(true);

  /** テストメソッド名は should で始まること。 */
  @ArchTest
  /* default */ static final ArchRule TEST_METHOD_NAMING =
      methods()
          .that()
          .areAnnotatedWith(Test.class)
          .and()
          .areDeclaredInClassesThat()
          .resideOutsideOfPackage("..architecture..")
          .and()
          .areDeclaredInClassesThat()
          .haveSimpleNameNotEndingWith("ApplicationTests")
          .should()
          .haveNameStartingWith("should")
          .as("@Test メソッド名は should で始まること（アーキテクチャテスト・スモークテストを除く）")
          .because("テスト命名規約: メソッド名を should<期待動作> または" + " should<期待動作>When<条件> に変更してください")
          .allowEmptyShould(true);
}
