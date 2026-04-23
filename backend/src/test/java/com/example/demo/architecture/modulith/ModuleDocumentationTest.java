package com.example.demo.architecture.modulith;

import com.example.demo.DemoApplication;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/** モジュール構成図（PlantUML）とモジュールキャンバスを自動生成する。 */
class ModuleDocumentationTest {

  /** テスト対象のアプリケーションモジュール構造。 */
  private static final ApplicationModules MODULES = ApplicationModules.of(DemoApplication.class);

  @Test
  @SuppressWarnings("PMD.UnitTestShouldIncludeAssert")
  void generateDocumentation() {
    new Documenter(MODULES)
        .writeModulesAsPlantUml()
        .writeIndividualModulesAsPlantUml()
        .writeModuleCanvases();
  }
}
