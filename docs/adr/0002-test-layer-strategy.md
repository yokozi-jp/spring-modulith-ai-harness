# Test layer strategy: unit, integration, API E2E, UI E2E

自動テストを4層に分類し、各層でアプリの起動方式・外部依存の管理方式・テストデータの投入方式を定める。現時点ではフロントエンドがないため UI E2E は将来方針のみ記載する。

## テスト層の定義

### Unit テスト

- **対象:** 単一クラスのビジネスロジック（ドメインモデル、値オブジェクト、ファクトリ等）
- **アプリ起動:** なし（Spring コンテキスト不要）
- **外部依存:** なし（モック or スタブ）
- **テストデータ:** テストメソッド内で直接生成
- **命名規約:** `*Test.java`
- **認証:** 不要

### Integration テスト

- **対象:** 複数コンポーネントの結合（コマンドハンドラ → リポジトリ → DB、コントローラ → セキュリティ等）
- **アプリ起動:** `@SpringBootTest`（JVM 内）
- **外部依存:** Testcontainers（PostgreSQL, Keycloak, Grafana LGTM を自動起動・自動破棄）
- **テストデータ:** テストごとに `@Sql` / Builder パターンで個別投入。共通 Seeder は使わない。
- **命名規約:** `*IntegrationTest.java`
- **認証:** 大半は `jwt()` モックで済ませる。認証フロー自体を検証するテストのみ Keycloak 実コンテナを使用。

### API E2E テスト（フロントエンドなし）

- **対象:** HTTP エンドポイントのフルフロー（リクエスト → 認証 → ビジネスロジック → DB → レスポンス）
- **アプリ起動:** `@SpringBootTest(webEnvironment = RANDOM_PORT)`（実 HTTP サーバー起動）
- **外部依存:** Testcontainers（Integration テストと同じ）
- **テストデータ:** テストごとに個別投入
- **命名規約:** `*IntegrationTest.java`（Integration テストと同じ仕組みのため区別しない）
- **認証:** 実トークン取得 or `jwt()` モック（テスト目的による）
- **備考:** ブラウザを使わないため Integration テストと実行コストがほぼ同じ。`@Tag` による分離は不要。

### UI E2E テスト（将来）

- **対象:** ブラウザ経由のユーザー操作フロー（画面遷移、フォーム入力、表示確認）
- **アプリ起動:** Docker Compose でコンテナ化（`@SpringBootTest` と統合不可）
- **外部依存:** Docker Compose（PostgreSQL, Keycloak, バックエンド, フロントエンド）
- **テストデータ:** init スクリプト or Seeder
- **ツール:** Playwright（フロントエンド側で TypeScript で記述）
- **認証:** Keycloak 実コンテナ経由
- **備考:** 導入時に `@Tag("e2e")` を追加し `./gradlew test` から除外する。API のビジネスロジック検証は Java 側の Integration テストで行い、Playwright は UI 操作フローの検証に限定する。

## Considered Options

**Integration テストと API E2E テストを分離するか？**

分離しない（採用）。API E2E は `@SpringBootTest(webEnvironment = RANDOM_PORT)` で実 HTTP サーバーを起動する点だけが異なるが、外部依存の管理・テストデータ投入・実行コストは Integration テストと同じ。命名やタグで分けると管理コストが増えるだけで実益がない。

**テストデータを共通 Seeder で投入するか？**

しない（採用）。共通 Seeder を使うとテスト間でデータが暗黙的に共有され、ローカル用データの変更がテストを壊す、テストに不要なデータが混入する、テストの前提条件がコードから読み取れない、という問題が起きる。テストごとに必要なデータだけを明示的に投入する。

**全テストで Keycloak 実コンテナを使うか？**

使わない（採用）。Keycloak コンテナの起動は数秒〜十数秒かかる。大半の API テストは「認証済みユーザーがこの操作をしたらどうなるか」の検証であり、Spring Security の `jwt()` モックで十分。実コンテナはトークン取得・JWT 検証・リフレッシュなど認証フロー自体のテストに限定する。

## Consequences

- スキーマ管理（Liquibase）は全テスト層で共通。テストデータのみ層ごとに異なる。
- `TestcontainersConfiguration` に全コンテナ（PostgreSQL, Keycloak, Grafana LGTM）を集約し、`@Import` で共有する。テストコンテキストキャッシュにより同一設定のテストクラス間でコンテナは再利用される。
- `@ActiveProfiles`, `@MockitoBean` 等でコンテキスト設定を変えるとコンテナが再作成されるため、コンテキスト設定はなるべく統一する。
