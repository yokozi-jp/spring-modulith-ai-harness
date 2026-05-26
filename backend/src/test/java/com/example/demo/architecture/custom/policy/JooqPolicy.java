package com.example.demo.architecture.custom.policy;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

/**
 * jOOQ 型漏洩防止ポリシー。
 *
 * <p>jOOQ の型を infrastructure 層に閉じ込め、上位層への漏洩を禁止する。
 */
public final class JooqPolicy {

  /** presentation 層から jOOQ への依存を禁止する。 */
  @ArchTest
  /* default */ static final ArchRule PRES_NO_JOOQ =
      noClasses()
          .that()
          .resideInAPackage("..presentation..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("org.jooq..")
          .as("presentation は jOOQ に依存してはいけない")
          .because("jOOQ 型漏洩防止: jOOQ の Record/DSLContext を infrastructure 層に閉じ込めてください")
          .allowEmptyShould(true);

  /** application 層から jOOQ への依存を禁止する。 */
  @ArchTest
  /* default */ static final ArchRule APP_NO_JOOQ =
      noClasses()
          .that()
          .resideInAPackage("..application..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("org.jooq..")
          .as("application は jOOQ に依存してはいけない")
          .because("jOOQ 型漏洩防止: jOOQ の Record/DSLContext を infrastructure 層に閉じ込めてください")
          .allowEmptyShould(true);

  /** domain 層から jOOQ への依存を禁止する。 */
  @ArchTest
  /* default */ static final ArchRule DOMAIN_NO_JOOQ =
      noClasses()
          .that()
          .resideInAPackage("..domain..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("org.jooq..")
          .as("domain は jOOQ に依存してはいけない")
          .because("jOOQ 型漏洩防止: jOOQ の Record/DSLContext を infrastructure 層に閉じ込めてください")
          .allowEmptyShould(true);

  /**
   * SoftDeleteCondition 以外のクラスから DELETED_AT フィールドへの直接アクセスを禁止する。
   *
   * <p>論理削除フィルタは必ず {@code SoftDeleteCondition.notDeleted(TABLE)} を使用すること。
   */
  @ArchTest
  /* default */ static final ArchRule NO_DIRECT_DELETED_AT_ACCESS =
      noClasses()
          .that()
          .doNotHaveSimpleName("SoftDeleteCondition")
          .should(deletedAtAccessCondition())
          .as("SoftDeleteCondition 以外から DELETED_AT フィールドに直接アクセスしてはいけない")
          .because(
              "論理削除フィルタ: SoftDeleteCondition.notDeleted(TABLE) を使用してください。"
                  + " 直接 DELETED_AT を参照するとフィルタ忘れの原因になります")
          .allowEmptyShould(true);

  private static ArchCondition<JavaClass> deletedAtAccessCondition() {
    return new ArchCondition<>("not access DELETED_AT field directly") {
      @Override
      public void check(JavaClass item, ConditionEvents events) {
        item.getFieldAccessesFromSelf().stream()
            .filter(
                access ->
                    "DELETED_AT".equals(access.getTarget().getName())
                        && access.getTarget().getOwner().getPackageName().contains(".jooq."))
            .forEach(
                access ->
                    events.add(
                        SimpleConditionEvent.violated(
                            item,
                            item.getName()
                                + " が DELETED_AT に直接アクセスしています ("
                                + access.getSourceCodeLocation()
                                + ")")));
      }
    };
  }

  private JooqPolicy() {}
}
