import { test as setup, chromium } from "@playwright/test";
import { Client } from "pg";
import path from "path";

/**
 * E2E テストのグローバルセットアップ。
 *
 * 1. PostgreSQL に接続して TRUNCATE → テストデータ INSERT
 * 2. Keycloak からアクセストークンを取得（API テスト用）
 * 3. ブラウザで Keycloak ログインを通してセッションクッキーを確立（ブラウザテスト用）
 *
 * Project Dependencies パターンにより、全テストプロジェクトの前に実行される。
 */

const DATABASE_URL =
  process.env.DATABASE_URL ?? "postgresql://demo:demo@postgres:5432/demo";

const KEYCLOAK_TOKEN_URL =
  process.env.KEYCLOAK_TOKEN_URL ??
  "http://keycloak:8080/realms/demo/protocol/openid-connect/token";

const FRONTEND_URL = process.env.FRONTEND_URL ?? "http://frontend-e2e:80";

const KEYCLOAK_CLIENT_ID = process.env.KEYCLOAK_CLIENT_ID ?? "demo-app";
const KEYCLOAK_CLIENT_SECRET =
  process.env.KEYCLOAK_CLIENT_SECRET ?? "demo-app-secret";
const KEYCLOAK_USERNAME = process.env.KEYCLOAK_USERNAME ?? "testuser";
const KEYCLOAK_PASSWORD = process.env.KEYCLOAK_PASSWORD ?? "test";

/** storageState の保存先。 */
export const STORAGE_STATE_PATH = path.resolve(
  __dirname,
  "../.auth/storage-state.json",
);

setup("seed test data", async () => {
  const client = new Client({ connectionString: DATABASE_URL });
  await client.connect();

  try {
    // TRUNCATE（依存順序を考慮: closures → categories）
    await client.query(`
      TRUNCATE TABLE category_closures CASCADE;
      TRUNCATE TABLE categories CASCADE;
    `);

    // テストデータ INSERT
    // カテゴリ: 「食品」「飲料」の2件
    await client.query(`
      INSERT INTO categories (id, name, sort_order, parent_category_id, created_at, updated_at, created_by, updated_by, version)
      VALUES
        ('11111111-1111-1111-1111-111111111111', '食品', 1, NULL, NOW(), NOW(), 'system', 'system', 0),
        ('22222222-2222-2222-2222-222222222222', '飲料', 2, NULL, NOW(), NOW(), 'system', 'system', 0);
    `);

    // closure テーブル（自己参照 depth=0）
    await client.query(`
      INSERT INTO category_closures (ancestor_id, descendant_id, depth, created_at, updated_at, created_by, updated_by, version)
      VALUES
        ('11111111-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111111', 0, NOW(), NOW(), 'system', 'system', 0),
        ('22222222-2222-2222-2222-222222222222', '22222222-2222-2222-2222-222222222222', 0, NOW(), NOW(), 'system', 'system', 0);
    `);
  } finally {
    await client.end();
  }
});

setup("acquire auth token", async () => {
  // Keycloak の Resource Owner Password Grant でトークン取得（API テスト用）
  const response = await fetch(KEYCLOAK_TOKEN_URL, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: new URLSearchParams({
      grant_type: "password",
      client_id: KEYCLOAK_CLIENT_ID,
      client_secret: KEYCLOAK_CLIENT_SECRET,
      username: KEYCLOAK_USERNAME,
      password: KEYCLOAK_PASSWORD,
    }),
  });

  if (!response.ok) {
    const text = await response.text();
    throw new Error(
      `Failed to acquire token: ${response.status} ${response.statusText} - ${text}`,
    );
  }

  const data = (await response.json()) as { access_token: string };
  process.env.ACCESS_TOKEN = data.access_token;
});

setup("authenticate browser session", async () => {
  // ブラウザで backend 経由の OAuth2 ログインを通し、セッションクッキーを取得する。
  // backend に直接アクセス → Keycloak にリダイレクト → ログイン → backend にコールバック
  // → セッションクッキーが発行される → storageState に保存
  const backendURL = process.env.BACKEND_URL ?? "http://backend:18080";
  const browser = await chromium.launch();
  const context = await browser.newContext();
  const page = await context.newPage();

  // backend に直接アクセス（未認証なので Keycloak にリダイレクトされる）
  await page.goto(`${backendURL}/api/v1/categories`, {
    waitUntil: "domcontentloaded",
  });

  // Keycloak ログインフォームが表示されるのを待つ
  await page.waitForSelector("#username", { timeout: 15000 });

  // ログイン情報を入力
  await page.fill("#username", KEYCLOAK_USERNAME);
  await page.fill("#password", KEYCLOAK_PASSWORD);
  await page.click("#kc-login");

  // ログイン成功後、backend にリダイレクトされる（セッションクッキーが発行される）
  await page.waitForURL((url) => !url.href.includes("keycloak"), {
    timeout: 15000,
  });

  // storageState を保存（セッションクッキー含む）
  await context.storageState({ path: STORAGE_STATE_PATH });

  await browser.close();
});
