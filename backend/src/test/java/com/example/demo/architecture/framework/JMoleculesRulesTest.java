package com.example.demo.architecture.framework;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.jmolecules.archunit.JMoleculesArchitectureRules;
import org.jmolecules.archunit.JMoleculesDddRules;

/** jMolecules の Onion Architecture + DDD 構造ルールを検証する。 */
@SuppressWarnings("PMD.TestClassWithoutTestCases")
@AnalyzeClasses(packages = "com.example.demo", importOptions = ImportOption.DoNotIncludeTests.class)
class JMoleculesRulesTest {

  /** Classical Onion のリング間依存方向を検証する。 */
  @ArchTest
  /* default */ static final ArchRule ONION_CLASSICAL =
      JMoleculesArchitectureRules.ensureOnionClassical()
          .because(
              "Onion Architecture 違反: 内側リング(Domain)から外側リング(Infrastructure/Application)への"
                  + "依存を除去してください。依存方向は外→内のみ許可されます");

  /** DDD の集約境界・エンティティ識別子・値オブジェクトの構造を検証する。 */
  @ArchTest
  /* default */ static final ArchRule DDD_RULES =
      JMoleculesDddRules.all()
          .because(
              "jMolecules DDD 構造違反: AggregateRoot/Entity/ValueObject/Identifier の"
                  + "実装と配置パッケージの対応を確認してください")
          .allowEmptyShould(true);
}
