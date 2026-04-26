package com.example.demo.architecture.custom;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates;
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
class CustomArchRulesTest {

  /** package-info クラスの除外条件用。 */
  private static final String PKG_INFO = "package-info";

  /** {@code @CommandHandler} メソッド判定用。 */
  @SuppressWarnings("unchecked")
  private static final DescribedPredicate<JavaMethod> HAS_CMD_HANDLER =
      (DescribedPredicate<JavaMethod>)
          (DescribedPredicate<?>)
              Predicates.annotatedWith(org.jmolecules.architecture.cqrs.CommandHandler.class);

  /** {@code @ApplicationModuleListener} メソッド判定用。 */
  @SuppressWarnings("unchecked")
  private static final DescribedPredicate<JavaMethod> HAS_MOD_LISTENER =
      (DescribedPredicate<JavaMethod>)
          (DescribedPredicate<?>)
              Predicates.annotatedWith(
                  org.springframework.modulith.events.ApplicationModuleListener.class);

  // === アノテーション配置制約 ===

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

  /** {@code @CommandHandler} は command/handler にのみ配置可能。 */
  @ArchTest
  /* default */ static final ArchRule CMD_H_PLACEMENT =
      classes()
          .that()
          .areAnnotatedWith(org.jmolecules.architecture.cqrs.CommandHandler.class)
          .should()
          .resideInAPackage("..command.handler..")
          .as("@CommandHandler アノテーション付きクラスは ..command.handler.. パッケージにのみ配置可能")
          .because("CQRS 制約: 対象クラスを ..command.handler.. パッケージに移動してください")
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

  // === command / query 混在禁止 ===

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
          .as("query パッケージは domain パッケージに依存してはいけない")
          .because("CQRS 依存制約: query から domain への import を除去し、query 用の DTO/インターフェースを使用してください")
          .allowEmptyShould(true);

  /**
   * domain パッケージは Spring に依存してはいけない。
   *
   * <p>domain/service と domain/repository は jMolecules ByteBuddy プラグインにより Spring ステレオタイプ
   * アノテーションがバイトコードレベルで付与されるため除外する。
   */
  @ArchTest
  /* default */ static final ArchRule DOMAIN_NO_SPRING =
      noClasses()
          .that()
          .resideInAPackage("..domain..")
          .and()
          .resideOutsideOfPackage("..domain.service..")
          .and()
          .resideOutsideOfPackage("..domain.repository..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("org.springframework..")
          .as("domain パッケージ(service/repository 除く)は Spring に依存してはいけない")
          .because(
              "DDD 制約: domain 内の Spring import を除去してください。DI は jMolecules ByteBuddy 経由で自動付与されます")
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
          .as("presentation は infrastructure に依存してはいけない")
          .because("レイヤー制約: presentation から infrastructure への直接参照を除去し、application 層経由にしてください")
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
          .as("domain/repository にはインターフェースのみ配置可能")
          .because("実装クラスを infrastructure/db/repository に移動してください")
          .allowEmptyShould(true);

  /** query/service にはインターフェースのみ配置可能。 */
  @ArchTest
  /* default */ static final ArchRule QRY_SVC_IFACE =
      classes()
          .that()
          .resideInAPackage("..query.service..")
          .should()
          .beInterfaces()
          .as("query/service にはインターフェースのみ配置可能")
          .because("実装クラスを infrastructure/db/query に移動してください")
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
          .as("command/dto には record のみ配置可能(package-info 除く)")
          .because("対象クラスを record に変換してください (例: public record XxxCommand(...))")
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
          .as("query/dto には record のみ配置可能(package-info 除く)")
          .because("対象クラスを record に変換してください (例: public record XxxQuery(...))")
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
          .as("event パッケージには record のみ配置可能(package-info 除く)")
          .because("対象クラスを record に変換してください (例: public record XxxEvent(...))")
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
          .as("presentation/request には record のみ配置可能(package-info 除く)")
          .because("対象クラスを record に変換してください (例: public record XxxRequest(...))")
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
          .as("presentation/response には record のみ配置可能(package-info 除く)")
          .because("対象クラスを record に変換してください (例: public record XxxResponse(...))")
          .allowEmptyShould(true);

  // === DDD 型制約 ===

  /** aggregate パッケージには AggregateRoot 実装のみ配置可能。 */
  @ArchTest
  /* default */ static final ArchRule AGG_TYPE =
      classes()
          .that()
          .resideInAPackage("..model.aggregate..")
          .and()
          .haveSimpleNameNotContaining(PKG_INFO)
          .should()
          .beAssignableTo(org.jmolecules.ddd.types.AggregateRoot.class)
          .as("model/aggregate には AggregateRoot 実装のみ配置可能(package-info 除く)")
          .because("対象クラスに AggregateRoot<T, ID> を実装するか、適切なパッケージに移動してください")
          .allowEmptyShould(true);

  /** entity パッケージには Entity 実装のみ配置可能。 */
  @ArchTest
  /* default */ static final ArchRule ENTITY_TYPE =
      classes()
          .that()
          .resideInAPackage("..model.entity..")
          .and()
          .haveSimpleNameNotContaining(PKG_INFO)
          .should()
          .beAssignableTo(org.jmolecules.ddd.types.Entity.class)
          .as("model/entity には Entity 実装のみ配置可能(package-info 除く)")
          .because("対象クラスに Entity<T, ID> を実装するか、適切なパッケージに移動してください")
          .allowEmptyShould(true);

  /** valueobject/identifier パッケージには Identifier 実装のみ配置可能。 */
  @ArchTest
  /* default */ static final ArchRule ID_TYPE =
      classes()
          .that()
          .resideInAPackage("..valueobject.identifier..")
          .and()
          .haveSimpleNameNotContaining(PKG_INFO)
          .should()
          .beAssignableTo(org.jmolecules.ddd.types.Identifier.class)
          .as("valueobject/identifier には Identifier 実装のみ配置可能(package-info 除く)")
          .because("対象クラスに Identifier を実装するか、適切なパッケージに移動してください")
          .allowEmptyShould(true);

  // === 逆方向配置制約（型 → パッケージ） ===

  /** AggregateRoot 実装は aggregate パッケージにのみ配置可能。 */
  @ArchTest
  /* default */ static final ArchRule AGG_IN_PKG =
      classes()
          .that()
          .areAssignableTo(org.jmolecules.ddd.types.AggregateRoot.class)
          .should()
          .resideInAPackage("..model.aggregate..")
          .as("AggregateRoot 実装は ..model.aggregate.. パッケージにのみ配置可能")
          .because("対象クラスを ..model.aggregate.. パッケージに移動してください")
          .allowEmptyShould(true);

  /** Entity 実装は entity パッケージにのみ配置可能。 */
  @ArchTest
  /* default */ static final ArchRule ENT_IN_PKG =
      classes()
          .that()
          .areAssignableTo(org.jmolecules.ddd.types.Entity.class)
          .and()
          .areNotAssignableTo(org.jmolecules.ddd.types.AggregateRoot.class)
          .should()
          .resideInAPackage("..model.entity..")
          .as("Entity 実装(AggregateRoot 除く)は ..model.entity.. パッケージにのみ配置可能")
          .because("対象クラスを ..model.entity.. パッケージに移動してください")
          .allowEmptyShould(true);

  /** Identifier 実装は valueobject/identifier にのみ配置可能。 */
  @ArchTest
  /* default */ static final ArchRule ID_IN_PKG =
      classes()
          .that()
          .areAssignableTo(org.jmolecules.ddd.types.Identifier.class)
          .should()
          .resideInAPackage("..valueobject.identifier..")
          .as("Identifier 実装は ..valueobject.identifier.. パッケージにのみ配置可能")
          .because("対象クラスを ..valueobject.identifier.. パッケージに移動してください")
          .allowEmptyShould(true);

  /** ValueObject 実装は valueobject パッケージにのみ配置可能。 */
  @ArchTest
  /* default */ static final ArchRule VO_IN_PKG =
      classes()
          .that()
          .areAssignableTo(org.jmolecules.ddd.types.ValueObject.class)
          .should()
          .resideInAPackage("..model.valueobject..")
          .as("ValueObject 実装は ..model.valueobject.. パッケージにのみ配置可能")
          .because("対象クラスを ..model.valueobject.. パッケージに移動してください")
          .allowEmptyShould(true);

  /** Repository インターフェースは domain/repository にのみ配置可能（infrastructure の実装クラスは除外）。 */
  @ArchTest
  /* default */ static final ArchRule REPO_IN_PKG =
      classes()
          .that()
          .areAssignableTo(org.jmolecules.ddd.types.Repository.class)
          .and()
          .areInterfaces()
          .should()
          .resideInAPackage("..domain.repository..")
          .as("Repository インターフェースは ..domain.repository.. パッケージにのみ配置可能")
          .because("対象インターフェースを ..domain.repository.. パッケージに移動してください")
          .allowEmptyShould(true);

  // === メソッドアノテーション配置制約 ===

  /** {@code @CommandHandler} メソッドを持つクラスは command/handler にのみ配置可能。 */
  @ArchTest
  /* default */ static final ArchRule CMD_H_METHOD =
      classes()
          .that()
          .containAnyMethodsThat(HAS_CMD_HANDLER)
          .should()
          .resideInAPackage("..command.handler..")
          .as("@CommandHandler メソッドを持つクラスは ..command.handler.. パッケージにのみ配置可能")
          .because("対象クラスを ..command.handler.. パッケージに移動してください")
          .allowEmptyShould(true);

  /** {@code @ApplicationModuleListener} メソッドを持つクラスは command/handler にのみ配置可能。 */
  @ArchTest
  /* default */ static final ArchRule EVT_LISTENER =
      classes()
          .that()
          .containAnyMethodsThat(HAS_MOD_LISTENER)
          .should()
          .resideInAPackage("..command.handler..")
          .as("@ApplicationModuleListener メソッドを持つクラスは ..command.handler.. パッケージにのみ配置可能")
          .because("対象クラスを ..command.handler.. パッケージに移動してください")
          .allowEmptyShould(true);

  // === 実装クラス配置制約 ===

  /** RepositoryImpl クラスは infrastructure/db/repository にのみ配置可能。 */
  @ArchTest
  /* default */ static final ArchRule REPO_IMPL_PKG =
      classes()
          .that()
          .haveSimpleNameEndingWith("RepositoryImpl")
          .should()
          .resideInAPackage("..infrastructure.db.repository..")
          .as("*RepositoryImpl クラスは ..infrastructure.db.repository.. パッケージにのみ配置可能")
          .because("対象クラスを ..infrastructure.db.repository.. パッケージに移動してください")
          .allowEmptyShould(true);

  /** QueryServiceImpl クラスは infrastructure/db/query にのみ配置可能。 */
  @ArchTest
  /* default */ static final ArchRule QRY_IMPL_PKG =
      classes()
          .that()
          .haveSimpleNameEndingWith("QueryServiceImpl")
          .should()
          .resideInAPackage("..infrastructure.db.query..")
          .as("*QueryServiceImpl クラスは ..infrastructure.db.query.. パッケージにのみ配置可能")
          .because("対象クラスを ..infrastructure.db.query.. パッケージに移動してください")
          .allowEmptyShould(true);

  // === 不変性制約 ===

  /** aggregate のフィールドは private final でなければならない。 */
  @ArchTest
  /* default */ static final ArchRule AGG_FIELDS_FINAL =
      fields()
          .that()
          .areDeclaredInClassesThat()
          .resideInAPackage("..model.aggregate..")
          .and()
          .areNotStatic()
          .should()
          .bePrivate()
          .andShould()
          .beFinal()
          .as("aggregate のフィールドは private final でなければならない")
          .because("不変性制約: フィールドに private final を付与してください。" + " 状態変更は新しいインスタンスを生成してください")
          .allowEmptyShould(true);

  /** entity のフィールドは private final でなければならない。 */
  @ArchTest
  /* default */ static final ArchRule ENT_FIELDS_FINAL =
      fields()
          .that()
          .areDeclaredInClassesThat()
          .resideInAPackage("..model.entity..")
          .and()
          .areNotStatic()
          .should()
          .bePrivate()
          .andShould()
          .beFinal()
          .as("entity のフィールドは private final でなければならない")
          .because("不変性制約: フィールドに private final を付与してください。" + " 状態変更は新しいインスタンスを生成してください")
          .allowEmptyShould(true);
}
