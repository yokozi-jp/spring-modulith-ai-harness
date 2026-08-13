# `eslint-plugins/` ディレクトリと `project-rules.js`: 実体は oxlint の JS プラグイン

`frontend/eslint-plugins/project-rules.js` はディレクトリ名・ファイル形式が ESLint プラグインと同じ見た目をしているが、
**実行されるのは ESLint ではなく oxlint**。oxlint v1.x の JS plugin 機構（`jsPlugins` 設定）で読み込まれる、
プロジェクト固有のカスタムルール集である。

## 誤解しやすい点

- 本プロジェクトは ESLint を導入していない。Lint は oxlint（Rust 実装、`vp lint` 経由）が全て担う
- しかし `project-rules.js` は ESLint のプラグイン API（`meta`, `create(context)`, `context.report()` 等）と
  ほぼ同じ形式で書かれている
- これは oxlint が ESLint 互換の JS plugin API をサポートしているためであり、ESLint 本体への依存ではない
- ディレクトリ名が `eslint-plugins/` になっているのは、この API 互換性に由来する命名であり、
  「ESLint の設定ファイルが別に存在する」ことを意味しない

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
- `eslint-plugins/` という名称は誤解を招くが、oxlint の JS plugin API が ESLint 互換であるため
  変更していない。将来的に oxlint が独自の命名規則を提示した場合は追従を検討する
