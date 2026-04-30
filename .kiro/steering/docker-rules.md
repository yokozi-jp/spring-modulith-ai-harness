# Docker 構成ルール

## 方針

- 日常開発は `bootRun`（ホスト JVM）で行う（ADR-0001）
- コンテナ確認・E2E・CI 用に compose profiles でアプリコンテナを起動できる
- Dockerfile は本番デプロイでもそのまま使う

## Dockerfile 配置

各アプリの Dockerfile はそのアプリのディレクトリ直下に置く。`docker/` には置かない。

```
backend/Dockerfile       ← context: ./backend
frontend/Dockerfile      ← context: ./frontend（将来）
docker/keycloak/         ← アプリ外の設定ファイル
```

理由: ビルドコンテキストをアプリディレクトリに閉じることで、不要ファイルの送信を防ぎ、Dockerfile 内のパスをシンプルに保つ。

## compose.yaml の構成

- プロファイルなし: インフラのみ（PostgreSQL, Keycloak, Grafana LGTM）
- `--profile app`: インフラ + アプリコンテナ

```bash
# 日常開発
docker compose up -d
cd backend && ./gradlew bootRun

# コンテナ確認
docker compose --profile app up --build
```

## ベースイメージ

Amazon Corretto を使用する（ローカル開発環境と同じディストリビューション）。

- builder ステージ: `amazoncorretto:<version>`（JDK）
- runtime ステージ: `amazoncorretto:<version>-headless`（JRE 相当）

## 環境変数

- `.env` で bootRun とコンテナ共通の変数を管理する
- コンテナ固有の上書き（DB/OAuth2 の Docker ネットワーク内 URL）は `compose.yaml` の `environment` で行う
- 本番では `.env` を使わず環境変数を直接設定する
