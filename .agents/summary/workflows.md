# Workflows

## Development Workflow

### Environment Setup

```mermaid
graph TB
    A["01-setup-java.sh<br/>Amazon Corretto 25"] --> B["02-setup-viteplus.sh<br/>Vite+ CLI"]
    B --> C["03-setup-kiro.sh<br/>Kiro CLI"]
    C --> D["04-setup-shell.sh<br/>Shell 環境変数"]
    D --> E["05-setup-lsp.sh<br/>LSP サーバー"]
    E --> F["source ~/.bashrc"]
```

- **Platform**: WSL2 (Ubuntu 24.04) + Docker Desktop
- **IDE**: VSCode (Remote WSL)
- **Version Management**: `scripts/local-environment-setup/versions.env` で一元管理

### Backend Development

```mermaid
graph LR
    CODE[コード編集] --> FMT["./gradlew spotlessApply<br/>フォーマット"]
    FMT --> BUILD["./gradlew build<br/>ビルド + テスト"]
    BUILD --> CHECK{"check タスク"}
    CHECK -->|PMD| PMD[PMD 検査]
    CHECK -->|SpotBugs| SB[SpotBugs 検査]
    CHECK -->|JaCoCo| JC["カバレッジ ≥ 85%"]
    CHECK -->|ErrorProne| EP[NullAway 検査]
```

### Frontend Development

```mermaid
graph LR
    CODE[コード編集] --> DEV["vp dev<br/>開発サーバー"]
    CODE --> CHECK["vp check<br/>lint + format + type check"]
    CODE --> TEST["vp test<br/>Vitest"]
    CODE --> BUILD["vp build<br/>本番ビルド"]
```

### Local Run with Testcontainers

`TestDemoApplication.main()` を実行すると、Testcontainers が自動的に PostgreSQL と Grafana LGTM コンテナを起動し、`@ServiceConnection` により接続情報が自動設定される。

```mermaid
sequenceDiagram
    participant Dev as Developer
    participant TDA as TestDemoApplication
    participant TC as Testcontainers
    participant PG as PostgreSQL
    participant LGTM as Grafana LGTM
    participant APP as Spring Boot App

    Dev->>TDA: Run main()
    TDA->>TC: Start containers
    TC->>PG: Launch PostgreSQL
    TC->>LGTM: Launch Grafana LGTM
    TC-->>TDA: Connection info
    TDA->>APP: Start with TestcontainersConfiguration
    APP->>PG: Connect (auto-configured)
    APP->>LGTM: Send telemetry (auto-configured)
```

## CI/CD Workflow

### Dependabot

- **Gradle**: `/backend` ディレクトリを週次スキャン
- **npm**: `/frontend` ディレクトリを週次スキャン

### Quality Gates

```mermaid
graph TB
    PR[Pull Request] --> SP["Spotless Check<br/>(ratchetFrom origin/main)"]
    PR --> PMD["PMD Analysis"]
    PR --> SB["SpotBugs Analysis"]
    PR --> TEST["JUnit Tests"]
    TEST --> JC["JaCoCo ≥ 85%"]
    PR --> EP["ErrorProne + NullAway"]
    PR --> FE["vp check + vp test"]
```
