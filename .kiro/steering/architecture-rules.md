# アーキテクチャルール — ArchUnit / jMolecules / Spring Modulith

本プロジェクトでは ArchUnit + jMolecules + Spring Modulith によるアーキテクチャテストを CI で実行している。
コード生成・変更時は以下のルールに従い、テスト違反を防ぐこと。

テストコード: `backend/src/test/java/com/example/demo/architecture/`

---

## モジュール構造

各モジュールは `com.example.demo.<module>` の直下サブパッケージとして配置する。
モジュールルートパッケージ（`<module>/` 直下）にはモジュール公開 API（例外クラス等）を配置可能。
モジュール内には以下のディレクトリ構成が必須であり、すべてに `package-info.java` を含めること。

```
<module>/
├── event/
├── domain/
│   ├── model/
│   │   ├── aggregate/
│   │   ├── entity/
│   │   └── valueobject/
│   │       └── identifier/
│   ├── repository/
│   └── service/
├── application/
│   ├── command/
│   │   ├── dto/
│   │   └── handler/
│   └── query/
│       ├── dto/
│       └── service/
├── presentation/
│   ├── controller/
│   ├── request/
│   └── response/
└── infrastructure/
    └── db/
        ├── repository/
        └── query/
```

---

## package-info.java の必須要件

### @NullMarked

`com.example.demo` 配下の全パッケージの `package-info.java` に `@NullMarked` を付与すること。

```java
@NullMarked
package com.example.demo.modulename.subpackage;

import org.jspecify.annotations.NullMarked;
```

### Onion Architecture アノテーション

各パッケージの `package-info.java` に、以下の対応に従って jMolecules Onion Architecture アノテーションを付与すること。

| パッケージ | アノテーション |
|---|---|
| `event/` | `@DomainModelRing` |
| `domain/` およびそのサブパッケージ全体 (`model/`, `aggregate/`, `entity/`, `valueobject/`, `identifier/`, `repository/`) | `@DomainModelRing` |
| `domain/service/` | `@DomainServiceRing` |
| `application/` およびそのサブパッケージ全体 (`command/`, `command/dto/`, `command/handler/`, `query/`, `query/dto/`, `query/service/`) | `@ApplicationServiceRing` |
| `presentation/` およびそのサブパッケージ全体 (`controller/`, `request/`, `response/`) | `@InfrastructureRing` |
| `infrastructure/` およびそのサブパッケージ全体 (`db/`, `db/repository/`, `db/query/`) | `@InfrastructureRing` |

---

## CQRS アノテーション配置制約

### クラスレベルアノテーション

| アノテーション | 配置先パッケージ |
|---|---|
| `@Command` | `..command.dto..` のみ |
| `@QueryModel` | `..query.dto..` のみ |
| `@DomainEvent` | `..event..` のみ |
| `@RestController` | `..presentation.controller..` のみ |

command パッケージに `@QueryModel` を、query パッケージに `@Command` を配置してはいけない。

### メソッドレベルアノテーション

| アノテーション | 付与可能な場所 |
|---|---|
| `@CommandHandler` | `..command.handler..` 内のクラスのメソッドにのみ付与可能 |

### 使用すべき完全修飾名

- `org.jmolecules.architecture.cqrs.Command`（非 deprecated）
- `org.jmolecules.architecture.cqrs.QueryModel`（非 deprecated）
- `org.jmolecules.architecture.cqrs.CommandHandler`（メソッドレベル、非 deprecated）
- `org.jmolecules.event.annotation.DomainEvent`

**使用禁止**: `org.jmolecules.architecture.cqrs.annotation.*`（deprecated）

---

## パッケージ依存制約

- **query → domain 禁止**: query パッケージは domain パッケージに依存してはいけない。
- **domain → Spring 禁止**: domain パッケージは Spring に依存してはいけない。ただし以下は例外とする:
  - `domain/service` と `domain/repository` は jMolecules ByteBuddy プラグインにより Spring ステレオタイプアノテーションがビルド時にバイトコードレベルで付与されるため除外（ソースコード上は jMolecules アノテーションのみ使用）。
  - `AbstractAggregateRoot`（`org.springframework.data.domain.AbstractAggregateRoot`）の継承は許可する。集約ルートは `AbstractAggregateRoot` を継承し、`registerEvent()` でドメインイベントを登録する。
- **presentation → infrastructure 禁止**: presentation は infrastructure に依存してはいけない。
- **presentation → domain 禁止**: presentation パッケージは domain パッケージに依存してはいけない。例外クラスはモジュールルートパッケージ（`<module>/` 直下）に配置し、presentation と application の両方から参照可能にする。
- **子 → 親パッケージ依存禁止**: 子パッケージから親パッケージへの依存を禁止する。

---

## 型制約

### インターフェースのみ

- `domain/repository/` — インターフェースのみ配置可能（`Repository` インターフェースおよび `IdGenerator` インターフェース）
- `query/service/` — インターフェースのみ配置可能

### record のみ（package-info を除く）

- `command/dto/` — record のみ
- `query/dto/` — record のみ
- `event/` — record のみ
- `presentation/request/` — record のみ
- `presentation/response/` — record のみ

### DDD 型とパッケージの双方向制約

| 型 | 配置先パッケージ |
|---|---|
| `AggregateRoot` 実装 | `model/aggregate/` のみ |
| `Entity` 実装（AggregateRoot 除く） | `model/entity/` のみ |
| `Identifier` 実装 | `model/valueobject/identifier/` のみ |
| `ValueObject` 実装 | `model/valueobject/` のみ |
| `Repository` 実装 | `domain/repository/` のみ |

逆方向も検証される: 各パッケージには対応する型の実装のみ配置可能。

---

## 不変性制約

### フィールドの不変性

集約（`AggregateRoot` 実装）およびエンティティ（`Entity` 実装）の全インスタンスフィールドは `private final` でなければならない。
状態変更は新しいインスタンスを返すパターンで実装する。

```java
@Getter
@EqualsAndHashCode(of = "id", callSuper = false)
public class Order extends AbstractAggregateRoot<Order> implements AggregateRoot<Order, OrderId> {

  private final OrderId id;
  private final String customerName;
  private final OrderStatus status;

  // コンストラクタ（Factory から呼び出す）
  Order(final OrderId id, final String customerName) {
    this.id = id;
    this.customerName = customerName;
    this.status = OrderStatus.CREATED;
    registerEvent(new OrderCreated(id));
  }

  // 状態変更は新しいインスタンスを返す
  public Order confirm() {
    return Order.reconstitute(this.id, this.customerName, OrderStatus.CONFIRMED);
  }
}
```

### equals/hashCode の規約

集約およびエンティティの `equals`/`hashCode` は ID のみで比較する。`@EqualsAndHashCode(of = "id")` を使用すること。
`AbstractAggregateRoot` を継承する集約ルートでは `@EqualsAndHashCode(of = "id", callSuper = false)` を指定する。

---

## 集約の再構築パターン

`RepositoryImpl` から集約を再構築する場合は、集約クラス内に `public static reconstitute(...)` メソッドを定義する。
直接 `new` による集約の生成は `model/aggregate/` 内と `domain/service/` 内でのみ許可する。

```java
@Getter
@EqualsAndHashCode(of = "id", callSuper = false)
public class Order extends AbstractAggregateRoot<Order> implements AggregateRoot<Order, OrderId> {

  private final OrderId id;
  private final String customerName;
  private final OrderStatus status;

  // Factory から呼び出す新規作成用コンストラクタ
  Order(final OrderId id, final String customerName) {
    this.id = id;
    this.customerName = customerName;
    this.status = OrderStatus.CREATED;
    registerEvent(new OrderCreated(id));
  }

  // RepositoryImpl からの再構築用
  public static Order reconstitute(
      final OrderId id, final String customerName, final OrderStatus status) {
    return new Order(id, customerName, status);
  }

  // 全フィールドを受け取る private コンストラクタ（reconstitute 用）
  private Order(final OrderId id, final String customerName, final OrderStatus status) {
    this.id = id;
    this.customerName = customerName;
    this.status = status;
  }
}
```

---

## Bean 登録アノテーション規約

実装クラスには以下のアノテーションを付与し、Spring Bean として登録する。
jMolecules アノテーションは ByteBuddy プラグインにより対応する Spring ステレオタイプアノテーションにビルド時変換される。

| クラス | 付与するアノテーション | ByteBuddy 変換先 |
|---|---|---|
| `*RepositoryImpl` | `@org.jmolecules.ddd.annotation.Repository` | → `@springframework.stereotype.Repository` |
| `*Factory` | `@org.jmolecules.ddd.annotation.Factory` | → `@springframework.stereotype.Component` |
| `*DomainService` | `@org.jmolecules.ddd.annotation.Service` | → `@springframework.stereotype.Service` |
| `*QueryServiceImpl` | `@org.springframework.stereotype.Component` | （直接付与、jMolecules マッピングなし） |
| `*IdGeneratorImpl` | `@org.springframework.stereotype.Component` | （直接付与、jMolecules マッピングなし） |
| `*CommandHandler` | `@org.springframework.stereotype.Component` | （直接付与、jMolecules マッピングなし） |
| `*EventListener` | `@org.springframework.stereotype.Component` | （直接付与、jMolecules マッピングなし）。`@ApplicationModuleListener` だけでは Bean 登録されない |

上記以外の Spring Bean（jMolecules マッピング対象外）は `@org.springframework.stereotype.Component` を直接付与する。

`create-class.sh` が自動付与するため手動で追加する必要はない。

### IdGenerator の構成

- `*IdGenerator` インターフェースは `domain/repository/` に配置する（Repository インターフェースと同様）。
- `*IdGeneratorImpl` 実装クラスは `infrastructure/db/repository/` に配置し、`@Component` を付与する。
- `aggregate` 生成時は Identifier・Factory・Repository・RepositoryImpl が自動連鎖生成される。
- `entity` 生成時は Identifier・Factory・IdGenerator・IdGeneratorImpl が自動連鎖生成される。

---

## トランザクション管理

`@Transactional` は CommandHandler の public メソッドにのみ付与する。
ArchUnit で配置を検証し、CommandHandler 以外のクラスに `@Transactional` が付与されている場合はテスト違反とする。

---

## jMolecules ルール

- **Onion Architecture (Classical)**: リング間の依存方向を検証する。内側のリングは外側に依存してはいけない。
- **DDD ルール**: 集約境界・エンティティ識別子・値オブジェクトの構造を検証する。

---

## ArchUnit 汎用ルール

### コーディングルール

- **java.util.logging 禁止**: SLF4J に統一する。
- **フィールドインジェクション禁止**: コンストラクタインジェクション（`@RequiredArgsConstructor`）を使用する。
- **@Deprecated API 使用禁止**

### プロキシルール（同一クラス内の直接呼び出し禁止）

以下のアノテーション付きメソッドを同一クラス内から直接呼び出してはいけない（Spring AOP プロキシが機能しないため）:

- `@Transactional`
- `@Cacheable`
- `@Async`

---

## Spring Modulith ルール

- **モジュール境界検証**: `ApplicationModules.of(DemoApplication.class).verify()` によりモジュール境界違反と循環依存を検出する。他モジュールの内部（サブパッケージ）クラスに直接アクセスしてはいけない。
- **ドキュメント自動生成**: モジュール構成図（PlantUML）とモジュールキャンバスを自動生成する。
