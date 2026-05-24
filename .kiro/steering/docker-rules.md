# Docker 構成ルール

## 方針

- 開発はコンテナ内 bootRun で行う（ADR-0001）
- `.env` で開発用環境変数を一元管理する
- `.env.test` でテスト用環境変数を管理する
- Dockerfile は本番デプロイでもそのまま使う（マルチステージ: dev + builder + runtime）
- テスト実行もコンテナ内で行う（Testcontainers は使用しない）

## Dockerfile 配置

各アプリの Dockerfile はそのアプリのディレクトリ直下に置く。`docker/` には置かない。

```
backend/Dockerfile       ← context: ./backend
frontend/Dockerfile      ← context: ./frontend（将来）
docker/keycloak/         ← アプリ外の設定ファイル
```

## compose.yaml の構成（開発用）

- `make dev` で全サービス起動（インフラ + アプリ）
- app サービスは dev ステージをターゲットにし、ソースをバインドマウント
- ソース変更は DevTools のホットリロードで反映されるため、通常は `--build` 不要

```bash
# 開発（フォアグラウンド）
make dev

# 開発（バックグラウンド）
make dev-up

# 停止
make dev-down

# Dockerfile や build.gradle の依存を変更した場合はイメージを再ビルド
docker compose up --build
```

### `--build` が必要なケース

- `Dockerfile` を変更した
- `build.gradle` の依存を変更した
- 確実にクリーンな状態で起動したい

## compose-test.yaml の構成（テスト用）

- `make test` でインフラ + テスト実行コンテナを起動し、`./gradlew check` を実行
- テスト完了後に全コンテナを自動停止
- `.env.test` で環境変数を注入（コンテナ内ネットワークのサービス名で接続）

```bash
# テスト実行（全チェック）
make test

# 特定テストのみ実行
make test-only T='*LiquibaseMigrationTest'

# テストコンテナ停止
make test-down
```

## ベースイメージ

Amazon Corretto を使用する（ローカル開発環境と同じディストリビューション）。

- dev ステージ: `amazoncorretto:<version>`（JDK、bootRun 用）
- builder ステージ: `amazoncorretto:<version>`（JDK、fat JAR ビルド用）
- runtime ステージ: `amazoncorretto:<version>-headless`（JRE 相当、本番用）

## コンテナ名

- プロジェクトプレフィックス `smah-` を付与し、他プロジェクトとの衝突を防ぐ
- 開発用: `smah-backend`, `smah-postgres`, `smah-redis`, `smah-keycloak`, `smah-grafana-lgtm`
- テスト用: `smah-backend-test`, `smah-postgres-test`, `smah-redis-test`, `smah-keycloak-test`, `smah-grafana-lgtm-test`

## 環境変数

- `application.yaml` の環境固有の値はすべて `${ENV_VAR}` プレースホルダーで定義する
- プレースホルダーにデフォルト値を設定しない（未設定時は起動失敗させる）
- ローカル開発: `.env` で全環境変数を管理（`env_file: .env` で読み込み）
- テスト: `.env.test` で全環境変数を管理（`env_file: .env.test` で読み込み）
- 本番: 環境変数を直接設定する
- テンプレート: `.env.example`、`.env.test.example`（Git 管理）
- 実ファイル: `.env`、`.env.test`（`.gitignore` で除外）
