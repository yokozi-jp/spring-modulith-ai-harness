SHELL := /bin/bash
export PATH := /usr/bin:$(PATH)

.PHONY: dev dev-up dev-down test test-only test-down e2e fmt jooq migrate rollback rollback-sql

# ローカル開発（フォアグラウンド）
dev:
	docker compose up

# ローカル開発（バックグラウンド）
dev-up:
	docker compose up -d

dev-down:
	docker compose down

# テスト実行（コンテナ内で ./gradlew check）
test:
	docker compose -f compose-test.yaml up --abort-on-container-exit --exit-code-from backend-test backend-test; \
	status=$$?; \
	docker compose -f compose-test.yaml down; \
	exit $$status

# 特定テストのみ実行（例: make test-only T='*LiquibaseMigrationTest'）
test-only:
	docker compose -f compose-test.yaml run --rm backend-test ./gradlew test --tests '$(T)' --project-cache-dir=/app/.gradle-docker-project; \
	status=$$?; \
	docker compose -f compose-test.yaml down; \
	exit $$status

test-down:
	docker compose -f compose-test.yaml down

# E2E テスト実行（全コンテナ必要）
e2e:
	docker compose -f compose-test.yaml run --rm backend-test ./gradlew e2eTest --project-cache-dir=/app/.gradle-docker-project; \
	status=$$?; \
	docker compose -f compose-test.yaml down; \
	exit $$status

# フォーマット適用
fmt:
	docker compose exec backend ./gradlew spotlessApply

# jOOQ コード生成
jooq:
	docker compose exec backend ./gradlew generateJooq

# Liquibase マイグレーション適用
migrate:
	docker compose exec backend ./gradlew update

# Liquibase ロールバック SQL 確認
rollback-sql:
	docker compose exec backend ./gradlew rollbackCountSql -PliquibaseCount=$(or $(COUNT),1)

# Liquibase ロールバック実行
rollback:
	docker compose exec backend ./gradlew rollbackCount -PliquibaseCount=$(or $(COUNT),1)
