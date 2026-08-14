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
    ignorePatterns: [
      "src/api/**",
      "src/routeTree.gen.ts",
      "oxlint-plugins/**",
      "src/components/ui/**",
    ],
    categories: {
      correctness: "error",
      suspicious: "error",
      pedantic: "error",
      perf: "error",
      style: "error",
      restriction: "error",
    },
    settings: {
      react: {
        linkComponents: [{ name: "Link", attribute: "to" }],
      },
      "better-tailwindcss": {
        entryPoint: "src/styles/globals.css",
      },
    },
    env: {
      browser: true,
      es2024: true,
    },
    plugins: ["typescript", "react", "unicorn", "import", "jsx-a11y", "oxc", "promise"],
    jsPlugins: ["./oxlint-plugins/project-rules.js", "eslint-plugin-better-tailwindcss"],
    rules: {
      "better-tailwindcss/enforce-shorthand-classes": "error",
      "better-tailwindcss/no-duplicate-classes": "error",
      "better-tailwindcss/no-conflicting-classes": "error",
      "better-tailwindcss/no-unknown-classes": "error",
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
      // floating promise 対策の `void invalidateQueries(...)` 等（frontend-data-patterns.md）を
      // 許可するため、ステートメントとしての void 演算子は許容する
      "no-void": ["error", { allowAsStatement: true }],
      "no-use-before-define": ["error", { functions: false }],
      "no-shadow": "error",
      "default-case-last": "error",
      "default-case": "error",
      "array-callback-return": "error",
      "no-case-declarations": "error",
      "no-else-return": "error",
      "no-unreachable-loop": "error",

      "react/no-array-index-key": "error",
      "react/jsx-no-target-blank": "error",
      "react/no-danger": "error",
      "react/function-component-definition": ["error", { namedComponents: "function-declaration" }],
      "react/forbid-dom-props": [
        "error",
        {
          forbid: [
            {
              propName: "style",
              message:
                "インラインスタイルは禁止です。Tailwind CSS のユーティリティクラスを使用してください。",
            },
          ],
        },
      ],

      "unicorn/filename-case": ["error", { case: "kebabCase" }],
      "unicorn/prefer-node-protocol": "error",

      "import/no-default-export": "error",
      "import/no-cycle": "error",

      "project-rules/no-direct-api-client": "error",
      "project-rules/hook-in-dedicated-file": "error",
      "project-rules/no-arrow-function-hook": "error",
      "project-rules/no-props-object-param": "error",
      "project-rules/no-button-inside-link": "error",

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

      // 本プロジェクトの方針（関数宣言・named export 強制）と直接矛盾するため無効化
      "func-style": "off",
      "import/no-named-export": "off",
      "import/prefer-default-export": "off",

      // React コンポーネントの戻り値型明示は必須化しない
      // Google TS Style Guide も「著者の判断に委ねる」と明記（一般的な TS 規約ではない）
      "typescript/explicit-function-return-type": "off",
      "typescript/explicit-module-boundary-types": "off",

      // React の外部コンポーネント型（ComponentProps 等）に readonly を強制すると
      // 大量の書き換えが必要になり過剰
      "typescript/prefer-readonly-parameter-types": "off",

      // Shadcn/ui のスタンダードパターン（import * as React）を許容
      "import/no-namespace": "off",

      // 型 import と値 import の分離は consistent-type-imports の方針と一致するため許容
      "no-duplicate-imports": ["error", { allowSeparateTypeImports: true }],

      // 1関数1exportの慣習を保つため、named export の統合は強制しない
      "import/group-exports": "off",

      // TanStack Router の `export const Route = createFileRoute(...)` パターンは
      // ファイル先頭に export を書くのが規約のため、末尾強制は不適切
      "import/exports-last": "off",

      // 複雑度制限は max-lines-per-function 等と同様に不採用方針
      "max-statements": "off",

      // 日本語コメントに英語の大文字開始規則を適用するのは不適切
      "capitalized-comments": "off",

      // 否定条件の禁止は unicorn/no-negated-condition と同様に不採用方針
      "no-negated-condition": "off",

      // 後続の分岐で初期化する変数宣言パターンを許容
      "init-declarations": "off",

      // シンプルな正規表現に名前付きキャプチャグループや u フラグを強制しない
      "prefer-named-capture-group": "off",
      "require-unicode-regexp": "off",
      "typescript/prefer-regexp-exec": "off",

      // CSS 等のサイドエフェクト import を許容（Google TS Style Guide も明示的に許容）
      "import/no-unassigned-import": "off",

      // Orval 生成コードとの互換性のための意図的な型アサーション（api-client.ts）
      "typescript/no-unsafe-type-assertion": "off",

      // React 17+ の新 JSX 変換では import React が不要（現代の標準）
      "react/react-in-jsx-scope": "off",

      // JSX ネスト深さ制限は現実的なコンポーネントに対して過剰
      "react/jsx-max-depth": "off",

      // 本プロジェクトは .tsx を使用する規約（.jsx ではない）
      "react/jsx-filename-extension": "off",

      // 日本語 UI テキストを JSX 内に直接書くのは正当なパターン（i18n 未導入）
      "react/jsx-no-literals": "off",

      // className を Shadcn/ui コンポーネントに渡すのは標準パターン
      "react/forbid-component-props": "off",

      // ネイティブ HTML 要素の onClick はハンドラ名規約の対象外
      "react/jsx-handler-names": "off",

      // rest/spread 構文は本プロジェクトで標準的に使用
      "oxc/no-rest-spread-properties": "off",

      // async/await は標準的に使用
      "oxc/no-async-await": "off",

      // TanStack Router のファイルベースルーティング規約
      // （export const Route とコンポーネントを同一ファイルに書く）と矛盾するため無効化
      "react/only-export-components": "off",

      // new URL("./src", ...) の相対パス表記は明示的で正しい
      "unicorn/relative-url-style": "off",

      // setTimeout 等のコールバックベース API を Promise 化する際の
      // 典型パターン（await new Promise((resolve) => setTimeout(resolve, ms))）を許容する。
      // async/await に書き換えられない正当なケースが存在するため一律禁止は過剰
      "promise/avoid-new": "off",
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
      {
        files: ["**/*.test.{ts,tsx}"],
        rules: {
          // テストで使わないものを明示的に禁止する（frontend-test-patterns.md）
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
                  group: ["*/index", "*/index.*"],
                  message:
                    "barrel export (index.ts) からのimportは禁止です。モジュールを直接importしてください。",
                },
              ],
              paths: [
                {
                  name: "vitest",
                  message:
                    "vitest を直接 import しないでください。vite-plus/test を使用してください。",
                },
                {
                  name: "msw",
                  message: "MSW は使用禁止です。vi.mock を使用してください。",
                },
                {
                  name: "msw/node",
                  message: "MSW は使用禁止です。vi.mock を使用してください。",
                },
                {
                  name: "enzyme",
                  message: "enzyme は非推奨です。@testing-library/react を使用してください。",
                },
              ],
            },
          ],
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
    // src/api/ と src/components/ui/ は生成物のため lint 対象外
    // (vite.config.ts の lint.ignorePatterns と同じ除外)。
    // 拡張 glob (!(...)) でディレクトリ自体を除外し、
    // 生成物のみがステージされた場合に 0 件ヒットで
    // タスクが失敗扱いになることを防ぐ。
    "src/!(api|components)/**/*.{ts,tsx}": [
      "vp lint --fix",
      "vp fmt",
      // カスタムチェック（verify.sh と同じ）
      "./scripts/checks/check-hook-location.sh",
      "./scripts/checks/check-features-structure.sh",
      "./scripts/checks/check-test-exists.sh",
    ],
    "src/components/!(ui)/**/*.{ts,tsx}": [
      "vp lint --fix",
      "vp fmt",
      "./scripts/checks/check-hook-location.sh",
      "./scripts/checks/check-features-structure.sh",
      "./scripts/checks/check-test-exists.sh",
    ],
    // components/ui/ と src/api/ は生成物の誤編集検出のため、
    // 対象ディレクトリへの変更があれば拡張子を問わず必ず実行する
    // （新規追加ファイルのみのステージングでも発火させるため）
    "src/components/ui/**": ["./scripts/checks/check-ui-readonly.sh"],
    "src/api/**": ["./scripts/checks/api-readonly.sh"],
  },
});
