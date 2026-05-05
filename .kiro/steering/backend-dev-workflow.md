# Backend 開発ワークフロー

本プロジェクトの backend コード変更時に従うワークフロー。
すべての作業は `backend/` ディレクトリを起点とする。

---

## 1. モジュール・クラスの作成

### 新規モジュール

```bash
cd backend && ./scripts/scaffold module <module-name>
```

- モジュール名は小文字英数字のみ（例: `order`, `shipping`）
- `package-info.java`（`@NullMarked` 付き）が自動生成される
- 作成後にアーキテクチャテストが自動実行される（`--no-test` でスキップ可）
- `--dry-run` で作成予定ファイルのプレビューが可能

### 新規クラス

```bash
cd backend && ./scripts/scaffold class <module> <layer> <name> [--aggregate <Aggregate>]
```

- 必ず `scaffold module` でモジュールを先に作成してから実行する
- ディレクトリと `package-info.java` は自動生成される（手動作成しない）
- `aggregate` 生成時は Identifier・Factory・Repository・RepositoryImpl が自動連鎖生成される
- `entity` 生成時は `--aggregate` 必須。Identifier・Factory・IdGenerator・IdGeneratorImpl が自動連鎖生成される

#### layer 一覧

| layer | 配置先 | 生成物 |
|---|---|---|
| `event` | `event/` | record（`@DomainEvent`） |
| `exception` | `exception/` | class（`RuntimeException` 継承、`@NamedInterface("exception")` で公開） |
| `aggregate` | `domain/model/aggregate/` | class（`AggregateRoot` 実装）+ Identifier + Factory + Repository + RepositoryImpl |
| `entity` | `domain/model/entity/` | class（`Entity` 実装）+ Identifier + Factory（`@Factory`） + IdGenerator + IdGeneratorImpl（`@Component`） |
| `identifier` | `domain/model/valueobject/identifier/` | record（`Identifier` 実装） |
| `valueobject` | `domain/model/valueobject/` | record（`ValueObject` 実装） |
| `repository` | `domain/repository/` | interface（`Repository`）+ RepositoryImpl（`@Repository`） |
| `repositoryimpl` | `infrastructure/db/repository/` | class（`@Repository`） |
| `factory` | `domain/service/` | class（`@Factory`） |
| `domainservice` | `domain/service/` | class（`@Service`） |
| `command` | `application/command/dto/` | record（`@Command`） |
| `commandhandler` | `application/command/handler/` | class（`@Component`、メソッドに `@CommandHandler`） |
| `eventlistener` | `application/command/handler/` | class（`@ApplicationModuleListener`） |
| `query` | `application/query/dto/` | record（`@QueryModel`） |
| `queryservice` | `application/query/service/` | interface + QueryServiceImpl（`@Component`） |
| `queryimpl` | `infrastructure/db/query/` | class（`@Component`） |
| `controller` | `presentation/controller/` | class（`@RestController`） |
| `exceptionhandler` | `presentation/controller/` | class（`@RestControllerAdvice`、`ProblemDetail` を返却） |
| `request` | `presentation/request/` | record |
| `response` | `presentation/response/` | record |

---

## 2. テスト作成（TDD: Red）

スケルトン生成後、ビジネスロジック実装の**前に**テストを作成する。テストコードは `test-coding-standards.md` に従うこと。

### テスト生成スクリプト

```bash
cd backend && ./scripts/scaffold test <module> <type> <target-class>
```

- `<type>` はテスト種別: `unit`, `integration`, `controller`
- テストクラスのスケルトンが `src/test/java` の対応パッケージに生成される

### テスト種別

| type | 用途 | 特徴 |
|---|---|---|
| `unit` | ドメインロジック・ハンドラ等の単体テスト | `@ExtendWith(MockitoExtension.class)` + `@Mock` / `@InjectMocks` |
| `integration` | リポジトリ・クエリサービスの統合テスト | `@SpringBootTest` + `@Import(TestcontainersConfiguration.class)` |
| `controller` | REST API のコントローラテスト | `@WebMvcTest` + `@MockitoBean` + `@WithMockUser` |

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
- ユニットテスト

### 4-3. 失敗時の対応

- **Spotless 違反**: `spotlessApply` を再実行
- **PMD 違反**: `java-coding-standards.md` の該当ルールを参照して修正。`@SuppressWarnings("PMD.RuleName")` は最終手段
- **NullAway 違反**: `@Nullable` の付与漏れを確認
- **アーキテクチャテスト違反**: `architecture-rules.md` を参照してパッケージ配置・依存方向を修正
- **テスト失敗**: テストコードまたは実装を修正

---

## 5. ワークフローまとめ

```
1. モジュール作成   → scaffold module
2. クラス作成       → scaffold class
3. テスト作成       → scaffold test
4. ビジネスロジック実装
5. spotlessApply    → フォーマット適用
6. gradlew check    → 全検証パス確認
```

すべてのステップを完了してからコミットする。
