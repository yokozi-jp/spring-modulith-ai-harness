# テストコーディング規約

本プロジェクトのテストコードに適用する規約。
PMD 7 全ルール有効の `java-coding-standards.md` を前提とし、テスト固有の補足を定める。

---

## 命名規約

### テストクラス名

- `<対象クラス名>Test`（例: `ProductTest`, `ProductFactoryTest`, `ProductControllerTest`）
- テストサポートクラス（`TestcontainersConfiguration` 等）はこの限りでない

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
- 統合テストの場合は `ProductRepositoryImpl の統合テスト。` のように種別を明示する

### テストメソッド Javadoc（必須）

- 何をテストしているかを簡潔に記述する
- メソッド名が十分に説明的な場合でも省略しない

### フィールド Javadoc（必須）

- `@Mock`, `@InjectMocks`, `@MockitoBean`, `@Autowired` フィールドすべてに Javadoc を付与する

```java
/** Mock for product repository. */
@Mock private ProductRepository productRepository;

/** Handler under test. */
@InjectMocks private ProductCommandHandler handler;

/** テスト対象リポジトリ。 */
@Autowired private ProductRepositoryImpl repository;
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

### フィールドインジェクション禁止

- テストコードでもフィールドインジェクション（`@Autowired` on field）は原則禁止
- コンストラクタインジェクション（`@RequiredArgsConstructor` またはテストコンストラクタ）を使用する

### 例外: `@WebMvcTest` / `@SpringBootTest`

- 現状 `@Autowired` フィールドインジェクションが使用されているテストが存在する
- 新規テストではコンストラクタインジェクションを優先する
- `MockMvc` 等の Spring テストインフラも可能な限りコンストラクタで注入する

### Mockito テスト

- `@ExtendWith(MockitoExtension.class)` + `@Mock` / `@InjectMocks` を使用する
- `@InjectMocks` はコンストラクタインジェクションを自動解決するため、フィールド宣言で問題ない
- `@MockitoBean` はフィールド宣言で使用する（コンストラクタインジェクションに置き換え不可）。フィールドインジェクション禁止の例外として許容する

---

## アサーション規約

### 1 テスト 1 アサーション原則

- 1 つのテストメソッドでは 1 つの論理的検証を行う
- `assertEquals`, `assertTrue`, `assertThrows` 等は 1 つに留める

### 複数アサーションが必要な場合

- 論理的に 1 つの検証を複数の assert で表現する場合に限り許容する
- `@SuppressWarnings("PMD.UnitTestContainsTooManyAsserts")` を付与する
- 理由がメソッド名や Javadoc から明らかでない場合はコメントで補足する

```java
@Test
@SuppressWarnings({"PMD.UnitTestContainsTooManyAsserts", "PMD.LawOfDemeter"})
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

## テスト種別

### ユニットテスト

- Spring コンテキスト不要
- `@ExtendWith(MockitoExtension.class)` + `@Mock` / `@InjectMocks`
- 配置: 対象クラスと同じパッケージ構造の `src/test/java` 配下

```java
@ExtendWith(MockitoExtension.class)
class ProductCommandHandlerTest {
    @Mock private ProductFactory productFactory;
    @InjectMocks private ProductCommandHandler handler;
}
```

### 統合テスト

- `@SpringBootTest` + `@Import(TestcontainersConfiguration.class)`
- 実際の DB・Redis・Keycloak 等を Testcontainers で起動する
- リポジトリ・クエリサービスの実装を検証する

```java
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class ProductRepositoryImplTest {
    @Autowired private ProductRepositoryImpl repository;
}
```

### コントローラテスト

- `@WebMvcTest(controllers = {XxxController.class, XxxExceptionHandler.class})`
- `@MockitoBean` で依存サービスをモック化する
- `@WithMockUser` でセキュリティコンテキストを設定する
- CSRF トークンが必要な変更系リクエストには `SecurityMockMvcRequestPostProcessors.csrf()` を付与する

```java
@SuppressWarnings({"PMD.UnitTestShouldIncludeAssert", "PMD.AvoidDuplicateLiterals"})
@WebMvcTest(controllers = {ProductController.class, ProductExceptionHandler.class})
class ProductControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private ProductCommandHandler commandHandler;
}
```

---

## PMD 抑制の許容範囲

テストコードで使用が許容される `@SuppressWarnings` の一覧。
必ず `"PMD.RuleName"` 形式を使用し、必要なルールのみを指定すること。

| ルール | 用途 | 適用範囲 |
|---|---|---|
| `PMD.UnitTestShouldIncludeAssert` | MockMvc テスト（`andExpect` で検証しており JUnit assert は不要） | クラスレベル |
| `PMD.UnitTestContainsTooManyAsserts` | 論理的に 1 つの検証を複数 assert で表現する場合 | メソッドレベル |
| `PMD.TooManyMethods` | テストクラスのメソッド数が閾値を超える場合 | クラスレベル |
| `PMD.LawOfDemeter` | テスト内のメソッドチェーン（getter 呼び出し、`captor.getValue().field()` 等） | メソッドレベル |
| `PMD.AvoidDuplicateLiterals` | テストデータの文字列リテラル重複が定数化に適さない場合 | クラスレベル |
| `PMD.TestClassWithoutTestCases` | テストサポートクラス（`TestcontainersConfiguration` 等） | クラスレベル |
| `PMD.UnitTestShouldIncludeAssert` | コンテキストロードのスモークテスト | クラスレベル |

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

## NullAway

- テストコードでは NullAway 無効（`java-coding-standards.md` 参照）
- `@Nullable` アノテーションは任意だが、意図を明示したい場合は付与してよい
