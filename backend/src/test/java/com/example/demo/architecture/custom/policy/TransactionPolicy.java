package com.example.demo.architecture.custom.policy;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * トランザクションポリシー。
 *
 * <p>{@code @Transactional} の配置制約と public メソッド制約を定義する。
 */
public final class TransactionPolicy {

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

  private TransactionPolicy() {}
}
