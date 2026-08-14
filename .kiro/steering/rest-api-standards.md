# REST API 規約

本プロジェクトの REST API 設計・実装に適用する規約。
コードファースト + SpringDoc OpenAPI で API ドキュメントを自動生成する。

---

## 基本方針

- コードファースト: Controller のアノテーションから OpenAPI spec を自動生成する
- CQRS 分離: POST（Command）と GET（Query）で異なるデータフローを使う
- SpringDoc OpenAPI（`springdoc-openapi-starter-webmvc-ui`）でドキュメント自動生成

---

## HTTP メソッドとステータスコード

| 操作 | メソッド | 成功時ステータス | レスポンスボディ |
|------|----------|-----------------|-----------------|
| 作成 | `POST` | `201 Created` | なし（`Location` ヘッダーのみ） |
| 単体取得 | `GET /{id}` | `200 OK` | `XxxDetailResponse` |
| 一覧取得 | `GET` | `200 OK` | `Page<XxxSummaryResponse>` |
| 更新 | `PUT /{id}` or `PATCH /{id}/{action}` | `200 OK` | 操作結果 DTO |
| 削除 | `DELETE /{id}` | `204 No Content` | なし |

### POST の規約

- `201 Created` + `Location` ヘッダーのみ返す
- レスポンスボディで作成結果を read-back しない（CQRS の Command/Query 分離を維持）
- `Location` ヘッダーには作成されたリソースの URI を設定する

```java
final URI location = ServletUriComponentsBuilder.fromCurrentRequest()
    .path("/{id}").buildAndExpand(id).toUri();
return ResponseEntity.created(location).build();
```

---

## レイヤー間の値の受け渡し

```
Client
  ↕ XxxRequest / XxxResponse (presentation 層)
Controller (presentation)
  ↕ XxxCommand + XxxDto / XxxParam + XxxDto (application 層)
CommandHandler / QueryService (application)
  ↕ Domain Objects (domain 層)
Factory / Repository / Aggregate (domain)
```

### 変換ルール

| 方向 | 変換方法 |
|------|----------|
| Request → Command | Controller 内で `new XxxCommand(request.field(), ...)` |
| CommandHandler → Controller | `@CommandResult` 付き DTO を返す |
| QueryService → Controller | `@QueryModel` 付き DTO を返す |
| DTO → Response | Response record の `static from(XxxDto dto)` メソッド |

### 依存方向の制約

- `presentation → application`: OK（Command, Param, DTO を参照可能）
- `presentation → domain`: **禁止**（Identifier 等のドメイン型を直接参照しない）
- `application → domain`: OK
- `infrastructure → application, domain`: OK

---

## Controller の構成

### クラスレベル

```java
@Tag(name = "Order", description = "注文管理 API")
@RestController
@RequestMapping("/orders")
@Slf4j
@RequiredArgsConstructor
public class OrderController {

  /** コマンドハンドラ。 */
  private final OrderCommandHandler commandHandler;

  /** クエリサービス。 */
  private final OrderQueryService queryService;
}
```

### メソッドレベル

```java
@Operation(summary = "注文を作成する")
@ApiResponse(responseCode = "201", description = "作成成功")
@PostMapping
public ResponseEntity<Void> create(@RequestBody @Valid final CreateOrderRequest request) {
  ...
}
```

---

## ページネーション

- Spring の `Pageable` を使用する
- 一覧取得は `Page<XxxSummaryResponse>` を返す
- クエリパラメータ: `?page=0&size=20&sort=createdAt,desc`
- QueryService のシグネチャ: `Page<XxxSummaryDto> findAll(XxxListParam param, Pageable pageable)`
- Controller で `Page.map()` を使い DTO → Response に変換する

```java
@GetMapping
public Page<OrderSummaryResponse> list(final OrderListParam param, final Pageable pageable) {
  return queryService.findAll(param, pageable).map(OrderSummaryResponse::from);
}
```

---

## 単体取得と Not Found

- QueryService は `Optional<XxxDetailDto>` を返す
- Controller で `orElseThrow` して例外を投げる
- 例外は `exception/` パッケージの `XxxNotFoundException` を使用する

```java
@GetMapping("/{id}")
public OrderDetailResponse findById(@PathVariable final String id) {
  return queryService.findById(id)
      .map(OrderDetailResponse::from)
      .orElseThrow(() -> new OrderNotFoundException(id));
}
```

---

## Response record の設計

### 一覧用と詳細用を分離する

- `XxxSummaryResponse` — 一覧用（ID + 表示に必要な最小限のフィールド）
- `XxxDetailResponse` — 単体取得用（全フィールド）

### `from()` static factory メソッド

```java
public record OrderDetailResponse(String id, String customerName, String status) {

  /** DTO から変換する。 */
  public static OrderDetailResponse from(final OrderDetailDto dto) {
    return new OrderDetailResponse(dto.id(), dto.customerName(), dto.status());
  }
}
```

---

## Request record の設計

- Bean Validation アノテーションでバリデーションを行う
- Controller のメソッド引数に `@Valid` を付与する

```java
public record CreateOrderRequest(
    @NotBlank(message = "顧客名は必須です")
    String customerName,

    @Positive(message = "金額は正の値である必要があります")
    BigDecimal amount
) {}
```

---

## エラーレスポンス

### RFC 9457 ProblemDetail

- Spring Boot 4 ではデフォルト有効
- カスタマイズが必要な場合のみ `@ExceptionHandler` で `ProblemDetail` を返す

### 例外ハンドラの 2 層構成

| 層 | クラス | スコープ |
|----|--------|---------|
| グローバル | `com.example.demo.GlobalExceptionHandler` | アプリ全体 |
| モジュール別 | `<module>/presentation/controller/<Name>ExceptionHandler` | `@RestControllerAdvice(basePackages = "...")` |

### グローバルハンドラの責務

- `IllegalStateException` → `409 Conflict`
- `IllegalArgumentException` → `400 Bad Request`
- `MethodArgumentNotValidException` → `400 Bad Request`（Bean Validation 違反）

### モジュール別ハンドラの責務

- `XxxNotFoundException` → `404 Not Found`
- その他モジュール固有の業務例外

---

## SpringDoc アノテーション規約

| アノテーション | 配置 | 必須/任意 |
|---------------|------|----------|
| `@Tag` | クラスレベル | 必須 |
| `@Operation(summary = "...", operationId = "...")` | メソッドレベル | 必須 |
| `@ApiResponse` | メソッドレベル | 必須（成功 + 主要エラー） |
| `@Parameter` | パラメータレベル | 任意 |
| `@Schema` | DTO フィールドレベル | 任意 |

### operationId の命名規則（必須）

`operationId` は Controller の Java メソッド名からではなく、必ず `@Operation` で明示的に指定する。

**理由**: SpringDoc はデフォルトで Java メソッド名（`findById`, `create`, `update`, `delete` 等）をそのまま `operationId` にする。CRUD メソッド名は全モジュールの Controller で共通化しているため、複数モジュールが存在すると `operationId` が重複し、SpringDoc が自動的に `findById_1`, `findById_2` のような連番を付与する。Orval はこの `operationId` をそのまま Hook 名に変換するため、フロントエンド側で `useFindById`, `useFindById1`, `useFindById2` のような意味の分からない Hook 名が生成されてしまう。`@Operation(operationId = ...)` で明示することでこの重複・連番付与を根本的に回避する。

**命名パターン**: `<動詞><Resource名>`（PascalCase の Resource 名、先頭のみ camelCase）

| 操作 | パターン | 例（Product） |
|------|---------|---------------|
| 作成 | `create<Resource>` | `createProduct` |
| 一覧取得 | `list<Resource>`（複数形にしない） | `listProduct` |
| 単体取得 | `find<Resource>ById` | `findProductById` |
| 更新 | `update<Resource>` | `updateProduct` |
| 削除 | `delete<Resource>` | `deleteProduct` |
| ドメインアクション | `<動詞><Resource>`（PATCH エンドポイント） | `publishProduct`, `moveCategory` |
| ネストリソース取得 | `find<ParentResource><ChildResource>`（複数形にしない） | `findCategoryChildren` |

```java
@Operation(summary = "商品を作成する", operationId = "createProduct")
@ApiResponse(responseCode = "201", description = "作成成功")
@PostMapping
public ResponseEntity<Void> create(...) { ... }

@Operation(summary = "商品詳細を取得する", operationId = "findProductById")
@ApiResponse(responseCode = "200", description = "取得成功")
@GetMapping("/{id}")
public ProductDetailResponse findById(@PathVariable final String id) { ... }
```

Controller の Java メソッド名自体（`findById`, `update` 等）は変更しない。`operationId` のみ Resource 名を含めて一意化する。

### scaffold 生成時の TODO

scaffold の `api` layer で生成されるコードには、機械的に決められない箇所に `TODO:` コメントが付与される。開発者は実装時にこれらを解消すること。`operationId` は Resource 名（`{{NAME}}`）から機械的に決定できるため scaffold が自動生成し、TODO は付与されない。

```java
@Tag(name = "Order", description = "TODO: Order API の説明を記述する")
@Operation(summary = "TODO: Order 作成の説明を記述する", operationId = "createOrder")
```

---

## URL 設計

- ベースパス: `/api/v1/` — `WebMvcConfig` が全 `@RestController` に自動付与
- リソース名は複数形・ケバブケース: `/orders`, `/products`, `/performance-ideas`, `/decision-items`
- 複合語のリソース名はケバブケースに変換する（例: `PerformanceIdea` → `/performance-ideas`）
- パス変数は ID: `/orders/{id}`
- ドメインアクション型の更新: `PATCH /orders/{id}/confirm`
- ネストリソース: `/orders/{orderId}/items`

### API バージョニング

パスプレフィックス `/api/v1` を固定で付与する（`WebMvcConfig`）。

- 設定: `com.example.demo.config.WebMvcConfig`
- パス構造: `/api/v1/orders`, `/api/v1/products`
- Controller の `@RequestMapping` にはバージョンを含めない（自動付与される）

```java
// Controller — "/orders" と書くだけで /api/v1/orders になる
@RestController
@RequestMapping("/orders")
public class OrderController { ... }
```

**SpringDoc との互換性のため `ApiVersionConfigurer` は使用しない。**
`addPathPrefix("/api/{version}", ...)` を使うと SpringDoc が `{version}` をリテラルとして OpenAPI spec に出力し、Swagger UI が正しく動作しない。

v2 が必要になった場合はヘッダー方式（`X-API-Version`）への移行を検討する:

```java
// 将来の v2 対応例（ヘッダー方式）
configurer.useRequestHeader("X-API-Version").setDefaultVersion("1");
```

---

## Clock の使用

- `Instant.now()` をドメイン層やファクトリで直接呼ばない
- `ClockConfig`（`com.example.demo.ClockConfig`）が `Clock.systemUTC()` を Bean として提供する
- `java.time.Clock` をコンストラクタインジェクションで受け取り `clock.instant()` で現在時刻を取得する
- テストでは `Clock.fixed(...)` で時刻を固定する
