package com.example.demo.architecture.custom.policy;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * メソッドアノテーション配置ポリシー。
 *
 * <p>{@code @CommandHandler} と {@code @ApplicationModuleListener} の配置制約を定義する。
 */
public final class AnnotationPolicy {

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

  private AnnotationPolicy() {}
}
