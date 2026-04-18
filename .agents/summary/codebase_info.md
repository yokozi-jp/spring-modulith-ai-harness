# Codebase Information

## Project

- **Name**: spring-modulith-ai-harness
- **License**: MIT (yokozi-jp)
- **Repository**: yokozi-jp/spring-modulith-ai-harness

## Technology Stack

### Backend

| Technology          | Version / Detail                  |
| :------------------ | :-------------------------------- |
| Language            | Java 25 (Amazon Corretto)         |
| Framework           | Spring Boot 4.0.5                 |
| Module System       | Spring Modulith 2.0.5             |
| Build Tool          | Gradle 9.4.1 (Groovy DSL)        |
| Database            | PostgreSQL (via Testcontainers)   |
| DB Migration        | Liquibase                         |
| ORM / Query         | jOOQ                              |
| Security            | Spring Security + OAuth2 Resource |
| API Docs            | SpringDoc OpenAPI 3.0.3           |
| Observability       | OpenTelemetry + Grafana LGTM      |
| Validation          | Bean Validation                   |
| Code Generation     | Lombok                            |
| Null Safety         | JSpecify + NullAway + ErrorProne  |
| Static Analysis     | SpotBugs, PMD 7.23.0, Spotless   |
| Mutation Testing    | Pitest                            |
| Code Coverage       | JaCoCo (minimum 85%)             |
| API Documentation   | Spring REST Docs + Asciidoctor    |

### Frontend

| Technology   | Version / Detail                |
| :----------- | :------------------------------ |
| Language     | TypeScript ~6.0.2               |
| Bundler      | Vite (via Vite+)                |
| Toolchain    | Vite+ (`vp` CLI)               |
| Package Mgr  | pnpm 10.33.0 (via Vite+)       |
| Linting      | Oxlint (type-aware, via Vite+)  |
| Formatting   | Oxfmt (via Vite+)               |

### DevOps / Tooling

| Tool              | Detail                                    |
| :---------------- | :---------------------------------------- |
| IDE               | VSCode (extensions.json で推奨拡張管理)    |
| Markdown Lint     | markdownlint-cli2                         |
| Dependency Update | Dependabot (Gradle weekly, npm weekly)    |
| Container         | Docker Desktop (WSL2)                     |
| Test Containers   | PostgreSQL, Grafana LGTM                  |
| AI Tooling        | Kiro CLI, AI-DLC workflow                 |

## Directory Structure

```
spring-modulith-ai-harness/
├── backend/                    # Spring Boot アプリケーション
│   ├── src/main/java/          # メインソースコード
│   ├── src/main/resources/     # 設定ファイル、DB マイグレーション
│   ├── src/test/java/          # テストコード
│   ├── config/pmd/             # PMD ルールセット
│   ├── build.gradle            # Gradle ビルド設定
│   └── gradle/                 # Gradle Wrapper
├── frontend/                   # Vite+ TypeScript アプリケーション
│   ├── src/                    # TypeScript ソースコード
│   ├── public/                 # 静的アセット
│   ├── package.json            # npm パッケージ定義
│   ├── vite.config.ts          # Vite+ 設定
│   ├── tsconfig.json           # TypeScript 設定
│   └── AGENTS.md               # フロントエンド用 AI エージェントガイド
├── scripts/                    # セットアップスクリプト
│   └── local-environment-setup/
├── docs/                       # プロジェクトドキュメント
├── .github/                    # GitHub 設定 (Dependabot)
├── .vscode/                    # VSCode 設定
├── .kiro/                      # Kiro CLI / AI-DLC 設定
├── README.md
├── CONTRIBUTING.md
└── LICENSE
```

## Languages

| Language   | Files | Status    |
| :--------- | ----: | :-------- |
| Java       |     4 | Supported |
| TypeScript |     3 | Supported |
| Groovy DSL |     2 | Supported |
| Shell      |     5 | Supported |
| YAML       |     5 | Supported |
| HTML       |     1 | Supported |
| CSS        |     1 | Supported |
| XML        |     1 | Supported |

## Project Status

初期段階のプロジェクト。バックエンドは Spring Boot のスキャフォールディングが完了し、品質ツールチェーン（Spotless, PMD, SpotBugs, JaCoCo, Pitest, ErrorProne/NullAway）が設定済み。フロントエンドは Vite+ テンプレートの初期状態。ビジネスロジックの実装はこれから。
