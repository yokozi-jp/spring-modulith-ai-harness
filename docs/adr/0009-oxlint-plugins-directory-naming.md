# `oxlint-plugins/` ディレクトリ: 実体は oxlint の JS プラグイン

`frontend/oxlint-plugins/project-rules.js` は、oxlint v1.x の JS plugin 機構（`jsPlugins` 設定）で
読み込まれる、プロジェクト固有のカスタムルール集である。

このディレクトリは元々 `eslint-plugins/` という名前だったが、本プロジェクトが ESLint を導入していない
にもかかわらず「ESLint の設定が別に存在する」という誤解を招いたため `oxlint-plugins/` にリネームした。

## 命名の背景

- `project-rules.js` は ESLint のプラグイン API（`meta`, `create(context)`, `context.report()` 等）と
  ほぼ同じ形式で書かれている
- これは oxlint が ESLint 互換の JS plugin API をサポートしているためであり、ESLint 本体への依存ではない
- JSDoc の型注釈 `@type {import('eslint').ESLint.Plugin}` も、型定義パッケージとして `eslint` の型を
  参照しているだけで、実行時に ESLint は一切関与しない

## なぜ ESLint を使わないのか

- oxlint（Rust 実装）は ESLint（Node.js 実装）よりも起動・実行が大幅に高速
- 本プロジェクトの Lint 設定は `frontend/vite.config.ts` の `lint` セクションに一元化されている
  （`categories`, `rules`, `overrides`, `plugins`, `jsPlugins` 等）。`.eslintrc.*` は存在せず、作成しない
- oxlint は主要な ESLint ルール・プラグイン（`typescript-eslint`, `eslint-plugin-react`,
  `eslint-plugin-import`, `eslint-plugin-unicorn`, `eslint-plugin-jsx-a11y` 等）をネイティブ実装として
  内蔵しており、大部分のケースで ESLint への切り替えが不要

## なぜカスタムルールが必要なのか

oxlint の組み込みルールでは表現できない、本プロジェクト固有のアーキテクチャ制約が存在する:

| カスタムルール | 検証内容 | 組み込みルールで代替できない理由 |
|---|---|---|
| `project-rules/no-direct-api-client` | `src/features/*/hooks/` 内で `apiClient` を直接 import することを禁止 | ファイルパスパターン（`features/*/hooks/`）と特定モジュール（`@/lib/api-client`）の組み合わせ検証が組み込みルールにない |
| `project-rules/hook-in-dedicated-file` | Hook 関数（`export function use...`）は `use-*.ts` ファイルでのみ定義可能 | ファイル名パターンと export 内容の組み合わせ検証が組み込みルールにない |
| `project-rules/no-arrow-function-hook` | Hook はアロー関数ではなく関数宣言で定義 | 組み込みの `react/function-component-definition` はコンポーネント（`.tsx`）のみが対象で、Hook（`.ts`、`use` プレフィックス）を検証できない |
| `project-rules/no-props-object-param` | Props は分割代入で受け取る（`props: XProps` 禁止） | `*Props` で終わる型注釈を持つ単一引数を検出する組み込みルールがない |

判断基準の詳細は `.kiro/steering/frontend-rules.md` の「カスタム oxlint ルール vs 組み込みルール」を参照。

## Consequences

- 新しいプロジェクト固有制約が必要になった場合、まず oxlint の組み込みルールで代替できないか確認する
  （`https://oxc.rs/docs/guide/usage/linter/rules/` を参照）
- 代替できない場合のみ `project-rules.js` にルールを追加する
- ディレクトリ名を変更する場合は `vite.config.ts` の `jsPlugins` パスと `ignorePatterns` を同時に更新する
