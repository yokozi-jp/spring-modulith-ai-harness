# タイムゾーン・日時規約

## タイムゾーン

- サーバー（JVM）と DB は UTC に統一する
- JVM（dev）: `build.gradle` の `bootRun.jvmArgs` で `-Duser.timezone=UTC` を指定
- JVM（本番）: 環境変数 `TZ=UTC` を設定（`JAVA_TOOL_OPTIONS=-Duser.timezone=UTC` でも可）
- PostgreSQL: `.env` の `TZ=UTC` を compose.yaml 経由で設定
- タイムゾーン変換はフロントエンドで行う

## DB のタイムスタンプ型

- `timestamptz`（`TIMESTAMP WITH TIME ZONE`）を使用する
- `timestamp`（タイムゾーンなし）は使わない
- Liquibase changeset では型名 `timestamptz` を指定する

## Java の日時 API

- `java.time` API を使用する（`java.util.Date`, `java.util.Calendar` は禁止）
- タイムスタンプ: `Instant`（UTC の瞬間）または `OffsetDateTime`（タイムゾーン付き）
- 日付のみ: `LocalDate`
- 時刻のみ: `LocalTime`
- `LocalDateTime` は「タイムゾーンなしの日時」であり、DB の `timestamptz` とは対応しないため原則使わない
