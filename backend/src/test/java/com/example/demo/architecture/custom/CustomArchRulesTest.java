package com.example.demo.architecture.custom;

import com.example.demo.architecture.custom.policy.AnnotationPolicy;
import com.example.demo.architecture.custom.policy.CqrsPolicy;
import com.example.demo.architecture.custom.policy.DddPolicy;
import com.example.demo.architecture.custom.policy.JooqPolicy;
import com.example.demo.architecture.custom.policy.LayerPolicy;
import com.example.demo.architecture.custom.policy.SecurityPolicy;
import com.example.demo.architecture.custom.policy.TimePolicy;
import com.example.demo.architecture.custom.policy.TransactionPolicy;
import com.example.demo.architecture.custom.policy.TypePolicy;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchTests;

/**
 * プロジェクト固有のアーキテクチャ制約を検証する。
 *
 * <p>各ポリシークラスにルールを委譲し、ここでは有効なポリシーの宣言のみ行う。
 */
@SuppressWarnings("PMD.TestClassWithoutTestCases")
@AnalyzeClasses(packages = "com.example.demo", importOptions = ImportOption.DoNotIncludeTests.class)
class CustomArchRulesTest {

  /** CQRS: アノテーション配置・Command/Query 分離・query→domain 依存禁止。 */
  @ArchTest /* default */ static final ArchTests CQRS = ArchTests.in(CqrsPolicy.class);

  /** DDD: 型↔パッケージ双方向・不変性・コンストラクタ制約・reconstitute・実装クラス配置。 */
  @ArchTest /* default */ static final ArchTests DDD = ArchTests.in(DddPolicy.class);

  /** レイヤー: presentation/domain/infrastructure 間の依存方向。 */
  @ArchTest /* default */ static final ArchTests LAYER = ArchTests.in(LayerPolicy.class);

  /** トランザクション: @Transactional の配置と可視性。 */
  @ArchTest
  /* default */ static final ArchTests TRANSACTION = ArchTests.in(TransactionPolicy.class);

  /** 日時: レガシー API 禁止・Clock/Instant.now() 制約。 */
  @ArchTest /* default */ static final ArchTests TIME = ArchTests.in(TimePolicy.class);

  /** jOOQ: 型漏洩防止（infrastructure 層に閉じ込め）。 */
  @ArchTest /* default */ static final ArchTests JOOQ = ArchTests.in(JooqPolicy.class);

  /** 型制約: record/interface のみ許可するパッケージ。 */
  @ArchTest /* default */ static final ArchTests TYPE = ArchTests.in(TypePolicy.class);

  /** メソッドアノテーション: @CommandHandler, @ApplicationModuleListener の配置。 */
  @ArchTest /* default */ static final ArchTests ANNOTATION = ArchTests.in(AnnotationPolicy.class);

  /** セキュリティ: SecurityFilterChain の順序・数・ADR-0004 ワークアラウンド。 */
  @ArchTest /* default */ static final ArchTests SECURITY = ArchTests.in(SecurityPolicy.class);
}
