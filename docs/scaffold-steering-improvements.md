# Scaffold / ツール改善 + Steering 改善の洗い出し

## A. Scaffold テストテンプレートの問題（TODO が残る根本原因）

scaffold が生成するテストは **全て TODO スタブ** であり、コンパイルは通るが実質テストしていない。AI がドメインロジックを実装した後でも、テストは手動で書き直す必要がある。

| テスト種別 | 問題 | 改善案 |
|---|---|---|
| **domain** | `assertNotNull(null, "TODO")` — 必ず失敗するはずが PMD 通過 | テストメソッドを空にして `// AI: ここにテストを実装` コメントのみにするか、**テストテンプレートを廃止して steering で AI に生成させる** |
| **factory** | `@InjectMocks` だけ宣言し `@Mock` が TODO コメント | `@Mock` フィールドを `XxxRepository` と `Clock` で生成する（scaffold 時点で集約名から推論可能） |
| **handler** | `@Mock` が TODO コメント | Factory と Repository の `@Mock` を生成する |
| **exceptionhandler** | `sut.handleXxx(...)` が TODO | `handleNotFound(new XxxNotFoundException("id"))` を生成する（例外クラス名は scaffold 時点で既知） |
| **response** | `assertEquals("expected", "expected", "TODO")` — 常に成功する無意味テスト | DTO 生成 → `from()` 呼び出し → フィールド検証のテンプレートを生成 |
| **exception** | `assertEquals("TODO: expected message", ...)` — 必ず失敗 | `"${NAME} not found: test-id"` を生成する（メッセージ形式は scaffold が生成する例外クラスと一致） |
| **controller** | `shouldDoSomething()` — 空テスト | 最低限 `GET /path → 200` のテストを生成する |
| **security** | `shouldRedirectWhenUnauthenticated()` — 空テスト | `GET` + `@WithAnonymousUser` → 302 のテストを生成する |
| **integration/usecase/moduletest/jooqquery** | `assertNotNull(sut)` のみ | DI 確認だけなら `@SuppressWarnings("PMD.UnitTestShouldIncludeAssert")` を付けるか、テスト自体を生成しない |

### 根本的な改善方針

1. **Option A: scaffold テストを「コンパイル通る最小限 + AI 向け指示コメント」に変更**
   - TODO を排除し、`// AI: implement test for ...` 形式のコメントのみ残す
   - テストメソッドは空 or `assertNotNull(sut)` で PMD を通す
   - steering に「scaffold 生成後、AI がテストを実装する」フローを明記

2. **Option B: scaffold テストを「実装済み」にする（推奨）**
   - scaffold 時点で既知の情報（クラス名、メソッド名、例外メッセージ）から実装可能なテストは生成する
   - 例: `exceptionhandler` テストは例外クラス名とメッセージ形式が確定しているので完全に生成可能
   - 例: `exception` テストもメッセージ形式が確定しているので完全に生成可能
   - ドメインロジック依存のテスト（domain, factory, handler, controller）は **生成しない**（steering で AI に委ねる）

3. **Option C: テストスケルトン生成を廃止し、steering で AI に全テスト生成を指示**
   - scaffold は main ソースのみ生成
   - steering の `backend-dev-workflow.md` に「テストは AI が実装する」と明記
   - テスト生成コマンド `scaffold.sh test` は残すが、`api` / `aggregate` の連鎖生成からは外す

---

## B. Scaffold main ソーステンプレートの問題

| ファイル | 問題 | 改善案 |
|---|---|---|
| **CreateXxxCommand** | `// TODO: フィールドを定義する` — 空 record | フィールドなしで生成し、TODO コメントを削除。AI が実装時に追加する |
| **CreateXxxRequest** | `// TODO: バリデーションメッセージを設定` — コメントアウトされたフィールド | 空 record で生成し、TODO を削除 |
| **XxxListParam** | `// TODO: フィルタ条件を定義する` — 空 record | 空 record で生成し、TODO を削除 |
| **XxxSummaryDto / XxxDetailDto** | `String id` のみ | そのまま（AI が拡張する前提） |
| **XxxSummaryResponse / XxxDetailResponse** | `String id` のみ + `from()` | そのまま（AI が拡張する前提） |
| **XxxQueryServiceImpl** | `// TODO: jOOQ でクエリを実装する` + 空実装 | TODO コメントを削除。空実装（`return Optional.empty()`）は残す（コンパイル通す） |
| **XxxCommandHandler** | `factory.create()` — 引数なし | そのまま（Factory の引数は AI が実装時に追加） |
| **XxxController** | `@Tag(description = "TODO: ...")`, `@Operation(summary = "TODO: ...")` | TODO を削除し、`description = "${NAME} API"`, `summary = "${NAME} を作成する"` を生成 |
| **XxxController** | `@SuppressWarnings("PMD.LawOfDemeter")` on handler | **削除**（PMD 7.23 で UnnecessaryWarningSuppression） |
| **XxxController** | `@SuppressWarnings("PMD.AvoidDuplicateLiterals")` | 残す（`"/{id}"` の重複は正当） |

---

## C. Scaffold 命名・パス生成のバグ

| 問題 | 原因 | 修正 |
|---|---|---|
| **`PerformanceideaExceptionHandler`** (小文字 i) | `module_cap` が `${MODULE:0:1}` を大文字化するだけで、camelCase 変換しない | ExceptionHandler のクラス名を `${NAME}ExceptionHandler` に変更する（モジュール名ではなく集約名を使う） |
| **`DecisionitemExceptionHandler`** (小文字 i) | 同上 | 同上 |
| **URL パス `/performanceideas`** | `local path="/${MODULE}s"` — モジュール名をそのまま使う | ケバブケース変換を追加: `performanceidea` → `/performance-ideas`, `decisionitem` → `/decision-items` |

---

## D. Scaffold `@ApplicationModuleTest` テンプレートの問題

| 問題 | 修正 |
|---|---|
| `ClockConfig` が見つからず起動失敗 | テンプレートに `@ApplicationModuleTest(extraIncludes = "config")` を追加 |
| `Scenario` パラメータを受け取るが使わない | テストメソッドから `Scenario` パラメータを削除するか、使用例を生成する |

---

## E. Steering 改善

| ファイル | 問題 | 改善案 |
|---|---|---|
| **backend-dev-workflow.md** | 「テスト作成（TDD: Red）」で scaffold test を実行するが、生成されるのは TODO スタブ | **「scaffold が生成するテストスケルトンは最小限のコンパイル通過コードであり、AI がビジネスロジックに合わせて実装する」** と明記する |
| **backend-dev-workflow.md** | scaffold の `api` 連鎖生成で 12 種のテストが自動生成されるが、大半が TODO | **テスト連鎖生成の対象を絞る**（exception, exceptionhandler のみ自動生成 → 他は AI が必要に応じて生成）、または **全テスト自動生成を維持しつつ「AI は TODO を解消すること」を明記** |
| **backend-dev-workflow.md** | `check` が通れば OK という記述 | **「TODO コメントが残っていないことを確認する」** チェック項目を追加 |
| **test-coding-standards.md** | scaffold 生成テストの TODO パターンについて言及なし | **「scaffold が生成する TODO スタブは必ず実装に置き換えること。TODO を含むテストはコードレビューで reject する」** を追加 |
| **java-coding-standards.md** | `@SuppressWarnings("PMD.LawOfDemeter")` が使用済みリストにない | PMD 7.23 で不要になったため **使用禁止** と明記する |
| **architecture-rules.md** | Dashboard モジュールの DB 直接アクセスパターンについて言及なし | **「Query-only モジュールは他モジュールの DB テーブルを直接クエリしてよい（QueryService 経由は不要）」** を明記するか、逆に禁止する |
| **rest-api-standards.md** | URL パスのケバブケース変換ルールが明記されていない | **「モジュール名が複合語の場合、URL パスはケバブケースに変換する（例: performanceidea → /performance-ideas）」** を追加 |
| **backend-dev-workflow.md** | ExceptionHandler の命名規則が不明確 | **「ExceptionHandler のクラス名は `<Aggregate>ExceptionHandler`（例: `ScheduleExceptionHandler`）とする。モジュール名ではなく集約名を使う」** を追加 |

---

## F. 優先度付き改善タスク一覧

### P1（TODO 根絶に直結）

1. scaffold `_class_generators.sh`: main ソースの TODO コメントを全削除（空実装は残す）
2. scaffold `_class_generators.sh`: Controller の `@Tag` / `@Operation` の TODO を日本語説明に置換
3. scaffold `scaffold.sh`: `exception` テストテンプレートを実装済みに変更（メッセージ形式が確定）
4. scaffold `scaffold.sh`: `exceptionhandler` テストテンプレートを実装済みに変更（例外クラス名が確定）
5. scaffold `scaffold.sh`: `response` テストテンプレートの無意味アサーションを削除
6. steering `backend-dev-workflow.md`: 「scaffold 生成後、AI は全 TODO を解消してからコミットする」を明記
7. steering `backend-dev-workflow.md`: 検証ステップに `grep -r "TODO" src/ && exit 1` チェックを追加

### P2（バグ修正）

8. scaffold `_class_generators.sh`: ExceptionHandler 命名を `${NAME}ExceptionHandler` に変更
9. scaffold `_class_generators.sh`: URL パス生成にケバブケース変換を追加
10. scaffold `scaffold.sh`: `moduletest` テンプレートに `@ApplicationModuleTest(extraIncludes = "config")` を追加
11. scaffold `_class_generators.sh`: CommandHandler の `@SuppressWarnings("PMD.LawOfDemeter")` を削除

### P3（テスト品質向上）

12. scaffold `scaffold.sh`: `domain` / `factory` / `handler` / `controller` / `security` テストテンプレートから TODO を削除し、最小限の構造のみ残す（AI 実装前提）
13. scaffold `scaffold.sh`: `integration` / `usecase` / `moduletest` / `jooqquery` テストに `@SuppressWarnings("PMD.UnitTestShouldIncludeAssert")` を追加（DI 確認のみ）
14. scaffold `scaffold.sh`: `exceptionhandler` テストの `@SuppressWarnings("PMD.AvoidDuplicateLiterals")` を削除（不要）

### P4（Steering 整備）

15. steering `test-coding-standards.md`: 「TODO を含むテストは禁止」ルールを追加
16. steering `java-coding-standards.md`: `PMD.LawOfDemeter` 抑制を使用禁止に変更
17. steering `rest-api-standards.md`: ケバブケース URL ルールを追加
18. steering `architecture-rules.md`: Dashboard の DB 直接アクセスパターンを明記
19. steering `backend-dev-workflow.md`: ExceptionHandler 命名規則を追加
