# Application YAML configuration strategy: base + profile override, no default values for secrets

全環境共通の設定を `main/resources/application.yaml` に集約し、環境固有の値はプロファイル別ファイルまたは環境変数で上書きする。シークレットや環境固有の接続先にはデフォルト値を設定せず、未設定時に即エラーにする。

## ファイル構成

| ファイル                                  | 用途                                                             | 読まれるタイミング                   |
| ----------------------------------------- | ---------------------------------------------------------------- | ------------------------------------ |
| `main/resources/application.yaml`         | 全環境共通ベース（Jackson, jOOQ, Liquibase, Modulith 等）        | 常に                                 |
| `main/resources/application-default.yaml` | ローカル開発用上書き（DB 接続先, OAuth2 issuer-uri, debug ログ） | `bootRun` 時（default プロファイル） |
| `test/resources/application-default.yaml` | テスト用上書き（OAuth2 ダミー値等）                              | `./gradlew test` 時                  |
| `main/resources/messages.properties`      | バリデーション・アプリメッセージ                                 | 常に                                 |

## Considered Options

**環境ごとに `application-prod.yaml`, `application-stg.yaml` を作るか？**

作らない（採用）。本番・STG の差分は OAuth2 issuer-uri や DB 接続先程度であり、環境変数（`${OAUTH2_ISSUER_URI}`）で注入すれば十分。設定ファイルにシークレットや環境固有値を入れるとリポジトリにコミットされるリスクがある。

**`${OAUTH2_ISSUER_URI:}` のようにデフォルト空値を許容するか？**

しない（採用）。デフォルト空値を設定すると、本番で環境変数の設定漏れがあっても起動してしまい、実行時に不可解なエラーになる。未設定時は起動失敗させて即座に気づけるようにする。テスト時は `test/resources/application-default.yaml` でダミー値を設定し、`DynamicPropertyRegistrar` が Testcontainers の実 URL で上書きする。

**`test/resources/application.yaml` を使うか？**

使わない（採用）。`test/resources/application.yaml` は `main/resources/application.yaml` と同名のため、テスト時に main 側を丸ごと上書きしてしまう。共通ベース設定（Jackson, Liquibase 等）が消えるため、プロファイル別ファイル `test/resources/application-default.yaml` で差分のみ上書きする方式を採用した。

## Consequences

- 本番デプロイ時は `OAUTH2_ISSUER_URI`, `SPRING_DATASOURCE_URL` 等を環境変数で必ず設定する必要がある。未設定なら起動失敗する。
- `bootTestRun` 時は `test/resources/application-default.yaml` が `main/resources/application-default.yaml` より優先される（同名ファイルは test クラスパスが優先）。
- `messages.properties` は Spring Boot の `MessageSource` に自動登録され、Bean Validation のメッセージ解決にも使われる（`ValidationMessages.properties` は不要）。
