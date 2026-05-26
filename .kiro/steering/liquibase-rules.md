# Liquibase 規約

## ファイル配置

- changeset ファイルは `backend/src/main/resources/db/changelog/migrations/` に配置する
- ファイル名: `NNN-<説明>.yaml`（例: `001-create-event-publication.yaml`）
- 番号は 3 桁ゼロ埋め、連番で採番する
- `db.changelog-master.yaml` の `includeAll` で自動読み込みされるため、個別の include 追加は不要

## changeset ID

- 形式: `NNN-<説明>`（ファイル名と一致させる）
- 例: `001-create-event-publication`, `002-create-event-publication-archive`
- 1 ファイル 1 changeset を原則とする

## author

- `system` を使用する（個人名は使わない）

## フォーマット

- YAML 形式で記述する（XML は使わない）
- Liquibase ネイティブの change type（`createTable`, `createIndex` 等）を優先する
- ネイティブで表現できない場合のみ `sql` を使用する
- 使用不可の change type: `addCheckConstraint` は Liquibase 5 の YAML パーサーで ParsedNodeException を起こすため使用不可。CHECK 制約は `sql` change type で記述し、`rollback` を明示すること

## ロールバック

- `createTable`, `createIndex` 等は Liquibase が自動ロールバックを生成するため、明示的なロールバックは不要
- `sql` で記述した場合は必ず `rollback` を明示する
- データ操作（`insert`, `update`, `delete`）を含む場合は必ず `rollback` を明示する

## 型の指定

- PostgreSQL の型を直接使用する（DB 移植性より明確さを優先）
- タイムスタンプ: `timestamptz`（`TIMESTAMP WITH TIME ZONE`）
- UUID: `uuid`
- 文字列: `text`
- 整数: `int`, `bigint`

## 既存 changeset の変更禁止

- 一度適用された changeset は変更しない（チェックサム不一致エラーになる）
- 修正が必要な場合は新しい changeset を追加する

## ロールバックテスト（必須）

- 新しい changeset を作成したら、必ずロールバックが正常に動作することを確認する
- 手順:
  1. `make migrate` でマイグレーション適用
  2. `make rollback-sql` でロールバック SQL を確認
  3. `make rollback` でロールバック実行
  4. `make migrate` で再適用できることを確認

---

## テーブル命名規約

- スキーマ: `demo`（単一スキーマ、モジュール別スキーマ分離はしない）
- 集約テーブル: 集約名の複数形スネークケース（`Order` → `orders`, `Product` → `products`）
- エンティティテーブル: `<集約名単数>_<エンティティ名複数>`（`OrderItem` → `order_items`, `ProductVariant` → `product_variants`）
- 中間テーブル: `<テーブルA>_<テーブルB>`（必要な場合）
- すべて小文字スネークケース（正規表現: `^[a-z][a-z0-9]*(_[a-z0-9]+)*$`）
- テーブル名は複数形で終わること

## 必須カラム

すべてのアプリケーションテーブル（除外テーブルを除く）に以下のカラムを必ず含めること。

| カラム | 型 | 制約 | 内容 |
|---|---|---|---|
| `created_at` | `timestamptz` | NOT NULL | レコード作成日時（UTC） |
| `updated_at` | `timestamptz` | NOT NULL | レコード更新日時（UTC） |
| `created_by` | `text` | NOT NULL | 作成者の Keycloak user ID（sub claim, UUID 文字列）。システム処理は `"system"` |
| `updated_by` | `text` | NOT NULL | 更新者の Keycloak user ID（sub claim, UUID 文字列）。システム処理は `"system"` |
| `version` | `int` | NOT NULL, DEFAULT 0 | 楽観ロック用バージョン |
| `deleted_at` | `timestamptz` | nullable | 論理削除日時（NULL なら有効、値ありなら削除済み） |

### changeset 例

```yaml
databaseChangeLog:
  - changeSet:
      id: "003-create-orders"
      author: "system"
      changes:
        - createTable:
            tableName: orders
            columns:
              - column:
                  name: id
                  type: uuid
                  constraints:
                    primaryKey: true
                    nullable: false
              - column:
                  name: customer_name
                  type: text
                  constraints:
                    nullable: false
              - column:
                  name: status
                  type: text
                  constraints:
                    nullable: false
              - column:
                  name: created_at
                  type: timestamptz
                  constraints:
                    nullable: false
              - column:
                  name: updated_at
                  type: timestamptz
                  constraints:
                    nullable: false
              - column:
                  name: created_by
                  type: text
                  constraints:
                    nullable: false
              - column:
                  name: updated_by
                  type: text
                  constraints:
                    nullable: false
              - column:
                  name: version
                  type: int
                  defaultValueNumeric: 0
                  constraints:
                    nullable: false
              - column:
                  name: deleted_at
                  type: timestamptz
```

## 論理削除ルール

- 全テーブルに `deleted_at` カラムを持たせ、物理削除は行わない
- `deleted_at IS NULL` → 有効レコード、`deleted_at IS NOT NULL` → 削除済み
- 集約の無効化も明細行の削除も `deleted_at` で統一的に表現する
- クエリ時は必ず `SoftDeleteCondition.notDeleted(TABLE)` ヘルパー（`com.example.demo.jooq.SoftDeleteCondition`）を使用すること
- jOOQ 生成コードの `DELETED_AT` フィールドを `SoftDeleteCondition` 以外から直接参照すると ArchUnit テスト（`JooqPolicy.NO_DIRECT_DELETED_AT_ACCESS`）で違反として検出される
- 削除済みを含めて取得する場合（管理画面等）はヘルパーを使わず、コメントで意図を明記すること

```java
// 通常クエリ: 有効レコードのみ
dsl.selectFrom(ORDERS)
    .where(SoftDeleteCondition.notDeleted(ORDERS))
    .and(ORDERS.STATUS.eq("ACTIVE"))
    .fetch();

// 管理画面: 削除済みも含めて取得（意図的にヘルパーを使わない）
// NOTE: 管理画面用に削除済みレコードも表示する
dsl.selectFrom(ORDERS)
    .where(ORDERS.STATUS.eq("ACTIVE"))
    .fetch();
```

## インデックス命名規約

- 通常インデックス: `<テーブル名>_<カラム名(s)>_idx`（例: `orders_customer_id_idx`）
- ユニーク制約: `<テーブル名>_<カラム名(s)>_key`（例: `orders_order_number_key`）
- 主キー: `<テーブル名>_pkey`（PostgreSQL が自動生成する命名に従う）
- 複合インデックス: カラム名をアンダースコアで連結（例: `orders_customer_id_status_idx`）
- すべて小文字スネークケース
- デフォルトは通常インデックス（`createIndex` ネイティブ change type で記述）
- 部分インデックスが必要な場合のみ `sql` change type で記述する

### 本番環境でのインデックス追加（参考）

本番環境でテーブルにロックをかけずにインデックスを追加する場合は `CREATE INDEX CONCURRENTLY` を使用する。

```yaml
- changeSet:
    id: "010-add-orders-customer-id-idx"
    author: "system"
    runInTransaction: false
    changes:
      - sql:
          sql: >-
            CREATE INDEX CONCURRENTLY orders_customer_id_idx
            ON orders (customer_id)
      - rollback:
          sql: DROP INDEX IF EXISTS demo.orders_customer_id_idx
```

## 外部キー制約

- モジュール内の外部キーは許可する
- モジュール間の外部キーは禁止する（モジュール境界を超える結合を防ぐ）
- 命名: `<テーブル名>_<カラム名>_fkey`（例: `order_items_order_id_fkey`）

## 除外テーブル

以下のテーブルは Spring Modulith / Liquibase が管理するため、DB 設計規約（必須カラム、命名規約等）の対象外とする。

- `event_publication` — Spring Modulith Event Publication Registry
- `event_publication_archive` — Spring Modulith Event Publication Archive
- `databasechangelog` — Liquibase 変更履歴
- `databasechangeloglock` — Liquibase ロック管理

## 設計判断

### `deleted_at` を採用する理由

1. 明細行の削除も集約の無効化も統一的に表現できる
2. 「いつ削除されたか」の情報を持てる（`is_deleted` boolean より情報量が多い）
3. 物理削除を避けることでデータ復旧・監査が容易

### `is_deleted`（boolean）を不採用とする理由

1. `deleted_at` で代替可能かつ情報量が少ない（削除日時がわからない）
2. カラムが増えるだけで利点がない

### `created_by` / `updated_by` を DB カラムに持つ理由

1. 分析・監査クエリで「誰が作成/更新したか」を DB レベルで直接参照できる
2. Keycloak user ID（UUID）を使用し、ユーザー名変更に影響されない

### 単一スキーマを採用する理由

1. Spring Modulith のモジュラーモノリスでは、モジュール境界はコード側で管理する
2. クロスモジュール JOIN（Query-only モジュール）が自然に書ける
3. Liquibase 設定・jOOQ codegen 設定がシンプルに保てる
