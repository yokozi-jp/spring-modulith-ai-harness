# テストコーディング規約

本プロジェクトのテストコードに適用する規約。
PMD 7 全ルール有効の `java-coding-standards.md` を前提とし、テスト固有の補足を定める。

---

## sourceSet 構成

テストは **単一の sourceSet（`src/test/`）** に配置し、`@Tag` で種別を分離する。

| タグ | 実行方法 | 外部依存 | `check` に含む |
|---|---|---|---|
| （タグなし） | `make be-test-only T='*SampleTest'` | なし（Docker 不要） | ✅ |
| `@Tag("integration")` | `make be-test-only T='*RepositoryImplTest'` | PostgreSQL + Redis + Keycloak コンテナ | ✅ |

### TDD ワークフロー

```
1. Domain の Red-Green     → make be-test-only T='*SampleTest'            (即座)
2. Handler の Red-Green    → make be-test-only T='*CommandHandlerTest'     (即座)
3. Controller の Red-Green → make be-test-only T='*ControllerTest'         (即座)
4. DB 結合確認             → make be-test-only T='*RepositoryImplTest'     (DB起動)
5. 全体確認                → make be-test                                  (全コンテナ)
```

### テストディレクトリ構成

**本番コードのモジュール構成を `src/test/java` でそのままミラーする。**
テスト種別（unit / integration）で横断ディレクトリを切らない。

```
src/test/java/com/example/demo/
├── architecture/                    ← ArchUnit / Modulith 境界検証
├── DemoApplicationTests.java        ← @Tag("integration") コンテキストロード
├── GlobalExceptionHandlerTest.java  ← unit グローバル例外ハンドラ
├── LiquibaseMigrationTest.java      ← @Tag("integration") マイグレーション検証
├── ActuatorSecurityTest.java        ← @Tag("integration") Actuator Basic 認証
├── OAuth2AuthenticationTest.java    ← @Tag("integration") OAuth2 リダイレクト検証
├── ObservabilityTest.java           ← @Tag("integration") OTEL SDK 検証
├── sample/
│   ├── SampleModuleTest.java        ← @ApplicationModuleTest（モジュール統合）
│   ├── domain/
│   │   └── model/aggregate/SampleTest.java
│   ├── application/
│   │   └── command/handler/SampleCommandHandlerTest.java
│   ├── infrastructure/
│   │   └── db/repository/SampleRepositoryImplTest.java  ← @Tag("integration")
│   └── presentation/
│       └── controller/SampleControllerTest.java
```

ポイント:
- テストを見た瞬間に「どのモジュールの仕様か」が分かる
- `@Tag` でフィルタするので、配置場所は本番コードと同じパッケージ
- モジュール直下に `XxxModuleTest` を配置（Spring Modulith の統合テスト入口）

---

## テスト種別の全体像

### 1. Unit テスト（外部依存ゼロ、Spring コンテキストなし）

| # | テスト対象 | 何を検証するか | scaffold type |
|---|---|---|---|
| 1a | Aggregate / Entity | ドメインルール、状態遷移、不変条件、イベント登録 | `domain` |
| 1b | ValueObject / Identifier | 生成制約、等価性、バリデーション | `domain` |
| 1c | Factory | 集約の正しい初期化、イベント発行 | `factory` |
| 1d | DomainService | ドメインロジック（複数集約にまたがる操作） | `handler` |
| 1e | CommandHandler | ユースケースロジック（Factory/Repository をモック） | `handler` |
| 1f | EventListener | イベント受信時の処理（依存をモック） | `handler` |
| 1g | ExceptionHandler | ProblemDetail の構築 | `exceptionhandler` |
| 1h | Request / Response record | `from()` 変換、バリデーションアノテーション | `response` |
| 1i | Exception | メッセージ生成 | `exception` |

### 2. Slice テスト（Spring の一部だけ起動）

| # | テスト対象 | アノテーション | 外部依存 | 何を検証するか | scaffold type |
|---|---|---|---|---|---|
| 2a | Controller（MockMvc） | `@WebMvcTest` | なし（モック） | ルーティング、バリデーション、レスポンス形式、ステータスコード | `controller` |
| 2b | Controller + Security | `@WebMvcTest` + `@WithAnonymousUser` | なし | 認証・認可（302/403）、CSRF | `security` |
| 2c | jOOQ Query（DB スライス） | `@JooqTest` + `@Tag("integration")` | PostgreSQL | SQL クエリの正しさ、ページネーション | `jooqquery` |
| 2d | Liquibase マイグレーション | `@SpringBootTest` + `@Tag("integration")` | PostgreSQL | スキーマ適用・ロールバック | （プロジェクト共通） |

### 3. Integration テスト（複数レイヤー結合）

| # | テスト対象 | アノテーション | 外部依存 | 何を検証するか | scaffold type |
|---|---|---|---|---|---|
| 3a | RepositoryImpl | `@SpringBootTest` + `@Tag("integration")` | PostgreSQL | CRUD、ID生成、jOOQ マッピング | `integration` |
| 3b | QueryServiceImpl | `@SpringBootTest` + `@Tag("integration")` | PostgreSQL | 検索・ページネーション・フィルタ | `integration` |
| 3c | UseCase → Domain 結合 | `@SpringBootTest` + `@Tag("integration")` | PostgreSQL | CommandHandler → Factory → Repository → DB の一連フロー | `usecase` |
| 3d | イベント発行・購読 | `@ApplicationModuleTest` | PostgreSQL | ドメインイベントの発行→リスナー実行→副作用 | `moduletest` |
| 3e | モジュール間連携 | `@ApplicationModuleTest` + `Scenario` | PostgreSQL | モジュール A のイベント → モジュール B のリスナー | `moduletest` |

### 4. Security テスト

| # | テスト対象 | アノテーション | 外部依存 | 何を検証するか | scaffold type |
|---|---|---|---|---|---|
| 4a | OAuth2 認証フロー | `@SpringBootTest(RANDOM_PORT)` | Keycloak + Redis | ログイン→セッション→API アクセス | （プロジェクト共通） |
| 4b | Actuator Basic 認証 | `@SpringBootTest(RANDOM_PORT)` | なし（InMemory） | `/actuator/**` の認証・認可 | （プロジェクト共通） |
| 4c | CSRF 保護 | `@WebMvcTest` + `csrf()` | なし | POST/PUT/DELETE に CSRF トークン必須 | `security` |
| 4d | 未認証アクセス | `@WebMvcTest` + `@WithAnonymousUser` | なし | 302 リダイレクト | `security` |
| 4e | 権限不足 | `@WebMvcTest` + `@WithMockUser(roles="...")` | なし | 403 Forbidden | `security` |

### 5. Full-Stack テスト（全コンテナ起動）

| # | テスト対象 | アノテーション | 外部依存 | 何を検証するか | scaffold type |
|---|---|---|---|---|---|
| 5a | API E2E（RANDOM_PORT） | `@SpringBootTest(RANDOM_PORT)` + `@Tag("integration")` | PostgreSQL + Redis + Keycloak | HTTP リクエスト→レスポンスの完全フロー | `e2e` |
| 5b | コンテキストロード | `@SpringBootTest` + `@Tag("integration")` | 全部 | Bean 定義の整合性 | （プロジェクト共通、既存） |
| 5c | Observability | `@SpringBootTest(RANDOM_PORT)` + `@Tag("integration")` | OTEL Collector | トレース・メトリクス送信 | （プロジェクト共通） |

### 6. Architecture テスト（既存、自動検証）

| # | テスト対象 | 何を検証するか | 備考 |
|---|---|---|---|
| 6a | ArchUnit カスタムルール | パッケージ依存、型制約、アノテーション配置 | モジュール追加で自動検証 |
| 6b | jMolecules ルール | DDD 構造、Onion Architecture | モジュール追加で自動検証 |
| 6c | Spring Modulith | モジュール境界、循環依存 | モジュール追加で自動検証 |
| 6d | package-info 準拠 | `@NullMarked`、Onion アノテーション | scaffold が自動生成 |

> **（プロジェクト共通）** と記載されたテストはモジュール単位ではなく、プロジェクト全体で 1 回だけ作成する。
> scaffold の `aggregate` / `api` 生成時にモジュール単位のテストは自動連鎖生成される。

---

## scaffold コマンド

```bash
./scripts/scaffold.sh test <module> <type> <target-class>
```

### type 一覧と配置先

| type | 配置先 | テンプレート | 対象クラス例 |
|---|---|---|---|
| `domain` | `src/test/` | plain JUnit（assertThrows 等） | Aggregate, Entity, VO, Identifier |
| `factory` | `src/test/` | `@ExtendWith(MockitoExtension.class)` + `@Mock` / `@InjectMocks` | Factory |
| `handler` | `src/test/` | `@ExtendWith(MockitoExtension.class)` + `@Mock` / `@InjectMocks` | CommandHandler, EventListener |
| `exceptionhandler` | `src/test/` | plain JUnit（ProblemDetail 検証） | ExceptionHandler |
| `response` | `src/test/` | plain JUnit（`from()` 変換検証） | Response record |
| `exception` | `src/test/` | plain JUnit（メッセージ検証） | Exception |
| `controller` | `src/test/` | `@WebMvcTest` + `@MockitoBean` + `@WithMockUser` | Controller |
| `security` | `src/test/` | `@WebMvcTest` + `@WithAnonymousUser` / CSRF | Controller（セキュリティ観点） |
| `integration` | `src/test/` | `@SpringBootTest` + `@Tag("integration")` | RepositoryImpl, QueryServiceImpl |
| `usecase` | `src/test/` | `@SpringBootTest` + `@Tag("integration")` | CommandHandler（UseCase→DB結合） |
| `moduletest` | `src/test/` | `@ApplicationModuleTest` + `Scenario` + `@Tag("integration")` | CommandHandler（イベント発行・購読） |
| `jooqquery` | `src/test/` | `@JooqTest` + `@Tag("integration")` | QueryServiceImpl（SQL クエリ検証） |
| `e2e` | `src/test/` | `@SpringBootTest(RANDOM_PORT)` + `@Tag("integration")` | Controller（HTTP完全フロー） |

### テスト作成の網羅チェックリスト

新しいモジュールを作成したら、以下のテストを漏れなく作成する:

```bash
# 1. Domain 層
./scripts/scaffold.sh test <module> domain <Aggregate>
./scripts/scaffold.sh test <module> domain <Identifier>
./scripts/scaffold.sh test <module> factory <Factory>

# 2. Application 層
./scripts/scaffold.sh test <module> handler <CommandHandler>

# 3. Presentation 層
./scripts/scaffold.sh test <module> exceptionhandler <ExceptionHandler>
./scripts/scaffold.sh test <module> response <DetailResponse>
./scripts/scaffold.sh test <module> response <SummaryResponse>
./scripts/scaffold.sh test <module> exception <NotFoundException>
./scripts/scaffold.sh test <module> controller <Controller>
./scripts/scaffold.sh test <module> security <Controller>

# 4. Integration（DB 結合）
./scripts/scaffold.sh test <module> integration <RepositoryImpl>
./scripts/scaffold.sh test <module> integration <QueryServiceImpl>
./scripts/scaffold.sh test <module> usecase <CommandHandler>
./scripts/scaffold.sh test <module> moduletest <CommandHandler>
./scripts/scaffold.sh test <module> jooqquery <QueryServiceImpl>

# 5. E2E（全コンテナ）
./scripts/scaffold.sh test <module> e2e <Controller>
```

---

## 命名規約

### テストクラス名

- `<対象クラス名>Test`（例: `ProductTest`, `ProductFactoryTest`, `ProductControllerTest`）
- Security テストは `<対象クラス名>SecurityTest`（例: `ProductControllerSecurityTest`）

### テストメソッド名

- `should<期待動作>`（例: `shouldCreateWithPositiveValue`）
- `should<期待動作>When<条件>`（例: `shouldReturn404WhenNotFound`）
- アクセス修飾子はパッケージプライベート（`JUnit5TestShouldBePackagePrivate` 準拠）

---

## コメント規約

### テストクラス Javadoc（必須）

```java
/** Unit tests for {@link Product} aggregate. */
class ProductTest {
```

- `{@link}` で対象クラスを参照する
- 統合テストの場合は `Integration tests for {@link ProductRepositoryImpl}.` のように種別を明示する

### テストメソッド Javadoc（必須）

- 何をテストしているかを簡潔に記述する
- メソッド名が十分に説明的な場合でも省略しない

### フィールド Javadoc（必須）

- `@Mock`, `@InjectMocks`, `@MockitoBean`, コンストラクタ注入フィールドすべてに Javadoc を付与する

```java
/** Mock for product repository. */
@Mock private ProductRepository productRepository;

/** Handler under test. */
@InjectMocks private ProductCommandHandler handler;

/** テスト対象リポジトリ。 */
private final ProductRepositoryImpl repository;
```

### 定数 Javadoc（必須）

- `private static final` 定数にも Javadoc を付与する

```java
/** テスト用固定時刻。 */
private static final Instant NOW = Instant.parse("2025-01-01T00:00:00Z");
```

---

## コードスタイル

### `var` 禁止

- テストコードでも `var` を使用しない（`UseExplicitTypes` 準拠）
- 型を明示的に記述する

### ローカル変数は `final`

- すべてのローカル変数に `final` を付与する（`LocalVariableCouldBeFinal` 準拠）

```java
final Product product = createDraftProduct();
final Optional<Product> found = repository.findById(product.getId());
```

### 定数で重複リテラルを排除

- 2 回以上使用する文字列・数値リテラルは `private static final` 定数に抽出する（`AvoidDuplicateLiterals` 準拠）
- 定数化が煩雑な場合はクラスレベルで `@SuppressWarnings("PMD.AvoidDuplicateLiterals")` を許容する

```java
/** Duplicate literal for product identifier. */
private static final String PRODUCT_ID_VALUE = "product-1";
```

### static import は最大 4 つまで

- `TooManyStaticImports` のデフォルト閾値に準拠する
- 超える場合はクラス名で修飾する（例: `MockMvcResultMatchers.status()`, `ArgumentMatchers.any()`）

---

## DI 規約

### コンストラクタインジェクション優先

- テストコードでもコンストラクタインジェクションを優先する
- `@TestConstructor(autowireMode = ALL)` + `@RequiredArgsConstructor` で実現する

```java
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class ProductRepositoryImplTest {
    private final ProductRepositoryImpl sut;
}
```

### Mockito テスト（domain / factory / handler）

- `@ExtendWith(MockitoExtension.class)` + `@Mock` / `@InjectMocks` を使用する
- `@InjectMocks` はコンストラクタインジェクションを自動解決するため、フィールド宣言で問題ない

### `@MockitoBean`（controller / security テスト）

- `@MockitoBean` はフィールド宣言で使用する（コンストラクタインジェクションに置き換え不可）
- フィールドインジェクション禁止の例外として許容する

---

## アサーション規約

### 1 テスト 1 アサーション原則

- 1 つのテストメソッドでは 1 つの論理的検証を行う
- `assertEquals`, `assertTrue`, `assertThrows` 等は 1 つに留める

### 複数アサーション

- テストでは複数アサーションが自然なため、`UnitTestContainsTooManyAsserts` ルールは PMD で除外済み
- `@SuppressWarnings` は不要

```java
@Test
void shouldUpdateNameDescriptionAndPrice() {
    // 1 つの update 操作の結果を複数フィールドで検証
    ...
}
```

### アサーションメッセージ必須

- すべてのアサーションに説明メッセージを含める（`UnitTestAssertionsShouldIncludeMessage` 準拠）

```java
assertEquals(ProductStatus.DRAFT, product.getStatus(), "initial status should be DRAFT");
```

---

## テスト種別と配置

### Domain テスト（`src/test/` — type: `domain`）

Spring コンテキスト不要。TDD の主戦場。plain JUnit のみ。

| 対象 | テスト内容 |
|---|---|
| Aggregate | ドメインルール、状態遷移、不変条件、イベント登録 |
| Entity | ドメインルール、状態遷移 |
| ValueObject | 生成制約、等価性、バリデーション |
| Identifier | value 保持、等価性 |

```java
/** Unit tests for {@link Product} aggregate. */
class ProductTest {

  @Test
  void shouldHaveDraftStatusWhenCreated() {
    final Product product = new Product(new ProductId("id"), "name");
    assertEquals(Product.Status.DRAFT, product.getStatus(), "initial status should be DRAFT");
  }

  @Test
  void shouldThrowWhenNameIsBlank() {
    assertThrows(IllegalArgumentException.class, () -> new Product(new ProductId("id"), "  "));
  }
}
```

### Factory テスト（`src/test/` — type: `factory`）

Mockito で Repository / Clock をモック。

```java
@ExtendWith(MockitoExtension.class)
class ProductFactoryTest {
    @Mock private ProductRepository repository;
    @Mock private Clock clock;
    @InjectMocks private ProductFactory sut;

    @Test
    void shouldCreateWithGeneratedId() {
        when(repository.generateId()).thenReturn(new ProductId("gen-id"));
        final Product result = sut.create("name");
        assertEquals("gen-id", result.getId().value(), "id should match");
    }
}
```

### Handler テスト（`src/test/` — type: `handler`）

Mockito で Factory / Repository をモック。ユースケースロジックを検証。

```java
@ExtendWith(MockitoExtension.class)
class ProductCommandHandlerTest {
    @Mock private ProductFactory factory;
    @InjectMocks private ProductCommandHandler sut;

    @Test
    void shouldReturnCreatedId() {
        when(factory.create("name")).thenReturn(product);
        final CreatedProductDto result = sut.handle(new CreateProductCommand("name"));
        assertEquals("id-1", result.id(), "returned id should match");
    }
}
```

### ExceptionHandler テスト（`src/test/` — type: `exceptionhandler`）

Spring コンテキスト不要。ProblemDetail の構築を検証。

```java
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class ProductExceptionHandlerTest {
    private final ProductExceptionHandler sut = new ProductExceptionHandler();

    @Test
    void shouldReturn404Status() {
        final ProblemDetail result = sut.handleNotFound(new ProductNotFoundException("id-1"));
        assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatus(), "status should be 404");
    }
}
```

### Response テスト（`src/test/` — type: `response`）

`from()` static factory メソッドの変換を検証。

```java
class ProductDetailResponseTest {
    @Test
    void shouldConvertFromDto() {
        final ProductDetailDto dto = new ProductDetailDto("id-1", "name", "DRAFT");
        final ProductDetailResponse result = ProductDetailResponse.from(dto);
        assertEquals("id-1", result.id(), "id should match");
    }
}
```

### Exception テスト（`src/test/` — type: `exception`）

例外メッセージの生成を検証。

```java
class ProductNotFoundExceptionTest {
    @Test
    void shouldContainIdInMessage() {
        final ProductNotFoundException ex = new ProductNotFoundException("abc");
        assertEquals("Product not found: abc", ex.getMessage(), "message should contain id");
    }
}
```

### Controller テスト（`src/test/` — type: `controller`）

Spring MVC スライスのみ起動。Docker 不要。`WebMvcConfig` を除外してパスプレフィックスなしでテスト。

```java
@SuppressWarnings("PMD.UnitTestShouldIncludeAssert")
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@WebMvcTest(
    controllers = {ProductController.class, ProductExceptionHandler.class},
    excludeFilters =
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebMvcConfig.class))
class ProductControllerTest {
    private final MockMvc mockMvc;
    @MockitoBean private ProductCommandHandler commandHandler;
    @MockitoBean private ProductQueryService queryService;

    @Test
    @WithMockUser
    void shouldReturn201WithLocationOnCreate() throws Exception {
        when(commandHandler.handle(any())).thenReturn(new CreatedProductDto("new-id"));
        mockMvc.perform(post("/products").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"test\"}"))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"));
    }
}
```

- `@WithMockUser` でセキュリティコンテキストを設定する
- CSRF トークンが必要な変更系リクエストには `csrf()` を付与する

### Security テスト（`src/test/` — type: `security`）

認証・認可・CSRF を検証。テストクラス名は `*SecurityTest`。

```java
@SuppressWarnings("PMD.UnitTestShouldIncludeAssert")
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@WebMvcTest(
    controllers = {ProductController.class, ProductExceptionHandler.class},
    excludeFilters =
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebMvcConfig.class))
class ProductControllerSecurityTest {
    private final MockMvc mockMvc;
    @MockitoBean private ProductCommandHandler commandHandler;
    @MockitoBean private ProductQueryService queryService;

    @Test
    @WithAnonymousUser
    void shouldRedirectWhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/products"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser
    void shouldReturn403WhenNoCsrfToken() throws Exception {
        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"test\"}"))
            .andExpect(status().isForbidden());
    }
}
```

### Integration テスト（`src/test/` — type: `integration`）

PostgreSQL コンテナが必要。Domain + Infrastructure の結合を検証。

| 対象 | テスト内容 |
|---|---|
| RepositoryImpl | CRUD、ID生成、jOOQ マッピング |
| QueryServiceImpl | 検索、ページネーション、フィルタ |
| UseCase → DB 一連フロー | CommandHandler → Factory → Repo → DB |
| `@ApplicationModuleTest` | イベント発行→リスナー→副作用 |
| Liquibase マイグレーション | スキーマ適用・ロールバック |

```java
@SpringBootTest
@Tag("integration")
@Transactional
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class ProductRepositoryImplTest {
    private final ProductRepositoryImpl sut;

    @Test
    void shouldGenerateId() {
        assertNotNull(sut.generateId(), "generated id should not be null");
    }
}
```

### E2E テスト（`src/test/` — type: `e2e`）

全コンテナ起動。最終確認用。

- `@AutoConfigureTestRestTemplate` を付与して `TestRestTemplate` Bean を有効化する
- `TestRestTemplate` は `org.springframework.boot.resttestclient.TestRestTemplate` を使用する

| 対象 | テスト内容 |
|---|---|
| API E2E（RANDOM_PORT） | HTTP → 全レイヤー → DB → レスポンス |
| OAuth2 認証フロー | Keycloak → セッション → API |
| Actuator Basic 認証 | `/actuator/**` の保護 |
| コンテキストロード | Bean 定義の整合性 |

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Tag("integration")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class ProductControllerE2eTest {
    private final TestRestTemplate restTemplate;

    @Test
    void shouldReturn404ForNonExistentProduct() {
        final ResponseEntity<String> response =
            restTemplate.getForEntity("/api/v1/products/non-existent", String.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "status should be 404");
    }
}
```

---

## テスト実行環境

テストは compose-test.yaml で起動したコンテナ内で実行する（Testcontainers は使用しない）。

- 環境変数は `.env.test` で管理し、`application.yaml` のプレースホルダーを解決する
- テストクラスに `@Import` でコンテナ設定を指定する必要はない
- `make be-test` で全チェック、`make be-test-only T='*ClassName'` で特定テストを実行

### 統合テストのデータ投入

全アプリケーションテーブルへの直接 INSERT（`DSLContext` 経由）では、必須カラム（`created_at`, `updated_at`, `created_by`, `updated_by`, `version`）を必ず設定すること。`category_closures` のような関係テーブルも例外ではない。未設定だと NOT NULL 制約違反でテストが失敗する。

---

## PMD 抑制の許容範囲

テストコードで使用が許容される `@SuppressWarnings` の一覧。
必ず `"PMD.RuleName"` 形式を使用し、必要なルールのみを指定すること。

| ルール | 用途 | 適用範囲 |
|---|---|---|
| `PMD.UnitTestShouldIncludeAssert` | MockMvc テスト（`andExpect` で検証しており JUnit assert は不要） | クラスレベル |
| `PMD.TooManyMethods` | テストクラスのメソッド数が閾値を超える場合 | クラスレベル |
| `PMD.LawOfDemeter` | テスト内のメソッドチェーン（getter 呼び出し、`captor.getValue().field()` 等） | メソッドレベル |
| `PMD.AvoidDuplicateLiterals` | テストデータの文字列リテラル重複が定数化に適さない場合 | クラスレベル |
| `PMD.TestClassWithoutTestCases` | ArchUnit テストクラス（`@ArchTest` フィールドのみ） | クラスレベル |
| `PMD.UnitTestShouldIncludeAssert` | コンテキストロードのスモークテスト | クラスレベル |
| `PMD.OnlyOneReturn` | ArchUnit ルールのヘルパーメソッド（早期 return パターン） | クラスレベル |

### 抑制時の注意

- クラスレベルの抑制は必要最小限に留める
- メソッドレベルで抑制可能な場合はメソッドレベルを優先する
- 複数ルールを抑制する場合は配列形式で記述する: `@SuppressWarnings({"PMD.RuleA", "PMD.RuleB"})`

---

## テストヘルパーメソッド

- テストデータ生成用のヘルパーメソッドはテストクラス内に `private` メソッドとして定義する
- 複数テストクラスで共有する場合はテストサポートクラスに抽出する

```java
private Product createDraftProduct() {
    return new Product(
        new ProductId("test-id"),
        new ProductName("テスト商品"),
        "説明文",
        new Price(new BigDecimal("1000")),
        ProductStatus.DRAFT,
        Instant.parse("2025-01-01T00:00:00Z"),
        Instant.parse("2025-01-01T00:00:00Z"));
}
```

---

## Controller テストの WebMvcConfig 除外

`WebMvcConfig` は API バージョニング（`/api/{version}` プレフィックス）を設定する。
`@WebMvcTest` ではこれを除外し、パスプレフィックスなし（`/products` 等）でテストする。

```java
@WebMvcTest(
    controllers = {ProductController.class, ProductExceptionHandler.class},
    excludeFilters =
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebMvcConfig.class))
```

E2E テスト（`TestRestTemplate`）では `WebMvcConfig` が有効なため、`/api/v1/products` でアクセスする。

---

## NullAway

- テストコードでは NullAway 無効（`java-coding-standards.md` 参照）
- `@Nullable` アノテーションは任意だが、意図を明示したい場合は付与してよい

---

## TODO 禁止

- テストコードに `TODO` コメントを残してはいけない
- scaffold が生成するテストスケルトンには TODO が含まれるが、**ビジネスロジック実装時に必ず実装済みコードに置き換えること**
- `./gradlew check` がパスしても TODO が残っている場合はコードレビューで reject する
- 検証時に `grep -r "TODO" src/test/java` で残存 TODO がないことを確認する
