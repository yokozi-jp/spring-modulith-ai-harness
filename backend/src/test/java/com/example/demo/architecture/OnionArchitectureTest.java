package com.example.demo.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.jmolecules.archunit.JMoleculesArchitectureRules;
import org.jmolecules.archunit.JMoleculesDddRules;

/** jMolecules の Onion Architecture + DDD 構造ルールを検証する。 */
@SuppressWarnings("PMD.TestClassWithoutTestCases")
@AnalyzeClasses(packages = "com.example.demo", importOptions = ImportOption.DoNotIncludeTests.class)
class OnionArchitectureTest {

  /** Classical Onion のリング間依存方向を検証する。 */
  @ArchTest
  /* default */ static final ArchRule ONION_CLASSICAL =
      JMoleculesArchitectureRules.ensureOnionClassical();

  /** DDD の集約境界・エンティティ識別子・値オブジェクトの構造を検証する。 */
  @ArchTest
  /* default */ static final ArchRule DDD_RULES = JMoleculesDddRules.all().allowEmptyShould(true);
}
