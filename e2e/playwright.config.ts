import { defineConfig, devices } from "@playwright/test";
import path from "path";

/**
 * E2E テスト設定。
 *
 * URL 構成:
 *   - ブラウザテスト（scenarios）: frontend-e2e (nginx) にアクセス
 *   - API テスト: backend に直接リクエスト
 *
 * 認証:
 *   - scenarios: global.setup.ts で取得した storageState（セッションクッキー）を使用
 *   - api: global.setup.ts で取得した ACCESS_TOKEN を Authorization ヘッダーで使用
 */

const frontendURL = process.env.FRONTEND_URL ?? "http://frontend-e2e:80";
const backendURL = process.env.BACKEND_URL ?? "http://backend:18080";

const STORAGE_STATE_PATH = path.resolve(__dirname, ".auth/storage-state.json");

export default defineConfig({
  testDir: "./tests",
  outputDir: "./test-results",
  reporter: [["html", { outputFolder: "./playwright-report" }], ["list"]],
  timeout: 30_000,
  retries: 0,
  projects: [
    {
      name: "setup db",
      testMatch: /global\.setup\.ts/,
      teardown: "cleanup db",
    },
    {
      name: "cleanup db",
      testMatch: /global\.teardown\.ts/,
    },
    {
      name: "api",
      testMatch: /api\/.*\.spec\.ts/,
      use: {
        baseURL: backendURL,
        extraHTTPHeaders: {
          Accept: "application/json",
        },
      },
      dependencies: ["setup db"],
    },
    {
      name: "scenarios",
      testMatch: /scenarios\/.*\.spec\.ts/,
      use: {
        ...devices["Desktop Chrome"],
        baseURL: frontendURL,
        storageState: STORAGE_STATE_PATH,
      },
      dependencies: ["setup db"],
    },
  ],
});
