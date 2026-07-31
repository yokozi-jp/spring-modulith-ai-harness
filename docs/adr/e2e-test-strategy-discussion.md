# E2Eテスト戦略 — 議論メモ

**日付**: 2026-05-15
**ステータス**: 検討中

---

## 背景

AI駆動開発の基盤作成の一環として、AIベースのE2Eテストの導入を検討している。

## 検討したライブラリ

### AI-nativeライブラリ

| ライブラリ | 特徴 | 懸念 |
|---|---|---|
| Stagehand | 自然言語で操作、セレクタ不要 | LLMコスト高、非決定的 |
| Browser Use | LLMエージェント向け自律操作 | 安定性・CI統合に課題 |
| Shortest | AI-first、Playwright基盤 | 成熟度低 |
| Midscene.js | マルチモーダル、既存ツール統合可 | LLMコスト |

### セレクタベースライブラリ

| ライブラリ | 特徴 |
|---|---|
| **Playwright** | マルチブラウザ、自動待機、MCP公式対応、並列実行無料 |
| Cypress | DX重視、タイムトラベルデバッグ、単一タブ制約あり |
| Selenium | 最古参、多言語、設定が重い |
| TestCafe | プロキシベース、WebDriver不要 |

## 決定事項

### Playwright を採用する

**理由:**

1. **AI連携の公式サポート** — MCPサーバーが公式提供済み。AIエージェントとの標準インターフェースが整っている
2. **CI/CD適性** — ヘッドレス・Docker対応が堅牢、並列実行・シャーディングが標準機能
3. **安定性** — 自動待機がデフォルト、テスト分離が強力
4. **多言語対応** — TypeScript, Python, Java, .NET（本プロジェクトのJavaバックエンドとも親和性あり）
5. **Cypressに対する優位性** — マルチタブ、iframe、並列実行無料、OS操作対応

### アーキテクチャ方針: 「実行」と「メンテナンス」の分離

- **テスト実行**: セレクタベースで決定的に動作（LLM不要、高速、低コスト）
- **テストメンテナンス**: AI活用でセレクタ更新・テスト修正を自動化

```
コード変更（PR作成）
  ↓
CI: E2Eテスト実行（Playwright、LLM不要）
  ↓ 失敗検知
AIエージェント起動
  ├─ 失敗ログ + スクリーンショット解析
  ├─ DOM差分とセレクタの突合
  └─ テストコード修正PRを自動作成
```

**この方針のメリット:**
- LLMコストが「実行回数に比例」ではなく「変更頻度に比例」
- CIのフィードバックループが高速（LLM推論待ちなし）
- フレーキーテストのリスクを回避（決定的実行）
- 既存Playwrightエコシステム（レポート・トレース等）をそのまま活用

## 確定した前提

| 項目 | 決定 |
|---|---|
| テスト対象 | React SPA + Spring Boot バックエンド（ブラウザUI含むE2E） |
| テスト環境 | 開発用composeとは別に、E2Eテスト専用compose構成を用意（DB含め専用コンテナ） |
| 実行方式 | 全コンテナ完結（アプリ群 + Playwrightランナーすべてcompose内） |
| CI | GitHub Actions |
| 言語 | TypeScript |
| AI活用方針 | 実行は決定的（セレクタベース）、メンテナンスにAIを活用 |
| フロントエンド | React SPA |
| バックエンド | Spring Boot（本プロジェクト） |

## 作業順序

```
1. ディレクトリ構成・Playwrightプロジェクト骨格        ← 完了（構成確定）
   ↓（配置が確定）
1.5. 簡易テスト実行（APIレベルhealthチェックでパイプライン疎通確認）  ← 完了（2026-06-01）
   ↓（パイプラインが通る）
2. コンテナ構成（Dockerfile, compose設計, ネットワーク） ← 完了（compose.e2e.yaml オーバーライド方式）
   ↓（実行環境が確定）
3. GitHub Actions ワークフロー                        ← 次
   ↓（CI統合が確定）
4. AIメンテナンス自動化の仕組み（MCP活用、CIフック設計）
```

---

## ディレクトリ構成の検討状況

### 合意済みの方針

- `e2e/` をリポジトリルートに `backend/`, `frontend/` と並列配置
- 単体テスト・統合テストは各アプリ内、E2Eはシステムレベルとして独立
- 参考記事の To-Be モデルを基に、責務ごとのディレクトリ分離を採用
- シナリオベース（サービス横断）とモジュール単位のテストを共存させる

### 追加提案（次回吟味する）

参考資料の調査を踏まえた追加提案。次回セッションで一つずつ検討する。

| 追加要素 | 提案理由 |
|---|---|
| `config/` (環境別設定) | 環境依存の設定を分離。AIがテスト修正時に環境設定を誤変更するリスク低減 |
| `eslint.config.js` | `@typescript-eslint/no-floating-promises` 等でAI生成コードの await 忘れを静的検出 |
| `tests/api/` | ブラウザE2Eと並行してAPIレベルE2Eも管理。UI変更に影響されない安定したリグレッション検知 |
| `scripts/` | テストデータのシード投入・クリーンアップ。AIがセットアップ手順を理解しやすくなる |

### 改訂構成案（次回の議論ベース）

```
e2e/
├── tests/
│   ├── scenarios/          # ブラウザ操作による業務フロー
│   ├── api/                # APIレベルE2E（ブラウザ不要）
│   │   ├── order/
│   │   └── product/
│   └── support/
│       ├── fixtures/       # カスタムフィクスチャ
│       └── helpers/        # テスト固有ヘルパー
├── pages/                  # Page Object Model
│   └── common/
├── utils/                  # 汎用ユーティリティ
├── config/                 # 環境別設定
│   ├── local.ts
│   └── ci.ts
├── test-data/              # テストデータ
├── global-setup.ts
├── playwright.config.ts
├── eslint.config.js
├── tsconfig.json
├── package.json
├── Dockerfile
├── .env.test
├── .gitignore
├── test-results/           # 実行時生成（スクリーンショット・動画・トレース、.gitignore対象）
└── playwright-report/      # 実行時生成（HTMLレポート、.gitignore対象）
```

---

## 参考資料

### 記事

- [Playwright E2Eテストの羅針盤 ～最適なディレクトリ設計～ (LiB)](https://note.com/libtec/n/n4b8406377ec6)
  - AI活用時のディレクトリ構成設計、シナリオベース vs サービス単位の共存、段階的移行ステップ
- [Scaling Playwright Test Automation (DZone)](https://dzone.com/articles/scaling-playwright-test-automation)
  - スケーラブルなフォルダ構成、POM、フィクスチャ、config/ 分離の実践ガイド
- [E2E Testing Best Practices (OneUptime)](https://oneuptime.com/blog/post/2026-01-30-e2e-testing-best-practices/view)
- [End-to-End Testing with Playwright and Docker (BrowserStack)](https://www.browserstack.com/guide/playwright-docker)

### Playwright公式ドキュメント

- [Best Practices](https://playwright.dev/docs/best-practices) — ロケーター戦略、テスト分離、CI最適化、ESLint推奨
- [Page Object Models](https://playwright.dev/docs/pom)
- [Authentication](https://playwright.dev/docs/auth) — storageState による認証状態共有
- [Fixtures](https://playwright.dev/docs/test-fixtures) — カスタムフィクスチャ
- [Global Setup and Teardown](https://playwright.dev/docs/test-global-setup-teardown)
- [Docker](https://playwright.dev/docs/docker) — コンテナ実行
- [Sharding](https://playwright.dev/docs/test-sharding) — CI並列化
- [MCP](https://playwright.dev/mcp/introduction) — AI連携インターフェース

---

## コンテナ構成の検討状況

### 決定事項

| 項目 | 決定 |
|---|---|
| compose分離方法 | ベース + オーバーライド（`compose.yaml` + `compose.e2e.yaml`）。E2Eでは同じインフラ定義を再利用し、差分のみオーバーライドする |
| backendステージ | `runtime`（ビルド済みJAR実行、本番と同じ方式） |
| frontendサービス | 最小限（nginx + 静的HTML）でパイプライン動作確認を先行。本実装は後から差し替え |
| ネットワーク | composeデフォルト（特別な設定不要）。E2E用 `.env.test` でURLをコンテナ名ベースに変更 |
| DB分離 | ベース compose.yaml の postgres をそのまま使用。E2E実行時は専用ボリュームで独立（開発環境と同時起動しない） |
| Keycloak分離 | ベース compose.yaml の keycloak をそのまま使用。同じ `demo-realm.json` で起動 |
| Redis分離 | ベース compose.yaml の redis をそのまま使用 |
| CI | 全コンテナを毎回起動→テスト→全破棄（`down -v` でクリーン実行） |
| ローカル | `down -v` で完全リセット。ボリュームを残せばマイグレーション再適用不要で繰り返し実行が速い |

#### DB分離方針の変更履歴（2026-06-01）

当初はスキーマ分離（`DB_SCHEMA=demo_e2e`、同一Postgresコンテナ内）を検討していたが、以下の理由でコンテナ分離に変更:

- **設定の差分が少ない** — backendの接続先URLを変えるだけ。スキーマ名切り替えロジックやinitサービスが不要
- **リセットが確実** — コンテナごと破棄すれば完全クリーン。TRUNCATE漏れやスキーマDROPの考慮が不要
- **事故リスクの排除** — 開発データとE2Eデータが物理的に分離され、誤操作の可能性がない
- **Liquibase設定がそのまま使える** — `defaultSchemaName` の切り替えが不要。開発と同じ設定で動作する
- **起動コストは無視できる** — Postgresの起動は数秒。E2Eテスト全体の実行時間に対して影響なし

### E2E用サービス構成

ベース `compose.yaml` のサービスをオーバーライドで再利用する。差分のみ記載:

```
postgres       (ベースそのまま、ポート公開無効化)
keycloak       (ベースそのまま、ポート公開無効化)
redis          (ベースそのまま、ポート公開無効化)
backend        (runtime ステージに変更、env_file を .env.test に差し替え)
grafana-lgtm   (無効化)
frontend-e2e   (追加: nginx + 静的HTML)
e2e-runner     (追加: Playwright、テスト実行後に破棄)
```

### 起動順序

```
postgres → keycloak → backend → frontend-e2e → e2e-runner
redis ──────────────↗
```

`depends_on` + `condition: service_healthy` で制御。

### インフラ分離方針（2026-06-01 確定）

オーバーライド方式でベース `compose.yaml` のインフラ定義を再利用する。E2E実行時は開発環境と同時起動しない前提で、同じサービス定義を共有する。

- **DRY** — postgres/keycloak/redis の設定が1箇所で管理される。変更時にE2E側の追従漏れがない
- **差分が明確** — `compose.e2e.yaml` には「E2Eで何が違うか」だけが記述される
- **クリーンさ** — `docker compose -f compose.yaml -f compose.e2e.yaml down -v` でボリューム含め全破棄
- **分離** — 開発環境とE2E環境は同時に起動しない。ポート公開を無効化し衝突を防止

### ボリューム戦略（2026-06-01 確定）

- **postgres-e2e**: named volume を使用
- **リセット方法**: `docker compose -f compose.yaml -f compose.e2e.yaml down -v` で明示リセット
- **責務分離**:
  - スキーマ構造 → Liquibase（差分適用。named volumeと相性良く、適用済みchangesetは再実行されない）
  - テストデータ → global-setup.ts で毎回 TRUNCATE → INSERT（ボリュームに依存せず常に最新データ）
- **CI**: 毎回コンテナ群を破棄するため方式に依存しない
- **ローカル**: named volumeによりマイグレーション再適用が不要で繰り返し実行が速い。クリーンにしたいときは `down -v`

### テストデータシード方針（2026-06-01 確定）

Playwright公式推奨の **Project Dependencies** 方式を採用する。

- **セットアップ方式**: `global.setup.ts` を setup project として定義（`globalSetup` config option ではない）
- **データ定義**: SQLファイル（`e2e/test-data/*.sql`）として分離
- **実行ロジック**: `global.setup.ts` がSQLファイルを読み込み、直接 PostgreSQL に TRUNCATE → INSERT を実行
- **接続ライブラリ**: 実装時に選定（`pg` or `postgres` 等）

**Project Dependencies を採用する理由**（Playwright公式推奨）:
- HTMLレポートにセットアップの結果が表示される
- トレースが記録される（失敗時のデバッグが容易）
- フィクスチャが使える
- リトライ・並列化が標準で効く

**SQLファイルで分離する理由**:
- セットアップロジック（`.ts`）とデータ定義（`.sql`）の責務が分かれる
- SQLファイルはDBAやバックエンド開発者にも読みやすい
- データ変更時に TypeScript を触る必要がない

```typescript
// playwright.config.ts
export default defineConfig({
  projects: [
    {
      name: 'setup db',
      testMatch: /global\.setup\.ts/,
      teardown: 'cleanup db',
    },
    {
      name: 'cleanup db',
      testMatch: /global\.teardown\.ts/,
    },
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
      dependencies: ['setup db'],
    },
  ]
});
```

```typescript
// tests/global.setup.ts
import { test as setup } from '@playwright/test';
import { readFileSync } from 'fs';
import { Client } from 'pg';

setup('seed test data', async ({}) => {
  const client = new Client({ connectionString: process.env.DATABASE_URL });
  await client.connect();
  const seed = readFileSync('./test-data/seed.sql', 'utf-8');
  await client.query(seed);
  await client.end();
});
```

### 未検討（優先度順）

本プロジェクトの目的は AI 開発基盤の構築であるため、AI 活用に直結する課題を優先する。

**高優先（AI 基盤に直結）**:
- [ ] ディレクトリ構成の追加提案を吟味・確定（AIがテスト生成・修正する際の配置ルール）
- [ ] `tests/` の分類についてコンポーネント×API疎通確認の扱い（有力案: `smoke/` ディレクトリで「画面を開く→データ表示される」レベルの最小疎通確認を分離）
- [ ] POMの分割単位の決定（ページ/ルート単位 vs コンポーネント単位。AIメンテナンス自動化の対象粒度に直結）
- [ ] AIメンテナンス自動化の具体的な実装方式（MCP活用、CIフック設計）

**低優先（AI 活用の余地が現状ない）**:
- [ ] GitHub Actions ワークフロー
- [ ] Keycloakの起動時間短縮策（optimized build イメージの事前作成、health check のタイムアウト設定）
- [ ] `global.setup.ts` / `global.teardown.ts` のシード実装

---

## 進捗ログ

### 2026-06-01: パイプライン疎通確認完了

**実施内容**:
- E2Eプロジェクト骨格を作成（`e2e/` ディレクトリ）
- `compose.e2e.yaml`（オーバーライド方式）を作成
- healthチェックテスト（3テスト）が全て pass
- Project Dependencies パターン（setup/teardown）の動作確認

**方針変更**:
- DB分離: スキーマ分離 → コンテナ分離（オーバーライドで実現）
- Keycloak/Redis: 共有 → オーバーライドで `container_name` を変更し開発環境と共存
- compose: スタンドアロン → オーバーライド方式（DRY）

**修正した既存バグ**:
- `backend/Dockerfile`: builder ステージで `buildSrc/` のコピーが欠落

**確認コマンド**:
```bash
TZ=UTC docker compose -f compose.yaml -f compose.e2e.yaml up --abort-on-container-exit e2e-runner
```

---

## ローカル実行手順

### 前提

- Docker / Docker Compose がインストール済み
- リポジトリルートで実行する

### E2Eテスト実行（全コンテナ起動 → テスト → 結果確認）

```bash
# 1. E2E環境を起動してテスト実行（初回はビルドに時間がかかる）
docker compose -f compose.yaml -f compose.e2e.yaml up --build --abort-on-container-exit e2e-runner

# 2. テスト結果を確認
#    - コンソール出力で pass/fail を確認
#    - 詳細: e2e/playwright-report/index.html を開く

# 3. 環境を停止
docker compose -f compose.yaml -f compose.e2e.yaml down

# 4. 完全リセット（ボリューム含む）
docker compose -f compose.yaml -f compose.e2e.yaml down -v
```

### ローカルで繰り返し実行する場合

```bash
# インフラ + backend を起動しておく（バックグラウンド）
docker compose -f compose.yaml -f compose.e2e.yaml up -d --build backend frontend-e2e

# テストだけ再実行
docker compose -f compose.yaml -f compose.e2e.yaml run --rm e2e-runner

# 特定テストだけ実行
docker compose -f compose.yaml -f compose.e2e.yaml run --rm e2e-runner npx playwright test tests/api/health.spec.ts
```

### ファイル構成

```
e2e/
├── tests/
│   ├── api/
│   │   └── health.spec.ts       # APIレベルhealthチェック
│   ├── global.setup.ts          # テストデータシード（スケルトン）
│   └── global.teardown.ts       # クリーンアップ（スケルトン）
├── frontend-stub/
│   └── index.html               # 最小限のフロントエンドスタブ
├── test-data/                   # SQLシードファイル（今後追加）
├── playwright.config.ts
├── package.json
├── tsconfig.json
├── Dockerfile
├── .env.test
└── .gitignore
compose.e2e.yaml                 # E2E専用compose（スタンドアロン）
```

---

## E2Eテスト実行速度の改善方針（2026-06-26 検討中）

### 課題

`--abort-on-container-exit` で毎回全コンテナを起動→テスト→停止すると、テスト実行まで 2〜3 分かかる。

| ボトルネック | 所要時間 | 原因 |
|---|---|---|
| backend Gradle ビルド | ~40秒 | fat JAR を毎回ビルド |
| Keycloak 起動 | ~30-60秒 | Java アプリ起動 + realm import |
| PostgreSQL + Redis 起動 | ~5秒 | 軽量 |

### 対策案（優先順）

#### 1. コンテナ起動とテスト実行の分離（即効性: 高）

インフラ＋アプリをバックグラウンドで起動しっぱなしにし、テストランナーだけ `run --rm` で実行する。

```bash
# 初回 or リビルド時のみ
docker compose -f compose.yaml -f compose.e2e.yaml up -d --build backend frontend-e2e

# テスト実行（2回目以降は数秒）
docker compose -f compose.yaml -f compose.e2e.yaml run --rm e2e-runner
```

Makefile ターゲット化して `make e2e-up` / `make e2e` / `make e2e-down` で操作する。

#### 2. Docker BuildKit cache mount（即効性: 中）

Gradle の依存キャッシュを Docker build 間で共有し、依存DLをスキップする。

```dockerfile
RUN --mount=type=cache,target=/root/.gradle ./gradlew dependencies --no-daemon || true
RUN --mount=type=cache,target=/root/.gradle ./gradlew bootJar --no-daemon -x test -x check
```

#### 3. Keycloak optimized build イメージ（即効性: 中）

開発用に `start-dev` ではなく、事前ビルド済み（`kc.sh build` 済み）イメージを用意する。起動時間が半分程度に短縮される。

```dockerfile
FROM quay.io/keycloak/keycloak:26.6.0 AS builder
ENV KC_DB=postgres
RUN /opt/keycloak/bin/kc.sh build

FROM quay.io/keycloak/keycloak:26.6.0
COPY --from=builder /opt/keycloak/ /opt/keycloak/
ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
CMD ["start", "--optimized", "--import-realm", "--health-enabled=true"]
```

#### 4. CI 向け: backend イメージの事前ビルド（GitHub Actions）

CI では backend イメージを別ジョブでビルド＆キャッシュし、E2E ジョブではキャッシュ済みイメージを使用する。

### 採用方針

- **ローカル開発**: 対策 1 を即採用。2, 3 は効果を計測して判断
- **CI**: 対策 1 + 4 を組み合わせる（GitHub Actions ワークフロー設計時に決定）

---

## AIテスト生成のルール強制（ハーネスエンジニアリング）（2026-06-26 検討中）

### 目的

AIがE2Eテストコードを生成する際、プロジェクト固有のルール（POM使用、命名規約、フィクスチャ、データセットアップ方法等）を確実に守らせる仕組みを構築する。

### 3層構造

| レイヤー | 役割 | 強制力 | backend での対応物 |
|---|---|---|---|
| 1. steering（コンテキスト注入） | AI に生成時点でルールを守らせる | ソフト（従わない可能性あり） | `.kiro/steering/*.md` |
| 2. ESLint（静的解析） | 書いた後にルール違反を検出 | ハード（CI ゲート） | PMD + SpotBugs |
| 3. scaffold（骨格生成） | 構造を先に決めて穴埋めさせる | ハード（配置が強制される） | `scaffold.sh` |

### レイヤー1: steering — サブエージェント方式

E2Eテスト専用のサブエージェントを作成し、backend の steering とコンテキストを分離する。

```
.kiro/agents/e2e-test.json        ← エージェント定義
  └── resources:
        └── steering/
              ├── e2e-test-standards.md
              ├── pom-rules.md
              ├── fixture-rules.md
              └── data-seeding-rules.md
```

メリット:
- コンテキスト効率（Java/Spring Boot のルールを読み込まない）
- 責務分離（E2E 用ルールのみ適用）
- 独立進化（frontend 技術スタック変更の影響を局所化）

### レイヤー2: ESLint — AI 自律修正ループ

**発火タイミング**: AIがテストコードを書いた後、Makefile ターゲット（`make e2e-check`）内で自動実行。

**ルール例**:
- `@typescript-eslint/no-floating-promises` — await 忘れ防止
- `no-restricted-syntax` — `page.locator()` の直接使用を禁止（POM 経由を強制）
- `no-restricted-imports` — 特定モジュールの直接 import を禁止

**メッセージ設計が重要**: ESLint のエラーメッセージ自体が AI への修正指示書として機能する。具体的な修正方法を含めること。

```typescript
// ✅ AI が修正方法を理解できるメッセージ
message: 'Direct locator usage is forbidden. Create or use a Page Object in pages/ directory instead.'
```

**自律修正フロー**:
```
AI がテストコードを生成
  → npx eslint --fix .（自動修正可能なものを解消）
  → npx eslint .（残った違反を検出）
  → AI が違反内容を読んで修正
  → pass するまで繰り返し（最大3回）
```

### レイヤー3: scaffold（今後設計）

backend の `scaffold.sh` と同じ発想。骨格を生成して AI に穴埋めさせることで、構造違反を事前に防ぐ。

### 実行コマンドの制限によるループ強制

エージェント設定の `toolsSettings.execute_bash.allowedCommands` で、テスト実行を必ず Makefile 経由に制限する。

```json
{
  "toolsSettings": {
    "execute_bash": {
      "allowedCommands": [
        "make e2e-lint",
        "make e2e-check",
        "make e2e-up",
        "make e2e-down"
      ]
    },
    "fs_write": {
      "allowedPaths": ["e2e/tests/**", "e2e/pages/**"],
      "deniedPaths": ["e2e/playwright.config.ts", "e2e/eslint.config.ts"]
    }
  }
}
```

効果:
- AI は `make e2e-check`（lint + テスト実行を含む）しか実行できない
- lint を飛ばして直接テストを走らせることが構造的に不可能
- 設定ファイルの書き換えも防止される

### 未決定事項

- [ ] ESLint カスタムルールの具体的な一覧
- [ ] POM の分割単位（ページ単位 vs コンポーネント単位）
- [ ] scaffold テンプレートの設計
- [ ] Makefile ターゲット（`e2e-lint`, `e2e-check`, `e2e-up`, `e2e-down`）の実装
- [ ] エージェント設定ファイル（`.kiro/agents/e2e-test.json`）の作成

### 進捗ログ

#### 2026-06-26: 方針議論

- 3層構造（steering + ESLint + scaffold）の方針を確定
- サブエージェント方式でコンテキスト分離する方針を確認
- ESLint の自律修正ループ（`--fix` → 残り違反 → AI修正 → 再実行）を設計
- `allowedCommands` による実行コマンド制限でループ強制する方針を確認
- テストサンプル（`category.spec.ts`）を作成（動作確認は次回）

#### 2026-07-06: コマンド資材展開

**確定した設計:**
- Makefile ターゲット: `e2e-up`, `e2e-check`, `e2e-only`, `e2e-lint`, `e2e-down`, `e2e-clean`
- `postToolUse` hook（Kiro の hooks 機能）で fs_write 後に即時 lint 実行
- `e2e-check` からは lint を除外（hook で即時実行済みのため二重実行しない）
- AI エージェント許可コマンド: `make e2e-check`, `make e2e-only` のみ
- POM はページ単位で確定（コンポーネント単位のテストはフロント UT の責務）

**作成した資材:**
- `Makefile` — e2e-* ターゲット 6 件追加
- `e2e/scripts/lint-on-write.sh` — postToolUse hook スクリプト
- `e2e/eslint.config.ts` — typescript-eslint + POM 強制ルール
- `.kiro/agents/e2e-test.json` — E2E テスト専用エージェント設定

**lint 即時実行の仕組み:**
```
AI が fs_write でテストファイルを書く
  → postToolUse hook 発火
  → lint-on-write.sh が対象パス判定
  → e2e/tests/** or e2e/pages/** なら ESLint 実行
  → 違反時: exit ≠ 0 → STDERR が AI に warning 表示 → AI が修正
  → 通過時: exit 0 → AI は次の作業へ
```

**フロントエンド構成の前提:**
- `yaguchi/frontend-setup` ブランチの構成をテスト対象の前提とする
- React 19 + TanStack Router + TanStack Query + Tailwind v4 + Shadcn/ui
- features 構成: category, product, pricing
- ルート: `/`, `/categories`, `/categories/:id`, `/products`, `/products/:id` 等
