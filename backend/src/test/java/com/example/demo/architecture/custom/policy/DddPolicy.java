package com.example.demo.architecture.custom.policy;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaConstructorCall;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

/**
 * DDD 構造ポリシー。
 *
 * <p>型↔パッケージ双方向制約・不変性制約・コンストラクタ呼び出し制約・reconstitute 存在検証を定義する。
 */
public final class DddPolicy {

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

  /** reconstitute メソッド存在検証用の条件。 */
  private static final ArchCondition<com.tngtech.archunit.core.domain.JavaClass> HAVE_RECONSTITUTE =
      new ArchCondition<>("have a public static reconstitute method") {
        @Override
        public void check(
            final com.tngtech.archunit.core.domain.JavaClass item, final ConditionEvents events) {
          final boolean found =
              item.getMethods().stream()
                  .anyMatch(
                      m ->
                          "reconstitute".equals(m.getName())
                              && m.getModifiers().contains(JavaModifier.PUBLIC)
                              && m.getModifiers().contains(JavaModifier.STATIC));
          if (!found) {
            events.add(
                SimpleConditionEvent.violated(
                    item, item.getName() + " に public static reconstitute メソッドがありません"));
          }
        }
      };

  // === 正方向: パッケージ → 型制約 ===

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

  // === 逆方向: 型 → パッケージ制約 ===

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

  /** Repository インターフェースは domain/repository にのみ配置可能。 */
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
          .because("不変性制約: フィールドに private final を付与してください。状態変更は新しいインスタンスを生成してください")
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
          .because("不変性制約: フィールドに private final を付与してください。状態変更は新しいインスタンスを生成してください")
          .allowEmptyShould(true);

  // === コンストラクタ呼び出し制約 ===

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

  // === reconstitute 存在検証 ===

  /** AggregateRoot 実装クラスに public static reconstitute メソッドが存在すること。 */
  @ArchTest
  /* default */ static final ArchRule AGG_RECONSTITUTE =
      classes()
          .that()
          .areAssignableTo(org.jmolecules.ddd.types.AggregateRoot.class)
          .should(HAVE_RECONSTITUTE)
          .as("AggregateRoot 実装クラスに public static reconstitute メソッドが必要")
          .because("再構築制約: RepositoryImpl からの再構築用に public static reconstitute(...) メソッドを定義してください")
          .allowEmptyShould(true);

  // === IdGenerator / Impl 配置制約 ===

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

  private DddPolicy() {}
}
