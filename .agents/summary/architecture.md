# Architecture

## System Overview

spring-modulith-ai-harness は、Spring Modulith ベースのモジュラーモノリスアーキテクチャを採用したフルスタックアプリケーション。バックエンド（Spring Boot）とフロントエンド（Vite+ TypeScript）の2層構成。

```mermaid
graph TB
    subgraph Frontend["Frontend (Vite+ / TypeScript)"]
        UI[Browser SPA]
    end

    subgraph Backend["Backend (Spring Boot 4.0.5)"]
        SEC[Spring Security<br/>OAuth2 Resource Server]
        WEB[Spring WebMVC]
        MOD[Spring Modulith Core]
        JOOQ[jOOQ]
        LIQ[Liquibase]
        OBS[OpenTelemetry]
    end

    subgraph Infrastructure
        PG[(PostgreSQL)]
        LGTM[Grafana LGTM Stack]
    end

    UI -->|HTTP| SEC
    SEC --> WEB
    WEB --> MOD
    MOD --> JOOQ
    JOOQ --> PG
    LIQ -->|Migration| PG
    OBS -->|Traces, Metrics, Logs| LGTM
```

## Architectural Patterns

### Modular Monolith (Spring Modulith)

Spring Modulith によるモジュール境界の強制。パッケージ `com.example.demo` 配下にモジュールを配置し、モジュール間の依存関係を Spring Modulith が検証する。

### Layered Architecture

各モジュール内は以下のレイヤー構成を想定：

```mermaid
graph TB
    API[API Layer<br/>Controller / REST Endpoints] --> SVC[Service Layer<br/>Business Logic]
    SVC --> REPO[Persistence Layer<br/>jOOQ Queries]
    REPO --> DB[(PostgreSQL)]
```

### Security Architecture

OAuth2 Resource Server パターンを採用。JWT トークンによるステートレス認証。

### Observability Architecture

OpenTelemetry + Spring Modulith Observability によるトレース・メトリクス・ログの統合。Grafana LGTM（Loki, Grafana, Tempo, Mimir）スタックで可視化。

## Design Decisions

| Decision                     | Rationale                                                    |
| :--------------------------- | :----------------------------------------------------------- |
| Spring Modulith              | モジュール境界の強制と将来のマイクロサービス分割への備え     |
| jOOQ over JPA                | 型安全な SQL クエリと複雑なクエリの表現力                    |
| Liquibase                    | バージョン管理されたデータベースマイグレーション             |
| OAuth2 Resource Server       | ステートレスな API 認証                                      |
| Testcontainers               | 本番同等のインフラでの統合テスト                             |
| JSpecify + NullAway          | コンパイル時の null 安全性保証                               |
| Vite+ (frontend)             | Vite, Oxlint, Oxfmt, Vitest の統合ツールチェーン            |

## Quality Architecture

```mermaid
graph LR
    subgraph Static["Static Analysis"]
        SP[Spotless<br/>Code Formatting]
        PMD[PMD 7.23<br/>Code Rules]
        SB[SpotBugs<br/>Bug Detection]
        EP[ErrorProne + NullAway<br/>Null Safety]
    end

    subgraph Dynamic["Dynamic Analysis"]
        JU[JUnit 5<br/>Unit Tests]
        TC[Testcontainers<br/>Integration Tests]
        JC["JaCoCo ≥ 85%<br/>Coverage"]
        PI[Pitest<br/>Mutation Testing]
        RD[REST Docs<br/>API Documentation]
    end

    subgraph Frontend_QA["Frontend QA"]
        OX[Oxlint<br/>Type-Aware Linting]
        OF[Oxfmt<br/>Formatting]
        TS[TypeScript<br/>Type Checking]
    end
```
