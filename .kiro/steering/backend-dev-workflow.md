# Backend 開発ワークフロー

本プロジェクトの backend コード変更時に従うワークフロー。
すべての作業は開発コンテナ内（`docker compose exec backend bash`）で実行する。

---

## 1. モジュール・クラスの作成

### 新規モジュール

```bash
cd backend && ./scripts/scaffold.sh module <module-name> [--display-name <表示名>]
```

- モジュール名は小文字英数字のみ（例: `order`, `shipping`）
- `package-info.java`（`@NullMarked` 付き）が自動生成される
- `--display-name` を指定すると `@ApplicationModule(displayName = "...")` が付与される（ドキュメント生成時の表示名）
- 作成後にアーキテクチャテストが自動実行される（`--no-test` でスキップ可）
- `--dry-run` で作成予定ファイルのプレビューが可能

### 新規クラス

```bash
cd backend && ./scripts/scaffold.sh class <module> <layer> <name> [--aggregate <Aggregate>]
```

- 必ず `scaffold.sh module` でモジュールを先に作成してから実行する
- ディレクトリと `package-info.java` は自動生成される（手動作成しない）
- `<name>` にはレイヤーサフィックスを含めない（scaffold が自動付与する）
  - ✅ `./scripts/scaffold.sh class category command UpdateCategory` → `UpdateCategoryCommand.java`
  - ❌ `./scripts/scaffold.sh class category command UpdateCategoryCommand` → `UpdateCategoryCommandCommand.java`
- `aggregate` 生成時は Identifier・Factory・Repository・RepositoryImpl が自動連鎖生成される
- `entity` 生成時は `--aggregate` 必須。Identifier・Factory・IdGenerator・IdGeneratorImpl が自動連鎖生成される
- `exception` 生成時は ExceptionHandler（`@RestControllerAdvice`）が自動連鎖生成される

#### layer 一覧

| layer | 配置先 | 生成物 |
|---|---|---|
| `event` | `event/` | record（`@DomainEvent`） |
| `exception` | `exception/` | class（`RuntimeException` 継承、`@NamedInterface("exception")` で公開）+ ExceptionHandler |
| `aggregate` | `domain/model/aggregate/` | class（`AggregateRoot` 実装）+ Identifier + Factory + Repository + RepositoryImpl |
| `entity` | `domain/model/entity/` | class（`Entity` 実装）+ Identifier + Factory（`@Factory`）+ IdGenerator + IdGeneratorImpl（`@Component`） |
| `identifier` | `domain/model/valueobject/identifier/` | record（`Identifier` 実装） |
| `valueobject` | `domain/model/valueobject/` | record（`ValueObject` 実装） |
| `repository` | `domain/repository/` | interface（`Repository`）+ RepositoryImpl（`@Repository`） |
| `repositoryimpl` | `infrastructure/db/repository/` | class（`@Repository`） |
| `factory` | `domain/service/` | class（`@Factory`） |
| `domainservice` | `domain/service/` | class（`@Service`） |
| `command` | `application/command/command/` | record（`@Command`） |
| `commandresult` | `application/command/dto/` | record（`@CommandResult`） |
| `commandhandler` | `application/command/handler/` | class（`@Component`、メソッドに `@CommandHandler`） |
| `eventlistener` | `application/command/handler/` | class（`@Component`、メソッドに `@ApplicationModuleListener`） |
| `param` | `application/query/param/` | record（`@QueryParam`） |
| `query` | `application/query/dto/` | record（`@QueryModel`） |
| `queryservice` | `application/query/service/` | interface + QueryServiceImpl（`@Component`） |
| `queryimpl` | `infrastructure/db/query/` | class（`@Component`） |
| `controller` | `presentation/controller/` | class（`@RestController`） |
| `exceptionhandler` | `presentation/controller/` | class（`@RestControllerAdvice`、`ProblemDetail` を返却） |
| `request` | `presentation/request/` | record |
| `response` | `presentation/response/` | record |
| `api` | 複数パッケージ | Controller + Request + 2 Response + Command + CommandResult DTO + CommandHandler + Param + 2 QueryDto + QueryService + QueryServiceImpl + Exception + ExceptionHandler（14ファイル連鎖生成、aggregate 必須） |

> **ExceptionHandler 命名規則**: モジュール別 ExceptionHandler のクラス名は `<Aggregate名>ExceptionHandler`（例: `ScheduleExceptionHandler`, `PerformanceIdeaExceptionHandler`）とする。モジュール名ではなく集約名（PascalCase）を使う。scaffold が自動生成する。

---

## 2. テスト作成（TDD: Red）

スケルトン生成後、ビジネスロジック実装の**前に**テストを作成する。テストコードは `test-coding-standards.md` に従うこと。

### テスト生成スクリプト

```bash
cd backend && ./scripts/scaffold.sh test <module> <type> <target-class>
```

- `<type>` はテスト種別（下記参照）
- テストクラスのスケルトンが `src/test/java` の対応パッケージに生成される
- `--dry-run` で作成予定ファイルのプレビューが可能

### テスト種別

| type | 配置先 | 用途 |
|---|---|---|
| `domain` | `src/test/` | Aggregate/Entity/VO/Identifier の plain JUnit テスト |
| `factory` | `src/test/` | Factory の Mockito テスト |
| `handler` | `src/test/` | CommandHandler/EventListener の Mockito テスト |
| `exceptionhandler` | `src/test/` | ExceptionHandler の ProblemDetail 検証 |
| `response` | `src/test/` | Response record の `from()` 変換テスト |
| `exception` | `src/test/` | Exception のメッセージ検証 |
| `controller` | `src/test/` | `@WebMvcTest` + `@MockitoBean` + `@WithMockUser` |
| `security` | `src/test/` | `@WebMvcTest` + 認証・認可・CSRF 検証 |
| `integration` | `src/test/` | `@SpringBootTest` + `@Tag("integration")` + PostgreSQL コンテナ |
| `usecase` | `src/test/` | `@SpringBootTest` + `@Tag("integration")` + UseCase→DB結合 |
| `moduletest` | `src/test/` | `@ApplicationModuleTest` + `@Tag("integration")` + イベント検証 |
| `jooqquery` | `src/test/` | `@JooqTest` + `@Tag("integration")` + SQL クエリ検証 |
| `e2e` | `src/test/` | `@SpringBootTest(RANDOM_PORT)` + `@Tag("integration")` + 全コンテナ |

### 参照

- テストの命名・コメント・アサーション・PMD 抑制等の詳細は **`test-coding-standards.md`** を参照する

---

## 3. コード実装（TDD: Green → Refactor）

テストが Red（失敗）であることを確認した後、テストを通すためのビジネスロジックを実装する。以下を遵守すること。

### 必須ルール

- **`architecture-rules.md`** のパッケージ依存制約・型制約・Onion Architecture ルールに従う
- **`java-coding-standards.md`** の PMD 7 全ルール・Lombok 規約・jOOQ 規約・JSpecify/NullAway 規約に従う
- `package-info.java` やディレクトリ構造を手動で作成・変更しない（スクリプトが管理する）
- フォーマットは Spotless（Google Java Format）が管理するため手動調整しない

---

## 4. 検証（必須）

コード変更後は必ず以下を順番に実行し、すべて成功することを確認する。

### 4-1. フォーマット適用

```bash
make be-fmt
```

### 4-2. TDD ループ（高速フィードバック）

```bash
make be-quick
```

`quick` は compile + unit test のみ実行する（PMD/SpotBugs/integration をスキップ）。
TDD の Red-Green-Refactor サイクルではこれを繰り返し使う。

### 4-3. 静的解析のみ

```bash
make be-lint
```

Spotless check + PMD のみ実行する。コード規約違反を素早く確認したいときに使う。

### 4-4. 全チェック実行

```bash
make be-test
```

`check` には以下が含まれる:
- コンパイル（NullAway 検証含む）
- PMD 静的解析
- アーキテクチャテスト（ArchUnit / jMolecules / Spring Modulith）
- ユニットテスト（`src/test/`、タグなし）
- 統合テスト（`src/test/`、`@Tag("integration")` — PostgreSQL コンテナ自動起動）

### 4-5. 失敗時の対応

- **Spotless 違反**: `make be-fmt` を再実行
- **PMD 違反**: `java-coding-standards.md` の該当ルールを参照して修正。`@SuppressWarnings("PMD.RuleName")` は最終手段
- **NullAway 違反**: `@Nullable` の付与漏れを確認
- **アーキテクチャテスト違反**: `architecture-rules.md` を参照してパッケージ配置・依存方向を修正
- **テスト失敗**: テストコードまたは実装を修正

---

## 5. ワークフローまとめ

```
1. モジュール作成   → scaffold.sh module [--display-name]
2. 集約作成         → scaffold.sh class <module> aggregate <Name>
                      （テスト自動連鎖: domain×2 + factory + integration）
3. API 一式作成     → scaffold.sh class <module> api <Name>（aggregate 必須）
                      （テスト自動連鎖: handler + exceptionhandler + response×2
                       + exception + controller + security + integration
                       + usecase + moduletest + jooqquery + e2e）
4. Liquibase マイグレーション追加（テーブル定義）
5. jOOQ コード再生成 → make be-jooq
6. TDD ループ       → make be-quick（compile + unit test のみ、高速フィードバック）
7. ビジネスロジック実装（TODO を解消、6 と 7 を繰り返す）
8. spotlessApply    → make be-fmt
9. gradlew check    → make be-test
```

典型的な実行例:

```bash
./scripts/scaffold.sh module order --display-name "注文管理"
./scripts/scaffold.sh class order aggregate Order
./scripts/scaffold.sh class order api Order
# → main 29ファイル + test 16ファイルが自動生成される
# Liquibase マイグレーション追加
# jOOQ 生成コード更新
make be-jooq
# → TODO コメントを解消してビジネスロジックを実装
make be-quick  # TDD ループ（compile + unit test のみ）
make be-fmt
make be-test
```

すべてのステップを完了してからコミットする。

---

## 6. JaCoCo カバレッジ

- カバレッジ閾値は命令カバレッジ 85% 以上（`build.gradle` の `jacocoTestCoverageVerification`）
- `make be-test`（`check`）実行時にカバレッジ閾値を検証する
- 閾値未達の場合ビルドは失敗するが、TDD ループ（`make be-quick`）には影響しない
- 新規モジュール追加直後はカバレッジが不足するため、テスト実装を進めて 85% を達成すること
