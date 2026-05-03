package com.example.demo.architecture.custom;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.GeneralCodingRules;

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
}
