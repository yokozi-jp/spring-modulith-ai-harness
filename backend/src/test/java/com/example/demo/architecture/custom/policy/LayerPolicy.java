package com.example.demo.architecture.custom.policy;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * レイヤー依存ポリシー。
 *
 * <p>presentation/domain/infrastructure 間の不正な依存方向を禁止する。
 */
public final class LayerPolicy {

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
          .resideOutsideOfPackage("..domain.repository..")
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

  /** presentation は domain に依存してはいけない。 */
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

  private LayerPolicy() {}
}
