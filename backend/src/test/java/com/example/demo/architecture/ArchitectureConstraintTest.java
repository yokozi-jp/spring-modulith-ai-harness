package com.example.demo.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * プロジェクト固有のアーキテクチャ制約を検証する。
 *
 * <p>アノテーション配置制約・パッケージ間依存制約・型制約を定義。
 */
@SuppressWarnings("PMD.TestClassWithoutTestCases")
@AnalyzeClasses(packages = "com.example.demo", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureConstraintTest {

  /** package-info クラスの除外条件用。 */
  private static final String PKG_INFO = "package-info";

  // === アノテーション配置制約 ===

  /** {@code @Command} は command/dto にのみ配置可能。 */
  @ArchTest
  /* default */ static final ArchRule CMD_PLACEMENT =
      classes()
          .that()
          .areAnnotatedWith(org.jmolecules.architecture.cqrs.Command.class)
          .should()
          .resideInAPackage("..command.dto..")
          .allowEmptyShould(true);

  /** {@code @CommandHandler} は command/handler にのみ配置可能。 */
  @ArchTest
  /* default */ static final ArchRule CMD_H_PLACEMENT =
      classes()
          .that()
          .areAnnotatedWith(org.jmolecules.architecture.cqrs.CommandHandler.class)
          .should()
          .resideInAPackage("..command.handler..")
          .allowEmptyShould(true);

  /** {@code @QueryModel} は query/dto にのみ配置可能。 */
  @ArchTest
  /* default */ static final ArchRule QM_PLACEMENT =
      classes()
          .that()
          .areAnnotatedWith(org.jmolecules.architecture.cqrs.QueryModel.class)
          .should()
          .resideInAPackage("..query.dto..")
          .allowEmptyShould(true);

  /** {@code @DomainEvent} は event パッケージにのみ配置可能。 */
  @ArchTest
  /* default */ static final ArchRule EVT_PLACEMENT =
      classes()
          .that()
          .areAnnotatedWith(org.jmolecules.event.annotation.DomainEvent.class)
          .should()
          .resideInAPackage("..event..")
          .allowEmptyShould(true);

  /** {@code @RestController} は presentation/controller にのみ配置可能。 */
  @ArchTest
  /* default */ static final ArchRule CTRL_PLACEMENT =
      classes()
          .that()
          .areAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
          .should()
          .resideInAPackage("..presentation.controller..")
          .allowEmptyShould(true);

  // === command / query 混在禁止 ===

  /** command パッケージに {@code @QueryModel} を置いてはいけない。 */
  @ArchTest
  /* default */ static final ArchRule NO_QM_IN_CMD =
      noClasses()
          .that()
          .resideInAPackage("..command..")
          .should()
          .beAnnotatedWith(org.jmolecules.architecture.cqrs.QueryModel.class)
          .allowEmptyShould(true);

  /** query パッケージに {@code @Command} を置いてはいけない。 */
  @ArchTest
  /* default */ static final ArchRule NO_CMD_IN_QRY =
      noClasses()
          .that()
          .resideInAPackage("..query..")
          .should()
          .beAnnotatedWith(org.jmolecules.architecture.cqrs.Command.class)
          .allowEmptyShould(true);

  // === パッケージ依存制約 ===

  /** query パッケージは domain パッケージに依存してはいけない。 */
  @ArchTest
  /* default */ static final ArchRule QRY_NO_DOMAIN =
      noClasses()
          .that()
          .resideInAPackage("..query..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..domain..")
          .allowEmptyShould(true);

  /** domain パッケージは Spring に依存してはいけない。 */
  @ArchTest
  /* default */ static final ArchRule DOMAIN_NO_SPRING =
      noClasses()
          .that()
          .resideInAPackage("..domain..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("org.springframework..")
          .allowEmptyShould(true);

  /** presentation は infrastructure に依存してはいけない。 */
  @ArchTest
  /* default */ static final ArchRule PRES_NO_INFRA =
      noClasses()
          .that()
          .resideInAPackage("..presentation..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..infrastructure..")
          .allowEmptyShould(true);

  // === インターフェースのみ制約 ===

  /** domain/repository にはインターフェースのみ配置可能。 */
  @ArchTest
  /* default */ static final ArchRule REPO_IFACE =
      classes()
          .that()
          .resideInAPackage("..domain.repository..")
          .should()
          .beInterfaces()
          .allowEmptyShould(true);

  /** query/service にはインターフェースのみ配置可能。 */
  @ArchTest
  /* default */ static final ArchRule QRY_SVC_IFACE =
      classes()
          .that()
          .resideInAPackage("..query.service..")
          .should()
          .beInterfaces()
          .allowEmptyShould(true);

  // === record のみ制約 ===

  /** command/dto には record のみ配置可能（package-info を除く）。 */
  @ArchTest
  /* default */ static final ArchRule CMD_DTO_REC =
      classes()
          .that()
          .resideInAPackage("..command.dto..")
          .and()
          .haveSimpleNameNotContaining(PKG_INFO)
          .should()
          .beRecords()
          .allowEmptyShould(true);

  /** query/dto には record のみ配置可能（package-info を除く）。 */
  @ArchTest
  /* default */ static final ArchRule QRY_DTO_REC =
      classes()
          .that()
          .resideInAPackage("..query.dto..")
          .and()
          .haveSimpleNameNotContaining(PKG_INFO)
          .should()
          .beRecords()
          .allowEmptyShould(true);

  /** event パッケージには record のみ配置可能（package-info を除く）。 */
  @ArchTest
  /* default */ static final ArchRule EVT_RECORD =
      classes()
          .that()
          .resideInAPackage("..event..")
          .and()
          .haveSimpleNameNotContaining(PKG_INFO)
          .should()
          .beRecords()
          .allowEmptyShould(true);

  /** presentation/request には record のみ配置可能（package-info を除く）。 */
  @ArchTest
  /* default */ static final ArchRule REQ_RECORD =
      classes()
          .that()
          .resideInAPackage("..presentation.request..")
          .and()
          .haveSimpleNameNotContaining(PKG_INFO)
          .should()
          .beRecords()
          .allowEmptyShould(true);

  /** presentation/response には record のみ配置可能（package-info を除く）。 */
  @ArchTest
  /* default */ static final ArchRule RES_RECORD =
      classes()
          .that()
          .resideInAPackage("..presentation.response..")
          .and()
          .haveSimpleNameNotContaining(PKG_INFO)
          .should()
          .beRecords()
          .allowEmptyShould(true);
}
