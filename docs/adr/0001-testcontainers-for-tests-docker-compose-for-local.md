# Testcontainers for automated tests, Docker Compose for local development only

ローカル手動開発と自動テストで外部依存（DB, Keycloak 等）の起動方式を分離する。ローカル開発は Docker Compose + `bootRun`（ホットリロード有効）、自動テストは Testcontainers（`@SpringBootTest` 内で自動起動・自動破棄）を使う。Docker Compose をテストに流用しない。

## Considered Options

1. **Docker Compose 統一** — ローカルもテストも同じ `docker-compose.yaml` で外部依存を起動する
2. **Testcontainers 統一** — ローカル開発も `bootTestRun` + Testcontainers で起動する
3. **分離（採用）** — ローカルは Docker Compose、テストは Testcontainers

Option 1 を採用しない理由: テストごとにクリーンな状態が保証されない、CI で `docker compose up/down` の管理が必要、固定ポートで衝突リスクがある。Option 2 を採用しない理由: Testcontainers のコンテナは揮発性でデータが残らないため、SQL の実験やデータの永続化ができない。

## Consequences

- テストデータはテストごとに `@Sql` / Builder パターンで個別投入する。ローカル用の `LocalDataSeeder` とは共有しない。
- コンテナイメージのバージョンは Docker Compose と Testcontainers で個別に管理する（頻繁に変わらないため二重管理のコストは低い）。
- Keycloak は Spring Boot 4 に `@ServiceConnection` がないため、`DynamicPropertyRegistrar` で `issuer-uri` を動的設定する。大半の API テストでは Keycloak コンテナを使わず `jwt()` モックで済ませ、認証フロー自体のテストのみ実コンテナを使う。
- フロントエンド E2E テスト（Playwright 等）が必要になった時点で、アプリをコンテナ化した Docker Compose 構成と `@Tag("e2e")` による分離を導入する。
