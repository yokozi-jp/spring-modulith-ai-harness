import { defineConfig } from "vite-plus";
import react, { reactCompilerPreset } from "@vitejs/plugin-react";
import babel from "@rolldown/plugin-babel";
import tailwindcss from "@tailwindcss/vite";
import { tanstackRouter } from "@tanstack/router-plugin/vite";

export default defineConfig({
  plugins: [
    tanstackRouter({
      target: "react",
      autoCodeSplitting: true,
      semicolons: true,
      quoteStyle: "double",
    }),
    react(),
    babel({ presets: [reactCompilerPreset()] }),
    tailwindcss(),
  ],
  resolve: {
    alias: {
      "@": new URL("./src", import.meta.url).pathname,
    },
  },
  server: {
    proxy: {
      "/api": {
        target: "http://localhost:18080",
        changeOrigin: true,
      },
    },
  },
  fmt: { ignorePath: ".oxfmtignore" },
  lint: {
    options: { typeAware: true, typeCheck: true },
    ignorePatterns: ["src/api/**", "src/routeTree.gen.ts", "eslint-plugins/**"],
    categories: {
      correctness: "error",
      suspicious: "warn",
      pedantic: "warn",
      perf: "warn",
      style: "warn",
      restriction: "warn",
    },
    settings: {
      react: {
        linkComponents: [{ name: "Link", attribute: "to" }],
      },
    },
    env: {
      browser: true,
      es2024: true,
    },
    plugins: ["typescript", "react", "unicorn", "import", "jsx-a11y", "oxc"],
    jsPlugins: ["./eslint-plugins/project-rules.js"],
    rules: {
      "typescript/no-explicit-any": "error",
      "typescript/no-non-null-assertion": "error",
      "typescript/consistent-type-imports": "error",
      "typescript/ban-ts-comment": "error",
      "typescript/consistent-type-definitions": ["error", "interface"],
      "typescript/array-type": ["error", { default: "array" }],
      "typescript/no-empty-object-type": "error",
      radix: "error",
      "no-new-wrappers": "error",
      "no-var": "error",
      eqeqeq: "error",
      "prefer-const": "error",
      "no-nested-ternary": "error",
      "prefer-destructuring": "error",
      "object-shorthand": "error",
      "prefer-template": "error",
      "prefer-spread": "error",
      "prefer-rest-params": "error",
      "prefer-exponentiation-operator": "error",
      "no-array-constructor": "error",
      "no-object-constructor": "error",
      "guard-for-in": "error",
      "no-throw-literal": "error",
      "typescript/only-throw-error": "error",
      "no-use-before-define": ["error", { functions: false }],
      "no-shadow": "error",
      "default-case-last": "error",
      "default-case": "error",
      "array-callback-return": "error",
      "no-case-declarations": "error",
      "no-else-return": "error",

      "react/no-array-index-key": "error",
      "react/jsx-no-target-blank": "error",
      "react/no-danger": "error",
      "react/function-component-definition": ["error", { namedComponents: "function-declaration" }],

      "unicorn/filename-case": ["error", { case: "kebabCase" }],
      "unicorn/prefer-node-protocol": "error",

      "import/no-default-export": "error",
      "import/no-cycle": "error",

      "project-rules/no-direct-api-client": "error",
      "project-rules/hook-in-dedicated-file": "error",
      "project-rules/no-arrow-function-hook": "error",
      "project-rules/no-props-object-param": "error",

      "no-restricted-imports": [
        "error",
        {
          patterns: [
            {
              regex: "^\\.\\./",
              message: "親ディレクトリへの相対パスは禁止です。@/ エイリアスを使用してください。",
            },
            {
              group: ["*/index", "*/index.*"],
              message:
                "barrel export (index.ts) からのimportは禁止です。モジュールを直接importしてください。",
            },
          ],
        },
      ],

      "typescript/no-restricted-types": [
        "error",
        {
          types: {
            "React.FC": {
              message: "React.FC は使用禁止です。通常の関数宣言を使用してください。",
            },
            "React.FunctionComponent": {
              message: "React.FunctionComponent は使用禁止です。通常の関数宣言を使用してください。",
            },
            FC: {
              message: "FC は使用禁止です。通常の関数宣言を使用してください。",
            },
          },
        },
      ],

      "unicorn/no-null": "off",
      "unicorn/no-negated-condition": "off",
      "unicorn/no-useless-undefined": "off",
      "max-lines": "off",
      "max-lines-per-function": "off",
      "max-params": "off",
      "id-length": "off",
      "no-undefined": "off",
      "no-magic-numbers": "off",
      "no-ternary": "off",
      "no-plusplus": "off",
      "sort-keys": "off",
      "sort-imports": "off",
      "react/no-set-state": "off",
      "typescript/no-magic-numbers": "off",
      "oxc/no-optional-chaining": "off",
      "jsx-a11y/no-autofocus": "off",
    },
    overrides: [
      {
        files: ["src/routes/**/*.tsx"],
        rules: {
          "import/no-default-export": "off",
          "no-restricted-imports": [
            "error",
            {
              patterns: [
                {
                  regex: "^\\.\\./",
                  message:
                    "親ディレクトリへの相対パスは禁止です。@/ エイリアスを使用してください。",
                },
                {
                  group: ["@/lib/api-*"],
                  message:
                    "routesファイル内でAPI呼び出しを直接importしないでください。loaderまたはfeatures内のHookで行ってください。",
                },
                {
                  group: ["@/api/*"],
                  message:
                    "routesファイル内でAPI Hookを直接importしないでください。features内のHookで行ってください。",
                },
              ],
            },
          ],
        },
      },
      {
        files: ["*.config.ts", "vite.config.ts", "orval.config.ts"],
        rules: {
          "import/no-default-export": "off",
          "no-restricted-imports": "off",
        },
      },
      {
        files: ["src/api/**/*.ts"],
        rules: {
          "no-restricted-imports": "off",
          "typescript/no-explicit-any": "off",
          "typescript/no-misused-spread": "off",
          "typescript/no-base-to-string": "off",
          "import/no-default-export": "off",
          "typescript/consistent-type-definitions": "off",
          "no-nested-ternary": "off",
        },
      },
    ],
  },
  test: {
    globals: true,
    environment: "jsdom",
    include: ["src/**/*.test.{ts,tsx}"],
    coverage: {
      include: ["src/**/*.{ts,tsx}"],
      exclude: ["src/api/**", "src/routeTree.gen.ts", "src/routes/**"],
    },
  },
  staged: {
    "src/**/*.{ts,tsx}": [
      "vp lint --fix",
      "vp fmt",
      // カスタムチェック（verify.sh と同じ）
      "./scripts/checks/check-hook-location.sh",
      "./scripts/checks/check-features-structure.sh",
      "./scripts/checks/check-test-exists.sh",
      "./scripts/checks/check-ui-readonly.sh",
      "./scripts/checks/api-readonly.sh",
    ],
  },
});
