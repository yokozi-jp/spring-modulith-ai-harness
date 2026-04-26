# アーキテクチャルール — ArchUnit / jMolecules / Spring Modulith

本プロジェクトでは ArchUnit + jMolecules + Spring Modulith によるアーキテクチャテストを CI で実行している。
コード生成・変更時は以下のルールに従い、テスト違反を防ぐこと。

テストコード: `backend/src/test/java/com/example/demo/architecture/`

---

## モジュール構造

各モジュールは `com.example.demo.<module>` の直下サブパッケージとして配置する。
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

| アノテーション | 配置先パッケージ |
|---|---|
| `@Command` | `..command.dto..` のみ |
| `@CommandHandler` | `..command.handler..` のみ |
| `@QueryModel` | `..query.dto..` のみ |
| `@DomainEvent` | `..event..` のみ |
| `@RestController` | `..presentation.controller..` のみ |

command パッケージに `@QueryModel` を、query パッケージに `@Command` を配置してはいけない。

---

## パッケージ依存制約

- **query → domain 禁止**: query パッケージは domain パッケージに依存してはいけない。
- **domain → Spring 禁止**: domain パッケージは Spring に依存してはいけない。ただし `domain/service` と `domain/repository` は jMolecules ByteBuddy プラグインにより Spring アノテーションが付与されるため除外。
- **presentation → infrastructure 禁止**: presentation は infrastructure に依存してはいけない。
- **子 → 親パッケージ依存禁止**: 子パッケージから親パッケージへの依存を禁止する。

---

## 型制約

### インターフェースのみ

- `domain/repository/` — インターフェースのみ配置可能
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
