SHELL := /bin/bash
export PATH := /usr/bin:$(PATH)

.PHONY: dev dev-up dev-down build be-up be-test be-test-only be-down be-quick be-lint be-fmt be-jooq be-migrate be-rollback be-rollback-sql e2e e2e-up e2e-only e2e-down clean

# イメージ再ビルド
build:
	docker compose build
	docker compose -f compose-test.yaml build
	docker compose -f compose-e2e.yaml build

# ローカル開発（フォアグラウンド）
dev:
	docker compose up

# ローカル開発（バックグラウンド）
dev-up:
	docker compose up -d

dev-down:
	docker compose down

# --- Backend ---

# テスト用コンテナが起動していなければ起動する
be-up:
	@docker compose -f compose-test.yaml up -d --wait

# テスト実行（コンテナ内で ./gradlew check）
be-test: be-up
	docker compose -f compose-test.yaml exec backend-test ./gradlew check --project-cache-dir=/gradle/project-cache

# 特定テストのみ実行（例: make be-test-only T='*LiquibaseMigrationTest'）
be-test-only: be-up
	docker compose -f compose-test.yaml exec backend-test ./gradlew test --tests '$(T)' --project-cache-dir=/gradle/project-cache

# テストコンテナ停止
be-down:
	docker compose -f compose-test.yaml down

# TDD 高速ループ用（compile + unit test のみ、PMD/SpotBugs/integration スキップ）
be-quick: be-up
	docker compose -f compose-test.yaml exec backend-test ./gradlew classes testClasses test -x integrationTest -x jacocoTestReport --project-cache-dir=/gradle/project-cache

# 静的解析のみ（Spotless check + PMD）
be-lint: be-up
	docker compose -f compose-test.yaml exec backend-test ./gradlew spotlessCheck pmdMain pmdTest --project-cache-dir=/gradle/project-cache

# フォーマット適用
be-fmt:
	docker compose exec backend ./gradlew spotlessApply

# jOOQ コード生成
be-jooq:
	docker compose exec backend ./gradlew generateJooq

# Liquibase マイグレーション適用
be-migrate:
	docker compose exec backend ./gradlew update

# Liquibase ロールバック SQL 確認
be-rollback-sql:
	docker compose exec backend ./gradlew rollbackCountSql -PliquibaseCount=$(or $(COUNT),1)

# Liquibase ロールバック実行
be-rollback:
	docker compose exec backend ./gradlew rollbackCount -PliquibaseCount=$(or $(COUNT),1)

# --- E2E ---

# E2E テスト用コンテナが起動していなければ起動する
e2e-up:
	@docker compose -f compose-e2e.yaml up -d --wait

# E2E テスト実行（全コンテナ必要）
e2e: e2e-up
	docker compose -f compose-e2e.yaml exec backend-e2e ./gradlew e2eTest --project-cache-dir=/gradle/project-cache

# 特定 E2E テストのみ実行（例: make e2e-only T='*ObservabilityTest'）
e2e-only: e2e-up
	docker compose -f compose-e2e.yaml exec backend-e2e ./gradlew e2eTest --tests '$(T)' --project-cache-dir=/gradle/project-cache

e2e-down:
	docker compose -f compose-e2e.yaml down

# --- 共通 ---

# 全コンテナ・ボリューム・ネットワークを削除
clean:
	docker compose down -v --remove-orphans
	docker compose -f compose-test.yaml down -v --remove-orphans
	docker compose -f compose-e2e.yaml down -v --remove-orphans
