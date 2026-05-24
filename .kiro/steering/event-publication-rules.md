# Event Publication 規約

## 概要

Spring Modulith の Event Publication Registry（JDBC）を使用し、ドメインイベントの at-least-once 配信を保証する。

## completion-mode

`archive` を使用する。

- 完了イベントは `event_publication` → `event_publication_archive` へ自動移動される
- 主テーブルには未完了イベントのみが残り、クエリ性能を維持する
- アーカイブテーブルは監査ログとして機能する

## republish-outstanding-events-on-restart: false

起動時の未完了イベント自動再発行は **無効** にする。

理由:
- マルチインスタンスデプロイ時に、他インスタンスが処理中のイベントを「未完了」と誤判定し重複処理が発生するリスクがある
- 未完了イベントのリカバリは `IncompleteEventPublications` API を定期実行して行う（時間条件で絞り込めるため安全）

## 未完了イベントのリカバリ戦略

`IncompleteEventPublications.resubmitIncompletePublicationsOlderThan(Duration)` を定期実行する。

- 対象: `completion_date IS NULL` かつ一定時間経過したイベント
- 処理中のイベントを誤って再送しないよう、十分な Duration（例: 1 時間）を設定する
- 失敗イベントは `FailedEventPublications` API でリトライ回数を制限する

## アーカイブテーブルの保持期間

アーカイブテーブル（`event_publication_archive`）も無限に肥大化するため、定期的に古いレコードを削除する。

- `CompletedEventPublications.deletePublicationsOlderThan(Duration)` を使用する
- 保持期間はビジネス要件に応じて決定する（例: 30 日）

## スキーマ管理

- `schema-initialization.enabled: false` — Liquibase でスキーマを管理するため、Spring Modulith の自動スキーマ作成は無効
- テーブル定義: `001-create-event-publication.yaml`, `002-create-event-publication-archive.yaml`

## 監視

- `/actuator/modulith` エンドポイントで未完了イベントの状態を確認可能
- 未完了イベントが長時間滞留している場合はアラートを設定する
