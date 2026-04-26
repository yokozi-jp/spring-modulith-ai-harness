# create-class.sh 設計書

## 概要

モジュール内の各ディレクトリにファイルを作成するスクリプト。
配置先に応じて命名規則・種別（class / record / interface）・アノテーション・import を自動で揃える。

---

## コマンド仕様

```bash
./scripts/create-class.sh <module> <layer> <name> [--aggregate <AggregateRootName>]
```

| 引数 | 必須 | 説明 |
|---|---|---|
| `<module>` | ○ | モジュール名（例: `order`） |
| `<layer>` | ○ | 配置先レイヤー（後述の layer 一覧から選択） |
| `<name>` | ○ | 概念名（PascalCase、例: `Order`, `OrderItem`） |
| `--aggregate` | △ | 所属する集約ルート名。`entity` で必須、`repository` / `queryservice` では省略時に `<name>` を使用 |

---

## layer 一覧と生成ルール

### event

| 項目 | 値 |
|---|---|
| ディレクトリ | `event/` |
| 種別 | `record` |
| クラス名 | `{Name}Event` |
| アノテーション | `@DomainEvent` |
| import | `org.jmolecules.event.annotation.DomainEvent` |

```java
@DomainEvent
public record OrderCreatedEvent() {}
```

### aggregate

| 項目 | 値 |
|---|---|
| ディレクトリ | `domain/model/aggregate/` |
| 種別 | `class`（不変） |
| クラス名 | `{Name}` |
| 実装 | `implements AggregateRoot<{Name}, {Name}Id>` |
| アノテーション | `@Slf4j`, `@Getter`, `@EqualsAndHashCode`, `@ToString` |
| import | `org.jmolecules.ddd.types.AggregateRoot`, Lombok |
| 自動連鎖 | `{Name}Id` を `identifier` に自動生成 |
| 不変性 | 全フィールド `private final`。状態変更は新しいインスタンスを生成する |
| コンストラクタ | 手書き。引数の不正ガード（null チェック等）を記述する |

```java
@Slf4j
@Getter
@EqualsAndHashCode
@ToString
public class Order implements AggregateRoot<Order, OrderId> {

  private final OrderId id;

  /** コンストラクタ。 */
  public Order(final OrderId id) {
    Objects.requireNonNull(id, "id must not be null");
    this.id = id;
  }

  @Override
  public OrderId getId() {
    return id;
  }
}
```

### entity

| 項目 | 値 |
|---|---|
| ディレクトリ | `domain/model/entity/` |
| 種別 | `class`（不変） |
| クラス名 | `{Name}` |
| 実装 | `implements Entity<{Aggregate}, {Name}Id>` |
| アノテーション | `@Slf4j`, `@Getter`, `@EqualsAndHashCode`, `@ToString` |
| import | `org.jmolecules.ddd.types.Entity`, Lombok |
| 必須オプション | `--aggregate {AggregateRootName}` |
| 自動連鎖 | `{Name}Id` を `identifier` に自動生成 |
| 不変性 | 全フィールド `private final`。状態変更は新しいインスタンスを生成する |
| コンストラクタ | 手書き。引数の不正ガード（null チェック等）を記述する |

```java
@Slf4j
@Getter
@EqualsAndHashCode
@ToString
public class OrderItem implements Entity<Order, OrderItemId> {

  private final OrderItemId id;

  /** コンストラクタ。 */
  public OrderItem(final OrderItemId id) {
    Objects.requireNonNull(id, "id must not be null");
    this.id = id;
  }

  @Override
  public OrderItemId getId() {
    return id;
  }
}
```

### identifier

| 項目 | 値 |
|---|---|
| ディレクトリ | `domain/model/valueobject/identifier/` |
| 種別 | `record` |
| クラス名 | `{Name}Id` |
| 実装 | `implements Identifier` |
| import | `org.jmolecules.ddd.types.Identifier` |

```java
public record OrderId(String value) implements Identifier {}
```

### valueobject

| 項目 | 値 |
|---|---|
| ディレクトリ | `domain/model/valueobject/` |
| 種別 | `record` |
| クラス名 | `{Name}` |
| 実装 | `implements ValueObject` |
| import | `org.jmolecules.ddd.types.ValueObject` |

```java
public record Money(long amount, String currency) implements ValueObject {}
```

### repository

| 項目 | 値 |
|---|---|
| ディレクトリ | `domain/repository/` |
| 種別 | `interface` |
| クラス名 | `{Name}Repository` |
| 実装 | `extends Repository<{Name}, {Name}Id>` |
| import | `org.jmolecules.ddd.types.Repository` |
| 自動連鎖 | `{Name}RepositoryImpl` を `infrastructure/db/repository/` に自動生成 |

```java
public interface OrderRepository extends Repository<Order, OrderId> {}
```

### domainservice

| 項目 | 値 |
|---|---|
| ディレクトリ | `domain/service/` |
| 種別 | `class` |
| クラス名 | `{Name}DomainService` |
| アノテーション | `@Slf4j`, `@RequiredArgsConstructor` |
| import | Lombok |

```java
@Slf4j
@RequiredArgsConstructor
public class OrderDomainService {

  // DI フィールドをここに追加
}
```

### command

| 項目 | 値 |
|---|---|
| ディレクトリ | `application/command/dto/` |
| 種別 | `record` |
| クラス名 | `{Name}Command` |
| アノテーション | `@Command` |
| import | `org.jmolecules.architecture.cqrs.Command` |

```java
@Command
public record CreateOrderCommand() {}
```

### commandhandler

| 項目 | 値 |
|---|---|
| ディレクトリ | `application/command/handler/` |
| 種別 | `class` |
| クラス名 | `{Name}CommandHandler` |
| アノテーション | `@CommandHandler`, `@Slf4j`, `@RequiredArgsConstructor` |
| import | `org.jmolecules.architecture.cqrs.CommandHandler`, Lombok |

```java
@CommandHandler
@Slf4j
@RequiredArgsConstructor
public class CreateOrderCommandHandler {

  // DI フィールドをここに追加
}
```

### eventlistener

| 項目 | 値 |
|---|---|
| ディレクトリ | `application/command/handler/` |
| 種別 | `class` |
| クラス名 | `{Name}EventListener` |
| アノテーション | `@Slf4j`, `@RequiredArgsConstructor` |
| import | `org.springframework.modulith.events.ApplicationModuleListener`, Lombok |
| 備考 | メソッドに `@ApplicationModuleListener` を付与（トランザクション完了後に非同期実行） |

```java
@Slf4j
@RequiredArgsConstructor
public class OrderCreatedEventListener {

  /** イベントハンドラ。 */
  @ApplicationModuleListener
  void on(final OrderCreatedEvent event) {
    // イベント処理
  }
}
```

### query

| 項目 | 値 |
|---|---|
| ディレクトリ | `application/query/dto/` |
| 種別 | `record` |
| クラス名 | `{Name}Query` |
| アノテーション | `@QueryModel` |
| import | `org.jmolecules.architecture.cqrs.QueryModel` |

```java
@QueryModel
public record OrderDetailQuery() {}
```

### queryservice

| 項目 | 値 |
|---|---|
| ディレクトリ | `application/query/service/` |
| 種別 | `interface` |
| クラス名 | `{Name}QueryService` |
| 自動連鎖 | `{Name}QueryServiceImpl` を `infrastructure/db/query/` に自動生成 |

```java
public interface OrderQueryService {}
```

### controller

| 項目 | 値 |
|---|---|
| ディレクトリ | `presentation/controller/` |
| 種別 | `class` |
| クラス名 | `{Name}Controller` |
| アノテーション | `@RestController`, `@RequestMapping("/{module}/{name}")`, `@Slf4j`, `@RequiredArgsConstructor` |
| import | Spring Web, Lombok |

```java
@RestController
@RequestMapping("/orders")
@Slf4j
@RequiredArgsConstructor
public class OrderController {

  // DI フィールドをここに追加
}
```

### request

| 項目 | 値 |
|---|---|
| ディレクトリ | `presentation/request/` |
| 種別 | `record` |
| クラス名 | `{Name}Request` |

```java
public record CreateOrderRequest() {}
```

### response

| 項目 | 値 |
|---|---|
| ディレクトリ | `presentation/response/` |
| 種別 | `record` |
| クラス名 | `{Name}Response` |

```java
public record OrderResponse() {}
```

### repositoryimpl

| 項目 | 値 |
|---|---|
| ディレクトリ | `infrastructure/db/repository/` |
| 種別 | `class` |
| クラス名 | `{Name}RepositoryImpl` |
| 実装 | `implements {Name}Repository` |
| アノテーション | `@Slf4j`, `@RequiredArgsConstructor` |
| import | Lombok, `org.jooq.DSLContext` |
| 備考 | 通常は `repository` layer から自動連鎖で生成。単独生成も可能 |

```java
@Slf4j
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

  private final DSLContext dsl;
}
```

### queryimpl

| 項目 | 値 |
|---|---|
| ディレクトリ | `infrastructure/db/query/` |
| 種別 | `class` |
| クラス名 | `{Name}QueryServiceImpl` |
| 実装 | `implements {Name}QueryService` |
| アノテーション | `@Slf4j`, `@RequiredArgsConstructor` |
| import | Lombok, `org.jooq.DSLContext` |
| 備考 | 通常は `queryservice` layer から自動連鎖で生成。単独生成も可能 |

```java
@Slf4j
@RequiredArgsConstructor
public class OrderQueryServiceImpl implements OrderQueryService {

  private final DSLContext dsl;
}
```

---

## 自動連鎖生成

一部の layer では関連ファイルを自動生成する。

| トリガー layer | 自動生成されるファイル | 生成先 |
|---|---|---|
| `aggregate {Name}` | `{Name}Id` | `domain/model/valueobject/identifier/` |
| `entity {Name}` | `{Name}Id` | `domain/model/valueobject/identifier/` |
| `repository {Name}` | `{Name}RepositoryImpl` | `infrastructure/db/repository/` |
| `queryservice {Name}` | `{Name}QueryServiceImpl` | `infrastructure/db/query/` |

自動連鎖で生成されるファイルが既に存在する場合はスキップする。

---

## 使用例

```bash
# 集約ルート + Identifier を作成
./scripts/create-class.sh order aggregate Order
# → order/domain/model/aggregate/Order.java
# → order/domain/model/valueobject/identifier/OrderId.java

# エンティティ + Identifier を作成（集約ルート指定必須）
./scripts/create-class.sh order entity OrderItem --aggregate Order
# → order/domain/model/entity/OrderItem.java
# → order/domain/model/valueobject/identifier/OrderItemId.java

# 値オブジェクトを作成
./scripts/create-class.sh order valueobject Money
# → order/domain/model/valueobject/Money.java

# イベントを作成
./scripts/create-class.sh order event OrderCreated
# → order/event/OrderCreatedEvent.java

# コマンド + ハンドラを作成
./scripts/create-class.sh order command CreateOrder
./scripts/create-class.sh order commandhandler CreateOrder
# → order/application/command/dto/CreateOrderCommand.java
# → order/application/command/handler/CreateOrderCommandHandler.java

# イベントリスナーを作成（他モジュールのイベントを受信）
./scripts/create-class.sh order eventlistener OrderCreated
# → order/application/command/handler/OrderCreatedEventListener.java

# リポジトリ（interface + impl 自動連鎖）
./scripts/create-class.sh order repository Order
# → order/domain/repository/OrderRepository.java
# → order/infrastructure/db/repository/OrderRepositoryImpl.java

# クエリサービス（interface + impl 自動連鎖）
./scripts/create-class.sh order queryservice Order
# → order/application/query/service/OrderQueryService.java
# → order/infrastructure/db/query/OrderQueryServiceImpl.java

# コントローラ
./scripts/create-class.sh order controller Order
# → order/presentation/controller/OrderController.java

# リクエスト / レスポンス
./scripts/create-class.sh order request CreateOrder
./scripts/create-class.sh order response Order
# → order/presentation/request/CreateOrderRequest.java
# → order/presentation/response/OrderResponse.java
```

---

## PMD / アーキテクチャテスト適合

| 制約 | 対応方法 |
|---|---|
| `AtLeastOneConstructor` | class には `@RequiredArgsConstructor` を付与（Lombok 生成コンストラクタで充足） |
| record のみパッケージ | `event`, `command/dto`, `query/dto`, `request`, `response` は record で生成 |
| interface のみパッケージ | `domain/repository`, `query/service` は interface で生成 |
| DDD 型制約 | aggregate → `AggregateRoot`, entity → `Entity`, identifier → `Identifier`, valueobject → `ValueObject` |
| CQRS アノテーション制約 | `@Command` → `command/dto`, `@CommandHandler` → `command/handler`, `@QueryModel` → `query/dto`, `@DomainEvent` → `event` |
| `@RestController` 制約 | `presentation/controller` のみ |
| フィールドインジェクション禁止 | `@RequiredArgsConstructor` + `final` フィールドで対応 |
| domain → Spring 依存禁止 | domain 内のクラスに Spring import を含めない |
| query → domain 依存禁止 | query 系ファイルに domain の import を含めない |
