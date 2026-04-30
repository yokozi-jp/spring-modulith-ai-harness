# Testcontainers for automated tests, Docker Compose for local development only

ローカル手動開発と自動テストで外部依存（DB, Keycloak 等）の起動方式を分離する。ローカル開発は Docker Compose + `bootRun`（ホットリロード有効）、自動テストは Testcontainers（`@SpringBootTest` 内で自動起動・自動破棄）を使う。Docker Compose をテストに流用しない。

## Considered Options

1. **Docker Compose 統一** — ローカルもテストも同じ `docker-compose.yaml` で外部依存を起動する
2. **Testcontainers 統一** — ローカル開発も `bootTestRun` + Testcontainers で起動する
3. **分離（採用）** — ローカルは Docker Compose、テストは Testcontainers
4. **ローカル開発でもアプリをコンテナ化** — ソースをバインドマウントし、コンテナ内で `bootRun` を実行する

Option 1 を採用しない理由: テストごとにクリーンな状態が保証されない、CI で `docker compose up/down` の管理が必要、固定ポートで衝突リスクがある。Option 2 を採用しない理由: Testcontainers のコンテナは揮発性でデータが残らないため、SQL の実験やデータの永続化ができない。Option 4 を採用しない理由: Docker のバインドマウント経由ではファイル変更イベントが遅延・欠落し DevTools のホットリロードが不安定になる、Gradle キャッシュの I/O オーバーヘッドでビルドが遅くなる、IDE デバッガのリモート接続設定が必要になる、コンテナ再起動のたびに Gradle Daemon が失われ起動が遅い。開発環境は WSL 上で動作しておりバインドマウントの問題は Windows ネイティブほど深刻ではないが、ローカル JVM で `bootRun` する方が総合的に快適であるため採用しない。

## Consequences

- テストデータはテストごとに `@Sql` / Builder パターンで個別投入する。ローカル用の `LocalDataSeeder` とは共有しない。
- コンテナイメージのバージョンは Docker Compose と Testcontainers で個別に管理する（頻繁に変わらないため二重管理のコストは低い）。
- Keycloak は Spring Boot 4 に `@ServiceConnection` がないため、`DynamicPropertyRegistrar` で `issuer-uri` を動的設定する。大半の API テストでは Keycloak コンテナを使わず `jwt()` モックで済ませ、認証フロー自体のテストのみ実コンテナを使う。
- フロントエンド E2E テスト（Playwright 等）が必要になった時点で、アプリをコンテナ化した Docker Compose 構成と `@Tag("e2e")` による分離を導入する。

## コンテナ確認モード（2026-04-30 追記）

日常開発は引き続き `bootRun`（ホスト JVM）で行うが、本番イメージの動作確認・E2E テスト・CI 用に compose profiles でアプリコンテナを起動できるようにした。

### 使い分け

| モード | コマンド | 用途 |
|---|---|---|
| 日常開発 | `docker compose up -d` + `./gradlew bootRun` | ホットリロード、IDE デバッグ |
| コンテナ確認 | `docker compose --profile app up --build` | 本番イメージ検証、E2E テスト |

### 構成

- `backend/Dockerfile`: マルチステージビルド（builder: Corretto 25 JDK → runtime: Corretto 25 headless）。本番デプロイでもこの Dockerfile をそのまま使う。
- `compose.yaml` の `app` サービス: `profiles: [app]` で日常開発時には起動しない。`environment` で DB・OAuth2 の URL を Docker ネットワーク内アドレスに上書き。
- JDWP デバッグポート（デフォルト 5005）を公開。IDE からリモートデバッグ接続可能。

### 既知の制約: Keycloak issuer-uri の不一致

app コンテナから Keycloak には `http://keycloak:8080` でアクセスするが、ブラウザ経由で取得したトークンの `iss` クレームは `http://localhost:8080/realms/demo` になる。このため、ブラウザ → app コンテナの認証フローでは issuer 不一致エラーが発生する。対処方法:

- API の直接テスト（curl 等）では、app コンテナ内から Keycloak にトークンを要求すれば issuer が一致する。
- フロントエンドも含めた E2E テストでは、Keycloak の `KC_HOSTNAME` 設定またはリバースプロキシで issuer を統一する（フロントエンドコンテナ化時に対応）。
