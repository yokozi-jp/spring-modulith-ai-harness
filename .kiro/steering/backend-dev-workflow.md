# Backend 開発ワークフロー

本プロジェクトの backend コード変更時に従うワークフロー。
すべての作業は `backend/` ディレクトリを起点とする。

---

## 1. モジュール・クラスの作成

### 新規モジュール

```bash
cd backend && ./scripts/create-module.sh <module-name>
```

- モジュール名は小文字英数字のみ（例: `order`, `shipping`）
- `package-info.java`（`@NullMarked` 付き）が自動生成される
- 作成後にアーキテクチャテストが自動実行される（`--no-test` でスキップ可）
- `--dry-run` で作成予定ファイルのプレビューが可能

### 新規クラス

```bash
cd backend && ./scripts/create-class.sh <module> <layer> <name> [--aggregate <Aggregate>]
```

- 必ず `create-module.sh` でモジュールを先に作成してから実行する
- ディレクトリと `package-info.java` は自動生成される（手動作成しない）
- `aggregate` 生成時は Identifier・Factory・Repository が自動連鎖生成される
- `entity` 生成時は `--aggregate` 必須。Identifier・Factory・IdGenerator が自動連鎖生成される

#### layer 一覧

| layer | 配置先 | 生成物 |
|---|---|---|
| `event` | `event/` | record（`@DomainEvent`） |
| `aggregate` | `domain/model/aggregate/` | class（`AggregateRoot` 実装）+ Identifier + Factory + Repository + RepositoryImpl |
| `entity` | `domain/model/entity/` | class（`Entity` 実装）+ Identifier + Factory + IdGenerator + IdGeneratorImpl |
| `identifier` | `domain/model/valueobject/identifier/` | record（`Identifier` 実装） |
| `valueobject` | `domain/model/valueobject/` | record（`ValueObject` 実装） |
| `repository` | `domain/repository/` | interface（`Repository`）+ RepositoryImpl |
| `repositoryimpl` | `infrastructure/db/repository/` | class |
| `factory` | `domain/service/` | class |
| `domainservice` | `domain/service/` | class |
| `command` | `application/command/dto/` | record（`@Command`） |
| `commandhandler` | `application/command/handler/` | class（`@CommandHandler`） |
| `eventlistener` | `application/command/handler/` | class（`@ApplicationModuleListener`） |
| `query` | `application/query/dto/` | record（`@QueryModel`） |
| `queryservice` | `application/query/service/` | interface + QueryServiceImpl |
| `queryimpl` | `infrastructure/db/query/` | class |
| `controller` | `presentation/controller/` | class（`@RestController`） |
| `request` | `presentation/request/` | record |
| `response` | `presentation/response/` | record |

---

## 2. コード実装

生成されたスケルトンにビジネスロジックを実装する。以下を遵守すること。

### 必須ルール

- **`architecture-rules.md`** のパッケージ依存制約・型制約・Onion Architecture ルールに従う
- **`java-coding-standards.md`** の PMD 7 全ルール・Lombok 規約・jOOQ 規約・JSpecify/NullAway 規約に従う
- `package-info.java` やディレクトリ構造を手動で作成・変更しない（スクリプトが管理する）
- フォーマットは Spotless（Google Java Format）が管理するため手動調整しない

---

## 3. 検証（必須）

コード変更後は必ず以下を順番に実行し、すべて成功することを確認する。

### 3-1. フォーマット適用

```bash
cd backend && ./gradlew spotlessApply
```

### 3-2. 全チェック実行

```bash
cd backend && ./gradlew check
```

`check` には以下が含まれる:
- コンパイル（NullAway 検証含む）
- PMD 静的解析
- アーキテクチャテスト（ArchUnit / jMolecules / Spring Modulith）
- ユニットテスト

### 3-3. 失敗時の対応

- **Spotless 違反**: `spotlessApply` を再実行
- **PMD 違反**: `java-coding-standards.md` の該当ルールを参照して修正。`@SuppressWarnings("PMD.RuleName")` は最終手段
- **NullAway 違反**: `@Nullable` の付与漏れを確認
- **アーキテクチャテスト違反**: `architecture-rules.md` を参照してパッケージ配置・依存方向を修正
- **テスト失敗**: テストコードまたは実装を修正

---

## 4. ワークフローまとめ

```
1. モジュール作成   → create-module.sh
2. クラス作成       → create-class.sh
3. ビジネスロジック実装
4. spotlessApply    → フォーマット適用
5. gradlew check    → 全検証パス確認
```

すべてのステップを完了してからコミットする。
