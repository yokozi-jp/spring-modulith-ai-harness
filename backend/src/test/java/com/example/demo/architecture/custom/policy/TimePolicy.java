package com.example.demo.architecture.custom.policy;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * 日時 API ポリシー。
 *
 * <p>レガシー日時 API 禁止・Clock 制約・Instant.now() 禁止を定義する。
 */
public final class TimePolicy {

  /** {@code java.util.Date} の使用を禁止する。 */
  @ArchTest
  /* default */ static final ArchRule NO_JAVA_UTIL_DATE =
      noClasses()
          .should()
          .dependOnClassesThat()
          .haveFullyQualifiedName("java.util.Date")
          .as("java.util.Date の使用は禁止")
          .because("日時制約: java.util.Date ではなく java.time.Instant を使用してください")
          .allowEmptyShould(true);

  /** {@code java.util.Calendar} の使用を禁止する。 */
  @ArchTest
  /* default */ static final ArchRule NO_JAVA_UTIL_CALENDAR =
      noClasses()
          .should()
          .dependOnClassesThat()
          .haveFullyQualifiedName("java.util.Calendar")
          .as("java.util.Calendar の使用は禁止")
          .because("日時制約: java.util.Calendar ではなく java.time API を使用してください")
          .allowEmptyShould(true);

  /** {@code LocalDateTime} の使用を禁止する。 */
  @ArchTest
  /* default */ static final ArchRule NO_LOCAL_DATE_TIME =
      noClasses()
          .should()
          .dependOnClassesThat()
          .haveFullyQualifiedName("java.time.LocalDateTime")
          .as("LocalDateTime の使用は禁止")
          .because(
              "日時制約: LocalDateTime は DB の timestamptz と対応しません。"
                  + " Instant または OffsetDateTime を使用してください")
          .allowEmptyShould(true);

  /** domain パッケージから {@code Instant.now()} を直接呼び出してはいけない。 */
  @ArchTest
  /* default */ static final ArchRule NO_INSTANT_NOW_IN_DOMAIN =
      noClasses()
          .that()
          .resideInAPackage("..domain..")
          .should()
          .callMethod(java.time.Instant.class, "now")
          .as("domain パッケージから Instant.now() を呼び出してはいけない")
          .because(
              "テスタビリティ制約: java.time.Clock を DI し clock.instant() を使用してください。"
                  + " テストでは Clock.fixed(...) で時刻を固定できます")
          .allowEmptyShould(true);

  /** {@code Clock.systemUTC()} / {@code Clock.system()} の直接呼び出しを禁止する。 */
  @ArchTest
  /* default */ static final ArchRule NO_CLOCK_SYSTEM =
      noClasses()
          .that()
          .haveSimpleNameNotEndingWith("ClockConfig")
          .should()
          .callMethod(java.time.Clock.class, "systemUTC")
          .orShould()
          .callMethod(java.time.Clock.class, "system", java.time.ZoneId.class)
          .orShould()
          .callMethod(java.time.Clock.class, "systemDefaultZone")
          .as("ClockConfig 以外で Clock.systemUTC()/system()/systemDefaultZone() を呼び出してはいけない")
          .because(
              "テスタビリティ制約: Clock は ClockConfig Bean から DI してください。"
                  + " 直接 Clock.systemUTC() を呼ぶとテストで時刻を固定できません")
          .allowEmptyShould(true);

  private TimePolicy() {}
}
