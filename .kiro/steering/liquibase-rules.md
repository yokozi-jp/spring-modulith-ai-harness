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
  1. `./gradlew update` でマイグレーション適用
  2. `./gradlew rollbackCountSql -PliquibaseCount=1` でロールバック SQL を確認
  3. `./gradlew rollbackCount -PliquibaseCount=1` でロールバック実行
  4. `./gradlew update` で再適用できることを確認
