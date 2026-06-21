package com.example.demo.architecture.custom.policy;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Bean 登録アノテーション併用ポリシー。
 *
 * <p>jMolecules DDD アノテーション（{@code @Repository}, {@code @Factory}, {@code @Service}）を使用する
 * クラスには、対応する Spring ステレオタイプアノテーションも必ず併用する。
 */
public final class BeanRegistrationPolicy {

  /** jMolecules {@code @Repository} を持つクラスは Spring {@code @Repository} も持つこと。 */
  @ArchTest
  /* default */ static final ArchRule JMOL_REPO_HAS_SPRING_REPO =
      classes()
          .that()
          .areAnnotatedWith(org.jmolecules.ddd.annotation.Repository.class)
          .should()
          .beAnnotatedWith(org.springframework.stereotype.Repository.class)
          .as("jMolecules @Repository を持つクラスは Spring @Repository も併用すること")
          .allowEmptyShould(true);

  /** jMolecules {@code @Factory} を持つクラスは Spring {@code @Component} も持つこと。 */
  @ArchTest
  /* default */ static final ArchRule JMOL_FACTORY_HAS_SPRING_COMPONENT =
      classes()
          .that()
          .areAnnotatedWith(org.jmolecules.ddd.annotation.Factory.class)
          .should()
          .beAnnotatedWith(org.springframework.stereotype.Component.class)
          .as("jMolecules @Factory を持つクラスは Spring @Component も併用すること")
          .allowEmptyShould(true);

  /** jMolecules {@code @Service} を持つクラスは Spring {@code @Service} も持つこと。 */
  @ArchTest
  /* default */ static final ArchRule JMOL_SERVICE_HAS_SPRING_SERVICE =
      classes()
          .that()
          .areAnnotatedWith(org.jmolecules.ddd.annotation.Service.class)
          .should()
          .beAnnotatedWith(org.springframework.stereotype.Service.class)
          .as("jMolecules @Service を持つクラスは Spring @Service も併用すること")
          .allowEmptyShould(true);

  private BeanRegistrationPolicy() {}
}
