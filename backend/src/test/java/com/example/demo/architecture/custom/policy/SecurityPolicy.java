package com.example.demo.architecture.custom.policy;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.Collection;

/**
 * セキュリティポリシー。
 *
 * <p>SecurityFilterChain の構造的健全性を検証する。
 *
 * <ul>
 *   <li>全 SecurityFilterChain Bean に {@code @Order} が付与されていること
 *   <li>SecurityFilterChain Bean が正確に {@link #EXPECTED_FILTER_CHAIN_COUNT} 個であること
 *   <li>{@code ActuatorSecurityConfig} が {@code FlushingBasicAuthEntryPoint} に依存していること （ADR-0004）
 * </ul>
 *
 * @see <a
 *     href="../../../../../../docs/adr/0004-basic-auth-entrypoint-flush-workaround.md">ADR-0004</a>
 */
public final class SecurityPolicy {

  /** 期待する SecurityFilterChain Bean の数。3つ目を追加する場合はここを更新すること。 */
  private static final int EXPECTED_FILTER_CHAIN_COUNT = 2;

  /** SecurityFilterChain を返す {@code @Bean} メソッドの判定用。 */
  private static final DescribedPredicate<JavaMethod> IS_FILTER_CHAIN_BEAN =
      new DescribedPredicate<>("@Bean method returning SecurityFilterChain") {
        @Override
        public boolean test(final JavaMethod method) {
          return method.isAnnotatedWith(org.springframework.context.annotation.Bean.class)
              && method
                  .getRawReturnType()
                  .isEquivalentTo(org.springframework.security.web.SecurityFilterChain.class);
        }
      };

  /** {@code @Order} 付与検証用の条件。 */
  private static final ArchCondition<JavaClass> HAVE_ORDER_ON_FILTER_CHAIN_BEANS =
      new ArchCondition<>("have @Order on all SecurityFilterChain @Bean methods") {
        @Override
        public void check(final JavaClass item, final ConditionEvents events) {
          item.getMethods().stream()
              .filter(IS_FILTER_CHAIN_BEAN)
              .forEach(
                  method -> {
                    if (!method.isAnnotatedWith(org.springframework.core.annotation.Order.class)) {
                      events.add(
                          SimpleConditionEvent.violated(
                              method, method.getFullName() + " に @Order が付与されていません"));
                    }
                  });
        }
      };

  /** Bean 数検証用の条件。 */
  private static final ArchCondition<JavaClass> HAVE_EXACTLY_N_FILTER_CHAINS =
      new ArchCondition<>(
          "collectively define exactly "
              + EXPECTED_FILTER_CHAIN_COUNT
              + " SecurityFilterChain beans") {

        /* 検出された SecurityFilterChain Bean の総数。 */
        private int totalCount;

        @Override
        public void init(final Collection<JavaClass> allItems) {
          totalCount = 0;
          for (final JavaClass item : allItems) {
            totalCount += (int) item.getMethods().stream().filter(IS_FILTER_CHAIN_BEAN).count();
          }
        }

        @Override
        public void check(final JavaClass item, final ConditionEvents events) {
          // 検証は finish で一括実行する
        }

        @Override
        public void finish(final ConditionEvents events) {
          if (totalCount != EXPECTED_FILTER_CHAIN_COUNT) {
            events.add(
                SimpleConditionEvent.violated(
                    SecurityPolicy.class,
                    "SecurityFilterChain Bean 数が "
                        + totalCount
                        + " ですが、期待値は "
                        + EXPECTED_FILTER_CHAIN_COUNT
                        + " です。"
                        + " 追加する場合は SecurityPolicy.EXPECTED_FILTER_CHAIN_COUNT"
                        + " を更新してください"));
          }
        }
      };

  /** 全 SecurityFilterChain Bean に {@code @Order} が付与されていること。 */
  @ArchTest
  /* default */ static final ArchRule ALL_FILTER_CHAINS_HAVE_ORDER =
      classes()
          .that()
          .containAnyMethodsThat(IS_FILTER_CHAIN_BEAN)
          .should(HAVE_ORDER_ON_FILTER_CHAIN_BEANS)
          .as("全 SecurityFilterChain Bean に @Order が付与されていること")
          .because("セキュリティ制約: SecurityFilterChain の評価順序を明示するため @Order を付与してください");

  /** SecurityFilterChain Bean が正確に {@value #EXPECTED_FILTER_CHAIN_COUNT} 個であること。 */
  @ArchTest
  /* default */ static final ArchRule EXACTLY_N_FILTER_CHAINS =
      classes()
          .that()
          .containAnyMethodsThat(IS_FILTER_CHAIN_BEAN)
          .should(HAVE_EXACTLY_N_FILTER_CHAINS)
          .as("SecurityFilterChain Bean が正確に " + EXPECTED_FILTER_CHAIN_COUNT + " 個であること")
          .because("セキュリティ制約: フィルターチェーンの追加は意図的に行ってください。" + " 追加する場合は SecurityPolicy の定数を更新してください");

  /**
   * ActuatorSecurityConfig が FlushingBasicAuthEntryPoint に依存していること（ADR-0004）。
   *
   * <p>Spring Security 7 の正式なマルチチェーン構成のバグ修正が出た場合、本ルールを削除すること。
   */
  @ArchTest
  /* default */ static final ArchRule ACTUATOR_DEPENDS_ON_FLUSHING_ENTRY_POINT =
      classes()
          .that()
          .haveSimpleNameEndingWith("ActuatorSecurityConfig")
          .should()
          .dependOnClassesThat()
          .haveSimpleName("FlushingBasicAuthEntryPoint")
          .as("ActuatorSecurityConfig は FlushingBasicAuthEntryPoint に依存していること")
          .because(
              "ADR-0004: OAuth2 EntryPoint による 302 上書きを防ぐため"
                  + " FlushingBasicAuthEntryPoint を使用してください。"
                  + " Spring Security 7 のバグ修正後に本ルールを削除可能です");

  private SecurityPolicy() {}
}
