package com.example.demo.architecture.framework;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.DependencyRules;
import com.tngtech.archunit.library.GeneralCodingRules;
import com.tngtech.archunit.library.ProxyRules;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

/**
 * ArchUnit 本体が提供する汎用コーディングルール・依存ルール・プロキシルールを検証する。
 *
 * <p>PMD / SpotBugs と重複しないルールを中心に適用。
 */
@SuppressWarnings("PMD.TestClassWithoutTestCases")
@AnalyzeClasses(packages = "com.example.demo", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchUnitBuiltInRulesTest {

  // === GeneralCodingRules ===

  /** java.util.logging の使用を禁止し SLF4J に統一する。 */
  @ArchTest
  /* default */ static final ArchRule NO_JUL =
      GeneralCodingRules.NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING
          .because("java.util.logging の import を org.slf4j.Logger/LoggerFactory に置換してください")
          .allowEmptyShould(true);

  /** フィールドインジェクション禁止。コンストラクタインジェクションを使用する。 */
  @ArchTest
  /* default */ static final ArchRule NO_FIELD_DI =
      GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION
          .because("@Autowired フィールドを削除し、@RequiredArgsConstructor + final フィールドに変更してください")
          .allowEmptyShould(true);

  /** {@code @Deprecated} API の使用を禁止する。 */
  @ArchTest
  /* default */ static final ArchRule NO_DEPRECATED =
      GeneralCodingRules.DEPRECATED_API_SHOULD_NOT_BE_USED
          .because("@Deprecated API の呼び出しを非推奨でない代替 API に置換してください")
          .allowEmptyShould(true);

  // === DependencyRules ===

  /** 子パッケージから親パッケージへの依存を禁止する。 */
  @ArchTest
  /* default */ static final ArchRule NO_UPPER_DEPS =
      DependencyRules.NO_CLASSES_SHOULD_DEPEND_UPPER_PACKAGES
          .because("子パッケージから親パッケージへの import を除去し、依存方向を修正してください")
          .allowEmptyShould(true);

  // === ProxyRules ===

  /** 同一クラス内での {@code @Transactional} メソッド直接呼び出しを禁止する。 */
  @ArchTest
  /* default */ static final ArchRule NO_SELF_TX =
      ProxyRules
          .no_classes_should_directly_call_other_methods_declared_in_the_same_class_that_are_annotated_with(
              Transactional.class)
          .because(
              "@Transactional メソッドの呼び出しを別クラスに委譲してください" + "（同一クラス内呼び出しでは Spring AOP プロキシが機能しません）")
          .allowEmptyShould(true);

  /** 同一クラス内での {@code @Cacheable} メソッド直接呼び出しを禁止する。 */
  @ArchTest
  /* default */ static final ArchRule NO_SELF_CACHE =
      ProxyRules
          .no_classes_should_directly_call_other_methods_declared_in_the_same_class_that_are_annotated_with(
              Cacheable.class)
          .because("@Cacheable メソッドの呼び出しを別クラスに委譲してください" + "（同一クラス内呼び出しでは Spring AOP プロキシが機能しません）")
          .allowEmptyShould(true);

  /** 同一クラス内での {@code @Async} メソッド直接呼び出しを禁止する。 */
  @ArchTest
  /* default */ static final ArchRule NO_SELF_ASYNC =
      ProxyRules
          .no_classes_should_directly_call_other_methods_declared_in_the_same_class_that_are_annotated_with(
              Async.class)
          .because("@Async メソッドの呼び出しを別クラスに委譲してください" + "（同一クラス内呼び出しでは Spring AOP プロキシが機能しません）")
          .allowEmptyShould(true);

  // === Lombok 禁止ルール ===

  /** 本番コードで {@code @Data} の使用を禁止する。 */
  @ArchTest
  /* default */ static final ArchRule NO_DATA =
      noClasses()
          .should()
          .beAnnotatedWith("lombok.Data")
          .as("@Data の使用は禁止")
          .because(
              "@Data を除去し、@Getter + @EqualsAndHashCode + @ToString に置換してください。"
                  + " 不変クラスには @Value を使用してください")
          .allowEmptyShould(true);

  /** {@code @Setter} の使用を禁止する。 */
  @ArchTest
  /* default */ static final ArchRule NO_SETTER =
      noClasses()
          .should()
          .beAnnotatedWith("lombok.Setter")
          .as("@Setter の使用は禁止")
          .because("@Setter を除去してください。" + " フィールドへの書き込みが必要な場合は明示的な setter メソッドか @Builder を使用してください")
          .allowEmptyShould(true);
}
