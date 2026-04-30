# Container-based development with Docker Compose, Testcontainers for automated tests

ローカル開発はコンテナ内 `bootRun`（Docker Compose + バインドマウント + DevTools ホットリロード）で行う。自動テストは Testcontainers（`@SpringBootTest` 内で自動起動・自動破棄）を使う。Docker Compose をテストに流用しない。

## Considered Options

1. **Docker Compose 統一** — ローカルもテストも同じ `compose.yaml` で外部依存を起動する
2. **Testcontainers 統一** — ローカル開発も `bootTestRun` + Testcontainers で起動する
3. **ホスト JVM で bootRun + Docker Compose はインフラのみ** — アプリはホスト上で直接実行する
4. **コンテナ内 bootRun（採用）** — ソースをバインドマウントし、コンテナ内で `bootRun` を実行する

Option 1 を採用しない理由: テストごとにクリーンな状態が保証されない、CI で `docker compose up/down` の管理が必要、固定ポートで衝突リスクがある。Option 2 を採用しない理由: Testcontainers のコンテナは揮発性でデータが残らないため、SQL の実験やデータの永続化ができない。Option 3 を採用しない理由: `application-default.yaml` にダミー値が必要になる、`spring-boot-docker-compose` の自動注入と環境変数の二重管理が発生する、OTLP のプロパティ名がシグナルごとに異なり設定が複雑化する。コンテナ内 bootRun なら `.env` で全環境変数を一元管理でき、設定の見通しが良い。

## 採用理由

- `.env` で全環境変数を一元管理できる（`application-default.yaml` のダミー値問題がない）
- `docker compose up --build` の一コマンドで全サービスが起動する
- 本番と同じ Docker ネットワーク内で動作するため、サービス間の接続設定が統一される
- WSL 環境ではバインドマウントの性能問題が Windows ネイティブほど深刻ではない
- Gradle キャッシュを named volume でマウントすることで、2 回目以降のビルドは高速

## トレードオフ

- 初回ビルドは Gradle 依存ダウンロードで時間がかかる（2 回目以降はキャッシュで高速）
- IDE デバッグはリモート接続（JDWP、localhost:5005）のみ
- ホスト JVM での直接実行より若干遅い

## 構成

### Dockerfile（マルチステージ）

| ステージ | ベースイメージ | 用途 |
|---|---|---|
| dev | `amazoncorretto:25` | 開発用。バインドマウント + bootRun |
| builder | `amazoncorretto:25` | fat JAR ビルド |
| runtime | `amazoncorretto:25-headless` | 本番用。JRE + fat JAR |

### compose.yaml

- `docker compose up --build` で全サービス起動（インフラ + アプリ）
- app サービスは dev ステージをターゲットにし、ソースをバインドマウント
- Gradle キャッシュは named volume（`gradle-cache`）で永続化
- 環境変数は `.env` から `env_file` で読み込み

### デバッグ

- JDWP は `build.gradle` の `bootRun.jvmArgs` で設定（`JAVA_TOOL_OPTIONS` は Gradle Daemon にも適用されるため不可）
- IDE から `localhost:5005` にリモートデバッグ接続
- VS Code: `.vscode/launch.json` に設定済み

### テスト

- `./gradlew check` はホストで実行（Testcontainers が DB・Keycloak を自動起動）
- Docker Compose のインフラは不要

### 既知の制約: Keycloak issuer-uri

app コンテナから Keycloak には `http://keycloak:8080` でアクセスする。Keycloak が発行する JWT の `iss` クレームはリクエストの Host ヘッダに基づくため、コンテナ内からのアクセスでは `http://keycloak:8080/realms/demo` が issuer になる。ブラウザ経由のトークン（issuer=`localhost:8080`）とは不一致になるため、フロントエンド E2E テストでは Keycloak の `KC_HOSTNAME` 設定またはリバースプロキシで issuer を統一する必要がある（フロントエンドコンテナ化時に対応）。

## Consequences

- テストデータはテストごとに `@Sql` / Builder パターンで個別投入する。ローカル用の `LocalDataSeeder` とは共有しない。
- コンテナイメージのバージョンは Docker Compose と Testcontainers で個別に管理する。
- Keycloak は Spring Boot 4 に `@ServiceConnection` がないため、`DynamicPropertyRegistrar` で `issuer-uri` を動的設定する。大半の API テストでは Keycloak コンテナを使わず `jwt()` モックで済ませ、認証フロー自体のテストのみ実コンテナを使う。
