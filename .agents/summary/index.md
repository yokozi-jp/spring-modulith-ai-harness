# Documentation Index

> このファイルは AI アシスタントがコードベースを理解するためのナレッジベースインデックスです。
> 質問に応じて、以下のファイルを参照してください。

## How to Use This Index

1. まずこの `index.md` を読み、質問に関連するファイルを特定する
2. 該当するファイルを読み込んで詳細情報を取得する
3. 複数のファイルにまたがる質問の場合は、関連するファイルをすべて参照する

## File Reference

| File                                      | Purpose                                | When to Consult                                                                |
| :---------------------------------------- | :------------------------------------- | :----------------------------------------------------------------------------- |
| [codebase_info.md](codebase_info.md)      | 技術スタック、ディレクトリ構造、言語   | プロジェクト概要、使用技術、ディレクトリ構成について質問されたとき             |
| [architecture.md](architecture.md)        | システムアーキテクチャ、設計パターン   | アーキテクチャ判断、設計パターン、レイヤー構成について質問されたとき           |
| [components.md](components.md)            | 主要コンポーネントと責務               | 特定のクラス/モジュールの役割、コンポーネント間の関係について質問されたとき     |
| [interfaces.md](interfaces.md)            | API、インターフェース、統合ポイント    | REST API、セキュリティ、DB接続、Observability について質問されたとき           |
| [data_models.md](data_models.md)          | データ構造、モデル、null安全性         | データベーススキーマ、jOOQ、null safety について質問されたとき                 |
| [workflows.md](workflows.md)             | 開発ワークフロー、CI/CD                | 開発手順、ビルド方法、テスト実行、環境構築について質問されたとき               |
| [dependencies.md](dependencies.md)        | 外部依存関係とその用途                 | ライブラリ、プラグイン、バージョン、依存関係管理について質問されたとき         |

## Quick Summary

### Project Identity

Spring Modulith ベースのモジュラーモノリス。バックエンド (Spring Boot 4.0.5 / Java 25) + フロントエンド (Vite+ / TypeScript)。初期段階でビジネスロジック未実装。

### Key Characteristics

- **Module System**: Spring Modulith 2.0.5 によるモジュール境界の強制
- **Data Access**: jOOQ (型安全 SQL) + Liquibase (マイグレーション) + PostgreSQL
- **Security**: OAuth2 Resource Server (JWT)
- **Observability**: OpenTelemetry → Grafana LGTM
- **Quality**: Spotless, PMD, SpotBugs, JaCoCo (≥85%), Pitest, ErrorProne/NullAway
- **Null Safety**: JSpecify + NullAway (コンパイル時チェック)
- **Frontend Toolchain**: Vite+ (`vp` CLI) — Vite, Oxlint, Oxfmt, Vitest を統合

### Cross-Cutting Concerns

| Concern         | Backend                              | Frontend                |
| :-------------- | :----------------------------------- | :---------------------- |
| Formatting      | Spotless (Google Java Format)        | Oxfmt (via Vite+)      |
| Linting         | PMD, SpotBugs, ErrorProne            | Oxlint (via Vite+)     |
| Testing         | JUnit 5 + Testcontainers            | Vitest (via Vite+)     |
| Type Safety     | JSpecify + NullAway                  | TypeScript strict mode  |
| Dependency Mgmt | Spring BOM + Gradle                  | pnpm catalogs           |
| Auto-update     | Dependabot (weekly)                  | Dependabot (weekly)     |
