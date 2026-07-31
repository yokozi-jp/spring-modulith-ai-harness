/**
 * E2E テスト用 ESLint 設定
 *
 * AI がテストコードを生成する際のルール強制に使用する。
 * ESLint のエラーメッセージは「AI への修正指示書」として機能するため、
 * message には具体的な修正方法を含めること。
 *
 * ルール追加時の方針:
 *   - AI が頻繁に犯すミスを検出するルールを優先する
 *   - message で「何が間違いで、どう直すべきか」を明確に伝える
 *   - autofix 可能なものは fixable にする
 */
import eslint from "@eslint/js";
import tseslint from "typescript-eslint";

export default tseslint.config(
  eslint.configs.recommended,
  ...tseslint.configs.recommendedTypeChecked,
  {
    languageOptions: {
      parserOptions: {
        projectService: true,
        tsconfigRootDir: import.meta.dirname,
      },
    },
  },
  {
    // E2E テストファイル全体に適用するルール
    files: ["tests/**/*.ts", "pages/**/*.ts"],
    rules: {
      // --- await 忘れ防止（Playwright 操作で頻出） ---
      "@typescript-eslint/no-floating-promises": [
        "error",
        { ignoreVoid: false },
      ],

      // --- POM 経由を強制（page.locator() 直接使用を禁止） ---
      "no-restricted-syntax": [
        "error",
        {
          selector:
            "CallExpression[callee.object.name='page'][callee.property.name='locator']",
          message:
            "page.locator() の直接使用は禁止です。pages/ ディレクトリの Page Object を作成または使用してください。",
        },
        {
          selector:
            "CallExpression[callee.object.name='page'][callee.property.name='$']",
          message:
            "page.$() の直接使用は禁止です。pages/ ディレクトリの Page Object を作成または使用してください。",
        },
        {
          selector:
            "CallExpression[callee.object.name='page'][callee.property.name='$$']",
          message:
            "page.$$() の直接使用は禁止です。pages/ ディレクトリの Page Object を作成または使用してください。",
        },
      ],

      // --- 未使用変数の検出 ---
      "@typescript-eslint/no-unused-vars": [
        "error",
        { argsIgnorePattern: "^_" },
      ],
    },
  },
  {
    // テスト以外（設定ファイル等）は型チェックを無効化
    files: ["*.config.ts"],
    ...tseslint.configs.disableTypeChecked,
  },
  {
    ignores: [
      "node_modules/",
      "test-results/",
      "playwright-report/",
      ".playwright/",
    ],
  },
);
