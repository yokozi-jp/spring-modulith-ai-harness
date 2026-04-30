# Docker 構成ルール

## 方針

- 開発はコンテナ内 bootRun で行う（ADR-0001）
- `.env` で全環境変数を一元管理する
- Dockerfile は本番デプロイでもそのまま使う（マルチステージ: dev + builder + runtime）

## Dockerfile 配置

各アプリの Dockerfile はそのアプリのディレクトリ直下に置く。`docker/` には置かない。

```
backend/Dockerfile       ← context: ./backend
frontend/Dockerfile      ← context: ./frontend（将来）
docker/keycloak/         ← アプリ外の設定ファイル
```

## compose.yaml の構成

- プロファイルなし: インフラのみ（PostgreSQL, Keycloak, Grafana LGTM）
- `--profile app`: インフラ + アプリコンテナ（dev ステージ、バインドマウント + bootRun）

```bash
# 開発
docker compose up --build

# テスト・静的解析（ホストで実行）
cd backend && ./gradlew check
```

## ベースイメージ

Amazon Corretto を使用する（ローカル開発環境と同じディストリビューション）。

- dev ステージ: `amazoncorretto:<version>`（JDK、bootRun 用）
- builder ステージ: `amazoncorretto:<version>`（JDK、fat JAR ビルド用）
- runtime ステージ: `amazoncorretto:<version>-headless`（JRE 相当、本番用）

## 環境変数

- `application.yaml` の環境固有の値はすべて `${ENV_VAR}` プレースホルダーで定義する
- プレースホルダーにデフォルト値を設定しない（未設定時は起動失敗させる）
- ローカル開発: `.env` で全環境変数を管理（`env_file: .env` で読み込み）
- 本番: 環境変数を直接設定する
- 必要な環境変数の一覧は `.env.example` を参照する
- テスト: `test/resources/application-default.yaml` でダミー値を設定（Testcontainers が動的に上書き）
