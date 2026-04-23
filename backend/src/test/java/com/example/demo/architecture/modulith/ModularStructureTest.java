package com.example.demo.architecture.modulith;

import com.example.demo.DemoApplication;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/** Spring Modulith のモジュール境界違反と循環依存を検出する。 */
class ModularStructureTest {

  /** テスト対象のアプリケーションモジュール構造。 */
  private static final ApplicationModules MODULES = ApplicationModules.of(DemoApplication.class);

  @Test
  void verifyModularStructure() {
    // verify() はモジュール境界違反・循環依存があると例外をスローする
    MODULES.verify();
  }
}
