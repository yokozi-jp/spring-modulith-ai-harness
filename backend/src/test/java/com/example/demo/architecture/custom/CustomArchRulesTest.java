package com.example.demo.architecture.custom;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaConstructorCall;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
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

  /** aggregate パッケージパターン。 */
  private static final String PKG_AGG = "..model.aggregate..";

  /** entity パッケージパターン。 */
  private static final String PKG_ENT = "..model.entity..";

  /** domain/service パッケージパターン。 */
  private static final String PKG_SVC = "..domain.service..";

  /** domain/repository パッケージパターン。 */
  private static final String PKG_REPO = "..domain.repository..";

  /** {@code @CommandHandler} メソッド判定用。 */
  private static final DescribedPredicate<JavaMethod> HAS_CMD_HANDLER =
      new DescribedPredicate<>("annotated with @CommandHandler") {
        @Override
        public boolean test(final JavaMethod method) {
          return method.isAnnotatedWith(org.jmolecules.architecture.cqrs.CommandHandler.class);
        }
      };

  /** {@code @ApplicationModuleListener} メソッド判定用。 */
  private static final DescribedPredicate<JavaMethod> HAS_MOD_LISTENER =
      new DescribedPredicate<>("annotated with @ApplicationModuleListener") {
        @Override
        public boolean test(final JavaMethod method) {
          return method.isAnnotatedWith(
              org.springframework.modulith.events.ApplicationModuleListener.class);
        }
      };

  /** aggregate パッケージへのコンストラクタ呼び出し判定用。 */
  private static final DescribedPredicate<JavaConstructorCall> TARGET_IN_AGG =
      new DescribedPredicate<>("target is in ..model.aggregate..") {
        @Override
        public boolean test(final JavaConstructorCall call) {
          return call.getTargetOwner().getPackageName().contains(".model.aggregate");
        }
      };

  /** entity パッケージへのコンストラクタ呼び出し判定用。 */
  private static final DescribedPredicate<JavaConstructorCall> TARGET_IN_ENT =
      new DescribedPredicate<>("target is in ..model.entity..") {
        @Override
        public boolean test(final JavaConstructorCall call) {
          return call.getTargetOwner().getPackageName().contains(".model.entity");
        }
      };

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

  /**
   * domain パッケージは Spring に依存してはいけない。
   *
   * <p>domain/service と domain/repository は jMolecules ByteBuddy プラグインにより Spring ステレオタイプ
   * アノテーションがバイトコードレベルで付与されるため除外する。
   *
   * <p>AbstractAggregateRoot の継承は許可する（集約ルートのイベント登録に必要）。
   */
  @ArchTest
  /* default */ static final ArchRule DOMAIN_NO_SPRING =
      noClasses()
          .that()
          .resideInAPackage("..domain..")
          .and()
          .resideOutsideOfPackage("..domain.service..")
          .and()
          .resideOutsideOfPackage(PKG_REPO)
          .and()
          .areNotAssignableTo(org.springframework.data.domain.AbstractAggregateRoot.class)
          .should()
          .dependOnClassesThat()
          .resideInAPackage("org.springframework..")
          .as("domain パッケージ(service/repository/集約ルート 除く)は Spring に依存してはいけない")
          .because(
              "DDD 制約: domain 内の Spring import を除去してください。"
                  + " AbstractAggregateRoot の継承のみ許可されます。"
                  + " DI は jMolecules ByteBuddy 経由で自動付与されます")
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

  /** presentation は domain に依存してはいけない。例外クラスはモジュールルートに配置する。 */
  @ArchTest
  /* default */ static final ArchRule PRES_NO_DOMAIN =
      noClasses()
          .that()
          .resideInAPackage("..presentation..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("com.example.demo..domain..")
          .as("presentation は domain に依存してはいけない")
          .because(
              "レイヤー制約: presentation から domain への直接参照を除去してください。"
                  + " 例外クラスはモジュールルートに配置し、そこから参照してください")
          .allowEmptyShould(true);

  // === インターフェースのみ制約 ===

  /** domain/repository にはインターフェースのみ配置可能。 */
  @ArchTest
  /* default */ static final ArchRule REPO_IFACE =
      classes()
          .that()
          .resideInAPackage(PKG_REPO)
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
          .resideInAPackage(PKG_AGG)
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
          .resideInAPackage(PKG_ENT)
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
          .resideInAPackage(PKG_AGG)
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
          .resideInAPackage(PKG_ENT)
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
          .resideInAPackage(PKG_REPO)
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
          .resideInAPackage(PKG_AGG)
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
          .resideInAPackage(PKG_ENT)
          .and()
          .areNotStatic()
          .should()
          .bePrivate()
          .andShould()
          .beFinal()
          .as("entity のフィールドは private final でなければならない")
          .because("不変性制約: フィールドに private final を付与してください。" + " 状態変更は新しいインスタンスを生成してください")
          .allowEmptyShould(true);

  // === コンストラクタ呼び出し制約 ===

  /** domain パッケージから {@code Instant.now()} を直接呼び出してはいけない。Clock を DI すること。 */
  @ArchTest
  /* default */ static final ArchRule NO_INSTANT_NOW_IN_DOMAIN =
      noClasses()
          .that()
          .resideInAPackage("..domain..")
          .should()
          .callMethod(java.time.Instant.class, "now")
          .as("domain パッケージから Instant.now() を呼び出してはいけない")
          .because(
              "テスタビリティ制約: java.time.Clock を DI し clock.instant() を使用してください。"
                  + " テストでは Clock.fixed(...) で時刻を固定できます")
          .allowEmptyShould(true);

  /** IdGenerator インターフェースは domain/repository にのみ配置可能。 */
  @ArchTest
  /* default */ static final ArchRule ID_GEN_IFACE_PKG =
      classes()
          .that()
          .haveSimpleNameEndingWith("IdGenerator")
          .and()
          .areInterfaces()
          .should()
          .resideInAPackage(PKG_REPO)
          .as("*IdGenerator インターフェースは ..domain.repository.. パッケージにのみ配置可能")
          .because("IdGenerator インターフェースを ..domain.repository.. パッケージに移動してください")
          .allowEmptyShould(true);

  /** IdGeneratorImpl クラスは infrastructure/db/repository にのみ配置可能。 */
  @ArchTest
  /* default */ static final ArchRule ID_GEN_IMPL_PKG =
      classes()
          .that()
          .haveSimpleNameEndingWith("IdGeneratorImpl")
          .should()
          .resideInAPackage("..infrastructure.db.repository..")
          .as("*IdGeneratorImpl クラスは ..infrastructure.db.repository.. パッケージにのみ配置可能")
          .because("IdGeneratorImpl を ..infrastructure.db.repository.. パッケージに移動してください")
          .allowEmptyShould(true);

  /** {@code @Transactional} メソッド判定用。 */
  private static final DescribedPredicate<JavaMethod> HAS_TRANSACTIONAL =
      new DescribedPredicate<>("annotated with @Transactional") {
        @Override
        public boolean test(final JavaMethod method) {
          return method.isAnnotatedWith(
              org.springframework.transaction.annotation.Transactional.class);
        }
      };

  /** {@code @Transactional} は command/handler にのみ配置可能。 */
  @ArchTest
  /* default */ static final ArchRule TX_PLACEMENT =
      classes()
          .that()
          .containAnyMethodsThat(HAS_TRANSACTIONAL)
          .should()
          .resideInAPackage("..command.handler..")
          .as("@Transactional メソッドを持つクラスは ..command.handler.. パッケージにのみ配置可能")
          .because("トランザクション制約: @Transactional は CommandHandler の public メソッドにのみ付与してください")
          .allowEmptyShould(true);

  /** aggregate のコンストラクタは aggregate 自身と domain/service からのみ呼び出し可能。 */
  @ArchTest
  /* default */ static final ArchRule AGG_CTOR =
      noClasses()
          .that()
          .resideOutsideOfPackages(PKG_AGG, PKG_SVC)
          .should()
          .callConstructorWhere(TARGET_IN_AGG)
          .as("aggregate のコンストラクタは model/aggregate と domain/service からのみ呼び出し可能")
          .because(
              "aggregate の生成には Factory を使用してください。"
                  + " 直接 new は model/aggregate 内と domain/service 内でのみ許可されます。"
                  + " 永続化からの再構築には aggregate 内の static reconstitute メソッドを使用してください")
          .allowEmptyShould(true);

  /** entity のコンストラクタは entity 自身と domain/service からのみ呼び出し可能。 */
  @ArchTest
  /* default */ static final ArchRule ENT_CTOR =
      noClasses()
          .that()
          .resideOutsideOfPackages(PKG_ENT, PKG_SVC)
          .should()
          .callConstructorWhere(TARGET_IN_ENT)
          .as("entity のコンストラクタは model/entity と domain/service からのみ呼び出し可能")
          .because(
              "entity の生成には Factory を使用してください。"
                  + " 直接 new は model/entity 内と domain/service 内でのみ許可されます")
          .allowEmptyShould(true);

  // === valueobject 正方向制約 ===

  /** valueobject パッケージ（identifier 除く）には ValueObject 実装のみ配置可能。 */
  @ArchTest
  /* default */ static final ArchRule VO_TYPE =
      classes()
          .that()
          .resideInAPackage("..model.valueobject..")
          .and()
          .resideOutsideOfPackage("..valueobject.identifier..")
          .and()
          .haveSimpleNameNotContaining(PKG_INFO)
          .should()
          .beAssignableTo(org.jmolecules.ddd.types.ValueObject.class)
          .as("model/valueobject(identifier 除く)には ValueObject 実装のみ配置可能(package-info 除く)")
          .because("対象クラスに ValueObject を実装するか、適切なパッケージに移動してください")
          .allowEmptyShould(true);

  // === @Transactional public メソッド制約 ===

  /** {@code @Transactional} メソッドは public でなければならない。 */
  @ArchTest
  /* default */ static final ArchRule TX_PUBLIC =
      methods()
          .that()
          .areAnnotatedWith(org.springframework.transaction.annotation.Transactional.class)
          .should()
          .bePublic()
          .as("@Transactional メソッドは public でなければならない")
          .because(
              "トランザクション制約: @Transactional は public メソッドにのみ付与してください。"
                  + " Spring AOP プロキシは public メソッドでのみ機能します")
          .allowEmptyShould(true);

  // === Clock.systemUTC() / Clock.system() 直接呼び出し禁止 ===

  /** {@code Clock.systemUTC()} / {@code Clock.system()} の直接呼び出しを禁止する。ClockConfig のみ許可。 */
  @ArchTest
  /* default */ static final ArchRule NO_CLOCK_SYSTEM =
      noClasses()
          .that()
          .haveSimpleNameNotEndingWith("ClockConfig")
          .should()
          .callMethod(java.time.Clock.class, "systemUTC")
          .orShould()
          .callMethod(java.time.Clock.class, "system", java.time.ZoneId.class)
          .orShould()
          .callMethod(java.time.Clock.class, "systemDefaultZone")
          .as("ClockConfig 以外で Clock.systemUTC()/system()/systemDefaultZone() を呼び出してはいけない")
          .because(
              "テスタビリティ制約: Clock は ClockConfig Bean から DI してください。"
                  + " 直接 Clock.systemUTC() を呼ぶとテストで時刻を固定できません")
          .allowEmptyShould(true);

  // === reconstitute メソッド存在検証 ===

  /** reconstitute メソッド存在検証用の条件。 */
  private static final ArchCondition<com.tngtech.archunit.core.domain.JavaClass> HAVE_RECONSTITUTE =
      new ArchCondition<>("have a public static reconstitute method") {
        @Override
        public void check(
            final com.tngtech.archunit.core.domain.JavaClass item,
            final com.tngtech.archunit.lang.ConditionEvents events) {
          final boolean found =
              item.getMethods().stream()
                  .anyMatch(
                      m ->
                          "reconstitute".equals(m.getName())
                              && m.getModifiers().contains(JavaModifier.PUBLIC)
                              && m.getModifiers().contains(JavaModifier.STATIC));
          if (!found) {
            events.add(
                com.tngtech.archunit.lang.SimpleConditionEvent.violated(
                    item, item.getName() + " に public static reconstitute メソッドがありません"));
          }
        }
      };

  /** AggregateRoot 実装クラスに public static reconstitute メソッドが存在すること。 */
  @ArchTest
  /* default */ static final ArchRule AGG_RECONSTITUTE =
      classes()
          .that()
          .areAssignableTo(org.jmolecules.ddd.types.AggregateRoot.class)
          .should(HAVE_RECONSTITUTE)
          .as("AggregateRoot 実装クラスに public static reconstitute メソッドが必要")
          .because(
              "再構築制約: RepositoryImpl からの再構築用に" + " public static reconstitute(...) メソッドを定義してください")
          .allowEmptyShould(true);

  // === jOOQ 型漏洩防止 ===

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

  // === レガシー日時 API / LocalDateTime 使用禁止 ===

  /** {@code java.util.Date} の使用を禁止する。{@code java.time.Instant} を使用すること。 */
  @ArchTest
  /* default */ static final ArchRule NO_JAVA_UTIL_DATE =
      noClasses()
          .should()
          .dependOnClassesThat()
          .haveFullyQualifiedName("java.util.Date")
          .as("java.util.Date の使用は禁止")
          .because("日時制約: java.util.Date ではなく java.time.Instant を使用してください")
          .allowEmptyShould(true);

  /** {@code java.util.Calendar} の使用を禁止する。{@code java.time} API を使用すること。 */
  @ArchTest
  /* default */ static final ArchRule NO_JAVA_UTIL_CALENDAR =
      noClasses()
          .should()
          .dependOnClassesThat()
          .haveFullyQualifiedName("java.util.Calendar")
          .as("java.util.Calendar の使用は禁止")
          .because("日時制約: java.util.Calendar ではなく java.time API を使用してください")
          .allowEmptyShould(true);

  /** {@code LocalDateTime} の使用を禁止する。DB の timestamptz と対応しないため。 */
  @ArchTest
  /* default */ static final ArchRule NO_LOCAL_DATE_TIME =
      noClasses()
          .should()
          .dependOnClassesThat()
          .haveFullyQualifiedName("java.time.LocalDateTime")
          .as("LocalDateTime の使用は禁止")
          .because(
              "日時制約: LocalDateTime は DB の timestamptz と対応しません。"
                  + " Instant または OffsetDateTime を使用してください")
          .allowEmptyShould(true);
}
