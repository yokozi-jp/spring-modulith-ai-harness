# Backend 開発ワークフロー

本プロジェクトの backend コード変更時に従うワークフロー。
すべての作業は `backend/` ディレクトリを起点とする。

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
| `e2e` | `src/test/` | `@SpringBootTest(RANDOM_PORT)` + `@Tag("e2e")` + 全コンテナ |

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
cd backend && ./gradlew spotlessApply
```

### 4-2. 全チェック実行

```bash
cd backend && ./gradlew check
```

`check` には以下が含まれる:
- コンパイル（NullAway 検証含む）
- PMD 静的解析
- アーキテクチャテスト（ArchUnit / jMolecules / Spring Modulith）
- ユニットテスト（`src/test/`、タグなし）
- 統合テスト（`src/test/`、`@Tag("integration")` — PostgreSQL コンテナ自動起動）

### 4-3. 失敗時の対応

- **Spotless 違反**: `spotlessApply` を再実行
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
4. ビジネスロジック実装（TODO を解消）
5. spotlessApply    → フォーマット適用
6. gradlew check    → 全検証パス確認
```

典型的な実行例:

```bash
./scripts/scaffold.sh module order --display-name "注文管理"
./scripts/scaffold.sh class order aggregate Order
./scripts/scaffold.sh class order api Order
# → main 29ファイル + test 16ファイルが自動生成される
# → TODO コメントを解消してビジネスロジックを実装
./gradlew spotlessApply
./gradlew check
```

すべてのステップを完了してからコミットする。
