package com.example.demo.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.jmolecules.archunit.JMoleculesArchitectureRules;
import org.junit.jupiter.api.Test;

/** jMolecules の Classical Onion Architecture ルールでレイヤー依存方向を検証する。 */
class OnionArchitectureTest {

  /** ArchUnit 解析対象のプロダクションクラス群。 */
  @SuppressWarnings("PMD.LooseCoupling")
  private static final JavaClasses CLASSES =
      new ClassFileImporter()
          .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
          .importPackages("com.example.demo");

  @Test
  void ensureOnionClassicalArchitecture() {
    final ArchRule rule = JMoleculesArchitectureRules.ensureOnionClassical();
    // check() は違反があると AssertionError をスローする
    rule.check(CLASSES);
  }
}
