package com.example.demo.architecture.custom.policy;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * CQRS アーキテクチャポリシー。
 *
 * <p>アノテーション配置制約・Command/Query 混在禁止・query→domain 依存禁止を定義する。
 */
public final class CqrsPolicy {

  /** {@code @Command} は command/dto にのみ配置可能。 */
  @ArchTest
  /* default */ static final ArchRule CMD_PLACEMENT =
      classes()
          .that()
          .areAnnotatedWith(org.jmolecules.architecture.cqrs.Command.class)
          .should()
          .resideInAPackage("..command.dto..")
          .as("@Command アノテーション付きクラスは ..command.dto.. パッケージにのみ配置可能")
          .because("CQRS 制約: 対象クラスを ..command.dto.. パッケージに移動してください")
          .allowEmptyShould(true);

  /** {@code @QueryModel} は query/dto にのみ配置可能。 */
  @ArchTest
  /* default */ static final ArchRule QM_PLACEMENT =
      classes()
          .that()
          .areAnnotatedWith(org.jmolecules.architecture.cqrs.QueryModel.class)
          .should()
          .resideInAPackage("..query.dto..")
          .as("@QueryModel アノテーション付きクラスは ..query.dto.. パッケージにのみ配置可能")
          .because("CQRS 制約: 対象クラスを ..query.dto.. パッケージに移動してください")
          .allowEmptyShould(true);

  /** {@code @DomainEvent} は event パッケージにのみ配置可能。 */
  @ArchTest
  /* default */ static final ArchRule EVT_PLACEMENT =
      classes()
          .that()
          .areAnnotatedWith(org.jmolecules.event.annotation.DomainEvent.class)
          .should()
          .resideInAPackage("..event..")
          .as("@DomainEvent アノテーション付きクラスは ..event.. パッケージにのみ配置可能")
          .because("イベント制約: 対象クラスを ..event.. パッケージに移動してください")
          .allowEmptyShould(true);

  /** {@code @RestController} は presentation/controller にのみ配置可能。 */
  @ArchTest
  /* default */ static final ArchRule CTRL_PLACEMENT =
      classes()
          .that()
          .areAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
          .should()
          .resideInAPackage("..presentation.controller..")
          .as("@RestController アノテーション付きクラスは ..presentation.controller.. パッケージにのみ配置可能")
          .because("レイヤー制約: 対象クラスを ..presentation.controller.. パッケージに移動してください")
          .allowEmptyShould(true);

  /** command パッケージに {@code @QueryModel} を置いてはいけない。 */
  @ArchTest
  /* default */ static final ArchRule NO_QM_IN_CMD =
      noClasses()
          .that()
          .resideInAPackage("..command..")
          .should()
          .beAnnotatedWith(org.jmolecules.architecture.cqrs.QueryModel.class)
          .as("command パッケージに @QueryModel を配置してはいけない")
          .because("CQRS 分離違反: @QueryModel を ..query.dto.. パッケージに移動するか、アノテーションを除去してください")
          .allowEmptyShould(true);

  /** query パッケージに {@code @Command} を置いてはいけない。 */
  @ArchTest
  /* default */ static final ArchRule NO_CMD_IN_QRY =
      noClasses()
          .that()
          .resideInAPackage("..query..")
          .should()
          .beAnnotatedWith(org.jmolecules.architecture.cqrs.Command.class)
          .as("query パッケージに @Command を配置してはいけない")
          .because("CQRS 分離違反: @Command を ..command.dto.. パッケージに移動するか、アノテーションを除去してください")
          .allowEmptyShould(true);

  /** query パッケージはプロジェクトの domain パッケージに依存してはいけない。 */
  @ArchTest
  /* default */ static final ArchRule QRY_NO_DOMAIN =
      noClasses()
          .that()
          .resideInAPackage("..query..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("com.example.demo..domain..")
          .as("query パッケージは domain パッケージに依存してはいけない")
          .because("CQRS 依存制約: query から domain への import を除去し、query 用の DTO/インターフェースを使用してください")
          .allowEmptyShould(true);

  private CqrsPolicy() {}
}
