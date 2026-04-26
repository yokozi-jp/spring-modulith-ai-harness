package com.example.demo.architecture.modulith;

import static org.assertj.core.api.Assertions.fail;

import com.example.demo.DemoApplication;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/** Spring Modulith のモジュール境界違反と循環依存を検出する。 */
class ModularStructureTest {

  /** テスト対象のアプリケーションモジュール構造。 */
  private static final ApplicationModules MODULES = ApplicationModules.of(DemoApplication.class);

  @SuppressWarnings({"PMD.AvoidCatchingGenericException", "PMD.UnitTestContainsTooManyAsserts"})
  @Test
  void verifyModularStructure() {
    try {
      MODULES.verify();
    } catch (final Exception exception) {
      fail(
          "Spring Modulith モジュール境界違反を検出。"
              + " 修正: 他モジュールの内部(サブパッケージ)クラスへの直接参照を除去し、"
              + "モジュールのルートパッケージで公開された API のみを使用してください。"
              + " モジュール間通信には ApplicationEventPublisher を使用してください。"
              + " 詳細:\n"
              + exception.getMessage());
    }
  }
}
