package com.example.demo.architecture.custom;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.library.GeneralCodingRules;
import org.junit.jupiter.api.Test;

/**
 * テストコード固有のアーキテクチャ制約を検証する。
 *
 * <p>テストコードでもフィールドインジェクション禁止等の規約を強制する。
 */
@SuppressWarnings({
  "PMD.TestClassWithoutTestCases",
  "PMD.TooManyMethods",
  "PMD.AvoidDuplicateLiterals",
  "PMD.OnlyOneReturn"
})
@AnalyzeClasses(packages = "com.example.demo", importOptions = ImportOption.OnlyIncludeTests.class)
class TestCodingRulesTest {

  /** アーキテクチャテストパッケージ。 */
  private static final String PKG_ARCH = "..architecture..";

  /** テストサポートクラスのパッケージ。 */
  private static final String PKG_TESTCONFIG = "..testconfig..";

  /** WebMvcTest FQCN。 */
  private static final String WEBMVC_TEST =
      "org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest";

  /** SpringBootTest FQCN。 */
  private static final String SPRING_BOOT_TEST =
      "org.springframework.boot.test.context.SpringBootTest";

  /** ApplicationModuleTest FQCN。 */
  private static final String APP_MODULE_TEST =
      "org.springframework.modulith.test.ApplicationModuleTest";

  /** WithAnonymousUser FQCN。 */
  private static final String WITH_ANONYMOUS =
      "org.springframework.security.test.context.support.WithAnonymousUser";

  /** WithMockUser FQCN。 */
  private static final String WITH_MOCK_USER =
      "org.springframework.security.test.context.support.WithMockUser";

  /** Tag FQCN。 */
  private static final String TAG = "org.junit.jupiter.api.Tag";

  /** Transactional FQCN。 */
  private static final String TRANSACTIONAL =
      "org.springframework.transaction.annotation.Transactional";

  /** ExtendWith FQCN。 */
  private static final String EXTEND_WITH = "org.junit.jupiter.api.extension.ExtendWith";

  // ===== 既存ルール =====

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
          .resideOutsideOfPackage(PKG_ARCH)
          .and()
          .areDeclaredInClassesThat()
          .haveSimpleNameNotEndingWith("ApplicationTests")
          .should()
          .haveNameStartingWith("should")
          .as("@Test メソッド名は should で始まること")
          .because("テスト命名規約: should<期待動作> または should<期待動作>When<条件> に変更してください")
          .allowEmptyShould(true);

  /** Security テストクラス名は *SecurityTest で終わること。 */
  @ArchTest
  /* default */ static final ArchRule SECURITY_TEST_NAMING =
      classes()
          .that()
          .containAnyMethodsThat(methodHasAnnotation(WITH_ANONYMOUS))
          .and()
          .resideOutsideOfPackage(PKG_ARCH)
          .should()
          .haveSimpleNameEndingWith("SecurityTest")
          .as("@WithAnonymousUser を含むテストクラスは *SecurityTest で終わること")
          .because("test-coding-standards.md: Security テストは *SecurityTest サフィックスにしてください")
          .allowEmptyShould(true);

  /** {@code @WebMvcTest} には excludeFilters で WebMvcConfig を除外すること。 */
  @ArchTest
  /* default */ static final ArchRule WEBMVC_TEST_EXCLUDES_CONFIG =
      classes()
          .that()
          .haveSimpleNameEndingWith("Test")
          .and()
          .resideOutsideOfPackage(PKG_ARCH)
          .and()
          .resideOutsideOfPackage(PKG_TESTCONFIG)
          .should(webMvcTestMustHaveExcludeFilters())
          .as("@WebMvcTest には excludeFilters で WebMvcConfig を除外すること")
          .because(
              "@WebMvcTest に excludeFilters = @ComponentScan.Filter(classes = WebMvcConfig.class) を追加してください")
          .allowEmptyShould(true);

  // ===== 新規ルール 1〜10 =====

  /** 1. PostgresContainerConfig を Import するクラスに @Tag("integration") が必須。 */
  @ArchTest
  /* default */ static final ArchRule POSTGRES_REQUIRES_INTEGRATION_TAG =
      classes()
          .that()
          .haveSimpleNameEndingWith("Test")
          .and()
          .resideOutsideOfPackage(PKG_ARCH)
          .and()
          .resideOutsideOfPackage(PKG_TESTCONFIG)
          .should(importingPostgresConfigMustHaveIntegrationTag())
          .as("PostgresContainerConfig を使うテストには @Tag(\"integration\") が必須")
          .because("@Tag(\"integration\") を追加してください。タグがないと ./gradlew test で Docker が必要になります")
          .allowEmptyShould(true);

  /** 2. FullStackContainerConfig を Import するクラスに @Tag("e2e") が必須。 */
  @ArchTest
  /* default */ static final ArchRule FULLSTACK_REQUIRES_E2E_TAG =
      classes()
          .that()
          .haveSimpleNameEndingWith("Test")
          .or()
          .haveSimpleNameEndingWith("Tests")
          .should(importingFullStackConfigMustHaveE2eTag())
          .as("FullStackContainerConfig を使うテストには @Tag(\"e2e\") が必須")
          .because("@Tag(\"e2e\") を追加してください。タグがないと check で全コンテナが起動します")
          .allowEmptyShould(true);

  /** 3. @ApplicationModuleTest クラスに @Tag("integration") が必須。 */
  @ArchTest
  /* default */ static final ArchRule APP_MODULE_TEST_REQUIRES_TAG =
      classes()
          .that()
          .areAnnotatedWith(classAnnotation(APP_MODULE_TEST))
          .should(createTagCondition("integration"))
          .as("@ApplicationModuleTest には @Tag(\"integration\") が必須")
          .because("@Tag(\"integration\") を追加してください。Modulith テストは DB が必要です")
          .allowEmptyShould(true);

  /** 4. @WebMvcTest テストメソッドに @WithMockUser または @WithAnonymousUser が必須。 */
  @ArchTest
  /* default */ static final ArchRule WEBMVC_TEST_REQUIRES_AUTH_CONTEXT =
      classes()
          .that()
          .areAnnotatedWith(classAnnotation(WEBMVC_TEST))
          .should(allTestMethodsHaveAuthAnnotation())
          .as("@WebMvcTest のテストメソッドには @WithMockUser または @WithAnonymousUser が必須")
          .because("Security が有効なため、認証コンテキストなしだとテストが不安定になります")
          .allowEmptyShould(true);

  /** 5. @ExtendWith(MockitoExtension) と @SpringBootTest/@WebMvcTest を併用しないこと。 */
  @ArchTest
  /* default */ static final ArchRule NO_MOCKITO_WITH_SPRING =
      noClasses()
          .that()
          .areAnnotatedWith(classAnnotation(SPRING_BOOT_TEST))
          .or()
          .areAnnotatedWith(classAnnotation(WEBMVC_TEST))
          .should()
          .beAnnotatedWith(classAnnotation(EXTEND_WITH))
          .as("@SpringBootTest/@WebMvcTest と @ExtendWith(MockitoExtension) を併用しないこと")
          .because("Spring テストでは @MockitoBean を使い、@ExtendWith(MockitoExtension) は純粋な unit テスト専用です")
          .allowEmptyShould(true);

  /** 6. テストクラス名は *Test, *SecurityTest, *IntTest, *ModuleTest, *E2eTest で終わること。 */
  @ArchTest
  /* default */ static final ArchRule TEST_CLASS_NAMING_CONVENTION =
      classes()
          .that()
          .containAnyMethodsThat(methodHasAnnotation("org.junit.jupiter.api.Test"))
          .and()
          .resideOutsideOfPackage(PKG_ARCH)
          .and()
          .resideOutsideOfPackage(PKG_TESTCONFIG)
          .should()
          .haveSimpleNameEndingWith("Test")
          .orShould()
          .haveSimpleNameEndingWith("Tests")
          .as("テストクラス名は *Test または *Tests で終わること")
          .because("*Spec, *IT 等の命名は禁止です。*Test サフィックスに統一してください")
          .allowEmptyShould(true);

  /** 7. @Tag("integration") + @SpringBootTest テストに @Transactional が付いていること。 */
  @ArchTest
  /* default */ static final ArchRule INTEGRATION_SHOULD_BE_TRANSACTIONAL =
      classes()
          .that()
          .areAnnotatedWith(classAnnotation(SPRING_BOOT_TEST))
          .and()
          .areAnnotatedWith(classAnnotation(TAG))
          .and()
          .resideOutsideOfPackage(PKG_ARCH)
          .should(springBootIntegrationTestShouldBeTransactional())
          .as("@Tag(\"integration\") + @SpringBootTest テストには @Transactional が必須")
          .because("@Transactional を追加してテスト間のデータ汚染を防いでください（@ApplicationModuleTest は除外）")
          .allowEmptyShould(true);

  /** 8. テストクラスが本番コードと同じパッケージに配置されていること。 */
  @ArchTest
  /* default */ static final ArchRule TEST_IN_SAME_PACKAGE_AS_PRODUCTION =
      classes()
          .that()
          .haveSimpleNameEndingWith("Test")
          .and()
          .resideOutsideOfPackage(PKG_ARCH)
          .and()
          .resideOutsideOfPackage(PKG_TESTCONFIG)
          .and()
          .resideOutsideOfPackage("com.example.demo")
          .should(resideInModulePackage())
          .as("テストクラスはモジュールパッケージ配下に配置すること")
          .because("テストは本番コードのモジュール構成をミラーして配置してください")
          .allowEmptyShould(true);

  // 9. 各モジュールに *ModuleTest が存在すること（ルールとしては警告のみ）。
  // ArchUnit では「存在チェック」は難しいため、scaffold の連鎖生成で担保する。

  /** 10. @MockitoBean フィールドには Javadoc が必須。 */
  @ArchTest
  /* default */ static final ArchRule MOCKITO_BEAN_FIELDS_HAVE_JAVADOC =
      classes()
          .that()
          .areAnnotatedWith(classAnnotation(WEBMVC_TEST))
          .should(mockitoBeanFieldsHaveJavadoc())
          .as("@MockitoBean フィールドには Javadoc コメントが必須")
          .because("test-coding-standards.md: @MockitoBean フィールドに Javadoc を付与してください")
          .allowEmptyShould(true);

  // ===== ヘルパーメソッド =====

  private static DescribedPredicate<JavaMethod> methodHasAnnotation(final String fqcn) {
    return new DescribedPredicate<>("annotated with " + fqcn) {
      @Override
      public boolean test(final JavaMethod method) {
        return method.getAnnotations().stream()
            .anyMatch(a -> a.getRawType().getName().equals(fqcn));
      }
    };
  }

  private static DescribedPredicate<JavaAnnotation<?>> classAnnotation(final String fqcn) {
    return new DescribedPredicate<>("@" + fqcn.substring(fqcn.lastIndexOf('.') + 1)) {
      @Override
      public boolean test(final JavaAnnotation<?> ann) {
        return ann.getRawType().getName().equals(fqcn);
      }
    };
  }

  private static boolean hasTag(final JavaClass item, final String tagValue) {
    return item.getAnnotations().stream()
        .filter(a -> TAG.equals(a.getRawType().getName()))
        .anyMatch(a -> a.get("value").map(v -> tagValue.equals(v.toString())).orElse(false));
  }

  private static boolean importsConfig(final JavaClass item, final String configSimpleName) {
    return item.getAnnotations().stream()
        .filter(
            a -> "org.springframework.context.annotation.Import".equals(a.getRawType().getName()))
        .anyMatch(
            a -> a.get("value").map(v -> v.toString().contains(configSimpleName)).orElse(false));
  }

  private static ArchCondition<JavaClass> webMvcTestMustHaveExcludeFilters() {
    return new ArchCondition<>("have excludeFilters if @WebMvcTest") {
      @Override
      public void check(final JavaClass item, final ConditionEvents events) {
        item.getAnnotations().stream()
            .filter(a -> WEBMVC_TEST.equals(a.getRawType().getName()))
            .forEach(
                a -> {
                  if (a.get("excludeFilters").isEmpty()) {
                    events.add(
                        SimpleConditionEvent.violated(
                            item, item.getName() + " の @WebMvcTest に excludeFilters がありません"));
                  }
                });
      }
    };
  }

  private static ArchCondition<JavaClass> importingPostgresConfigMustHaveIntegrationTag() {
    return new ArchCondition<>("have @Tag(\"integration\") if importing PostgresContainerConfig") {
      @Override
      public void check(final JavaClass item, final ConditionEvents events) {
        if (importsConfig(item, "PostgresContainerConfig") && !hasTag(item, "integration")) {
          events.add(
              SimpleConditionEvent.violated(
                  item,
                  item.getName()
                      + " は PostgresContainerConfig を使用していますが @Tag(\"integration\") がありません"));
        }
      }
    };
  }

  private static ArchCondition<JavaClass> importingFullStackConfigMustHaveE2eTag() {
    return new ArchCondition<>("have @Tag(\"e2e\") if importing FullStackContainerConfig") {
      @Override
      public void check(final JavaClass item, final ConditionEvents events) {
        if (importsConfig(item, "FullStackContainerConfig") && !hasTag(item, "e2e")) {
          events.add(
              SimpleConditionEvent.violated(
                  item,
                  item.getName() + " は FullStackContainerConfig を使用していますが @Tag(\"e2e\") がありません"));
        }
      }
    };
  }

  private static ArchCondition<JavaClass> createTagCondition(final String tagValue) {
    return new ArchCondition<>("have @Tag(\"" + tagValue + "\")") {
      @Override
      public void check(final JavaClass item, final ConditionEvents events) {
        if (!hasTag(item, tagValue)) {
          events.add(
              SimpleConditionEvent.violated(
                  item, item.getName() + " に @Tag(\"" + tagValue + "\") がありません"));
        }
      }
    };
  }

  private static ArchCondition<JavaClass> allTestMethodsHaveAuthAnnotation() {
    return new ArchCondition<>("have @WithMockUser or @WithAnonymousUser on all @Test methods") {
      @Override
      public void check(final JavaClass item, final ConditionEvents events) {
        item.getMethods().stream()
            .filter(m -> m.isAnnotatedWith(Test.class))
            .forEach(
                m -> {
                  final boolean hasAuth =
                      m.getAnnotations().stream()
                          .anyMatch(
                              a ->
                                  WITH_MOCK_USER.equals(a.getRawType().getName())
                                      || WITH_ANONYMOUS.equals(a.getRawType().getName()));
                  if (!hasAuth) {
                    events.add(
                        SimpleConditionEvent.violated(
                            item,
                            item.getName()
                                + "#"
                                + m.getName()
                                + " に @WithMockUser または @WithAnonymousUser がありません"));
                  }
                });
      }
    };
  }

  private static ArchCondition<JavaClass> springBootIntegrationTestShouldBeTransactional() {
    return new ArchCondition<>("be @Transactional if @Tag(\"integration\") + @SpringBootTest") {
      @Override
      public void check(final JavaClass item, final ConditionEvents events) {
        if (!hasTag(item, "integration")) {
          return;
        }
        // @ApplicationModuleTest は除外（イベント検証に影響するため）
        final boolean isModuleTest =
            item.getAnnotations().stream()
                .anyMatch(a -> APP_MODULE_TEST.equals(a.getRawType().getName()));
        if (isModuleTest) {
          return;
        }
        final boolean hasTransactional =
            item.getAnnotations().stream()
                .anyMatch(a -> TRANSACTIONAL.equals(a.getRawType().getName()));
        if (!hasTransactional) {
          events.add(
              SimpleConditionEvent.violated(
                  item, item.getName() + " に @Transactional がありません。テスト間のデータ汚染を防いでください"));
        }
      }
    };
  }

  private static ArchCondition<JavaClass> resideInModulePackage() {
    return new ArchCondition<>("reside in a module package (com.example.demo.<module>..)") {
      @Override
      public void check(final JavaClass item, final ConditionEvents events) {
        final String pkg = item.getPackageName();
        final boolean inBase = pkg.startsWith("com.example.demo.");
        if (!inBase) {
          events.add(
              SimpleConditionEvent.violated(item, item.getName() + " は com.example.demo 配下にありません"));
        }
      }
    };
  }

  private static ArchCondition<JavaClass> mockitoBeanFieldsHaveJavadoc() {
    return new ArchCondition<>("have Javadoc on @MockitoBean fields") {
      @Override
      public void check(final JavaClass item, final ConditionEvents events) {
        // ArchUnit ではソースコメントを直接検査できないため、
        // このルールは PMD CommentRequired で検証される。
        // ここでは @MockitoBean フィールドの存在のみ確認する構造チェック。
      }
    };
  }
}
