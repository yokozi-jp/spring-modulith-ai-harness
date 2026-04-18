# Dependencies

## Backend Dependencies

### Core Framework

| Dependency                                          | Scope          | Purpose                          |
| :-------------------------------------------------- | :------------- | :------------------------------- |
| `spring-boot-starter-webmvc`                        | implementation | REST API (Spring MVC)            |
| `spring-boot-starter-security`                      | implementation | 認証・認可                       |
| `spring-boot-starter-security-oauth2-resource-server` | implementation | OAuth2 JWT 認証                |
| `spring-boot-starter-validation`                    | implementation | Bean Validation                  |
| `spring-boot-starter-actuator`                      | implementation | ヘルスチェック・メトリクス       |
| `spring-modulith-starter-core`                      | implementation | モジュール境界の定義・検証       |
| `spring-modulith-actuator`                          | runtimeOnly    | モジュール構造の Actuator 公開   |
| `spring-modulith-observability`                     | runtimeOnly    | モジュール単位の Observability   |

### Data Access

| Dependency                                | Scope          | Purpose                    |
| :---------------------------------------- | :------------- | :------------------------- |
| `spring-boot-starter-jooq`               | implementation | jOOQ 統合                  |
| `spring-boot-starter-liquibase`          | implementation | DB マイグレーション        |
| `postgresql`                              | runtimeOnly    | PostgreSQL JDBC ドライバ   |

### Observability

| Dependency                                    | Scope          | Purpose                |
| :-------------------------------------------- | :------------- | :--------------------- |
| `spring-boot-starter-opentelemetry`          | implementation | OpenTelemetry 統合     |

### API Documentation

| Dependency                                    | Scope          | Purpose                |
| :-------------------------------------------- | :------------- | :--------------------- |
| `springdoc-openapi-starter-webmvc-ui:3.0.3`  | implementation | Swagger UI + OpenAPI   |
| `spring-boot-starter-restdocs`               | test           | REST Docs テスト       |
| `spring-restdocs-mockmvc`                     | test           | MockMvc REST Docs      |

### Code Quality (Compile-time)

| Dependency                          | Scope               | Purpose                    |
| :---------------------------------- | :------------------- | :------------------------- |
| `lombok`                            | compileOnly/annotationProcessor | ボイラープレート削減 |
| `jspecify:1.0.0`                    | implementation       | Null safety アノテーション |
| `error_prone_core:2.49.0`          | errorprone           | コンパイル時バグ検出       |
| `nullaway:0.13.2`                   | errorprone           | Null 安全性チェック        |
| `spotbugs-annotations:4.9.3`       | compileOnly          | SpotBugs アノテーション    |

### Testing

| Dependency                                              | Scope | Purpose                    |
| :------------------------------------------------------ | :---- | :------------------------- |
| `spring-boot-testcontainers`                            | test  | Testcontainers 統合        |
| `testcontainers-postgresql`                             | test  | PostgreSQL コンテナ        |
| `testcontainers-grafana`                                | test  | Grafana LGTM コンテナ     |
| `testcontainers-junit-jupiter`                          | test  | JUnit 5 統合               |
| `spring-modulith-starter-test`                          | test  | モジュール構造テスト       |
| `spring-boot-starter-*-test` (各種)                     | test  | スターター別テストサポート |

### Gradle Plugins

| Plugin                          | Version | Purpose                    |
| :------------------------------ | :------ | :------------------------- |
| `org.springframework.boot`      | 4.0.5   | Spring Boot ビルド         |
| `io.spring.dependency-management` | 1.1.7 | 依存関係管理               |
| `com.diffplug.spotless`         | 8.4.0   | コードフォーマット         |
| `pmd`                           | (built-in) | 静的解析                |
| `com.github.spotbugs`           | 6.5.0   | バグ検出                   |
| `info.solidsoft.pitest`         | 1.19.0  | ミューテーションテスト     |
| `jacoco`                        | (built-in) | コードカバレッジ        |
| `net.ltgt.errorprone`           | 5.1.0   | ErrorProne 統合            |
| `org.asciidoctor.jvm.convert`   | 4.0.5   | Asciidoctor ドキュメント   |

## Frontend Dependencies

| Dependency   | Version  | Scope | Purpose              |
| :----------- | :------- | :---- | :------------------- |
| `typescript` | ~6.0.2   | dev   | TypeScript コンパイラ |
| `vite`       | catalog  | dev   | Vite (via Vite+)     |
| `vite-plus`  | catalog  | dev   | 統合ツールチェーン   |

## Dependency Management

- **Backend**: Spring Modulith BOM (`2.0.5`) + Spring Boot dependency management
- **Frontend**: pnpm workspace catalogs (`pnpm-workspace.yaml`)
- **Auto-update**: Dependabot (weekly, Gradle + npm)
