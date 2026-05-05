package com.example.demo.architecture.custom.policy;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

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

  private JooqPolicy() {}
}
