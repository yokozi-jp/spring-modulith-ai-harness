package com.example.demo.architecture.custom.policy;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * 型制約ポリシー。
 *
 * <p>特定パッケージに record またはインターフェースのみ配置可能とする制約を定義する。
 */
public final class TypePolicy {

  /** package-info クラスの除外条件用。 */
  private static final String PKG_INFO = "package-info";

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

  /** command/command には record のみ配置可能（package-info を除く）。 */
  @ArchTest
  /* default */ static final ArchRule CMD_CMD_REC =
      classes()
          .that()
          .resideInAPackage("..command.command..")
          .and()
          .haveSimpleNameNotContaining(PKG_INFO)
          .should()
          .beRecords()
          .as("command/command には record のみ配置可能(package-info 除く)")
          .because("対象クラスを record に変換してください (例: public record XxxCommand(...))")
          .allowEmptyShould(true);

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
          .because("対象クラスを record に変換してください (例: public record XxxDto(...))")
          .allowEmptyShould(true);

  /** query/param には record のみ配置可能（package-info を除く）。 */
  @ArchTest
  /* default */ static final ArchRule QRY_PARAM_REC =
      classes()
          .that()
          .resideInAPackage("..query.param..")
          .and()
          .haveSimpleNameNotContaining(PKG_INFO)
          .should()
          .beRecords()
          .as("query/param には record のみ配置可能(package-info 除く)")
          .because("対象クラスを record に変換してください (例: public record XxxParam(...))")
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

  private TypePolicy() {}
}
