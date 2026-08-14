# フロントエンド Lint 設定の出典と参考リポジトリ照合

`frontend/vite.config.ts` の oxlint 設定を検討する際に参考にした外部リポジトリ・スタイルガイドと、実際に採用・見送りした設定の記録。

## 組み込みルールへの移行経緯

- `no-arrow-function-component`（自作カスタムルール） → `react/function-component-definition`（oxlint 組み込み、v1.75.0 以降）に移行した。Airbnb React style guide も同じ方針（`prefer normal functions over arrow functions` for named components）であることを確認済み

## Google TypeScript Style Guide との照合

`eslint-config-airbnb` や Google の ESLint 設定パッケージをそのまま import して一括導入することはしない。理由:

- ESLint 前提のパッケージは oxlint にそのまま適用できない。oxlint は ESLint 互換の Rust 実装だが、ルール名・オプション形式が独自であり、`eslint-config-airbnb` 等の設定オブジェクトを読み込む仕組みがない
- 本プロジェクトの oxlint 設定は主要カテゴリを全て `error` にしており、Airbnb/Google が個別に選定したルール群の大部分を既に包含している（`no-var`, `eqeqeq`, `prefer-const` 等は oxlint の `style`/`correctness` カテゴリで標準カバー）

oxlint はカテゴリ（`style`/`pedantic`/`restriction` 等）を一括 `error` にしても、ルール定義側で `default: false` のルールは自動有効化されない。Google TypeScript Style Guide との照合で、以下のルールを個別に有効化した:

| ルール | 内容 | 採用理由 |
|---|---|---|
| `typescript/ban-ts-comment` | `@ts-ignore` / `@ts-expect-error` を制限 | 型エラーの誤魔化しを防ぐ。NullAway/PMD の厳格方針と一致 |
| `typescript/consistent-type-definitions` | オブジェクト型は `interface` に統一（`type` エイリアス禁止） | 「書き方を1通りにする」という steering の一貫方針に合致 |
| `typescript/array-type` | `T[]` 記法に統一（`Array<T>` 禁止） | 同上 |
| `typescript/no-empty-object-type` | `{}` 型の使用を制限 | `unknown` / `Record<string, T>` 等の明示的な型を促す |

## Airbnb JavaScript Style Guide との照合

以下2つの見落としを発見・採用した:

| ルール | 内容 | 採用理由 |
|---|---|---|
| `radix` | `parseInt()` に基数（第2引数）を明示 | 省略時は先頭が `0x`/`0X` かどうかで解釈が変わり、バグの元になる。Airbnb・Google 両方が明示的に要求 |
| `no-new-wrappers` | `new String()` / `new Boolean()` / `new Number()` を禁止 | `new Boolean(false)` が truthy になる等、直感に反する挙動を防ぐ |

Airbnb のフォーマット系ルール（`quotes`, `semi`, `comma-dangle`, `space-before-blocks` 等）は oxlint（lint）の対象外であり、oxfmt（フォーマッター）側で担当するため対応不要。`camelcase`, `no-mixed-operators` 等は oxlint に対応ルールが存在せず見送り。

## 参考リポジトリとの照合

React + TypeScript + Vitest + Tailwind CSS という同種の技術スタックで oxlint を運用している OSS の設定と照合し、抜け漏れを確認した。

- [`sapegin/oxlint-config-raccoon`](https://github.com/sapegin/oxlint-config-raccoon) — 最も構成が近い。`base`/`typescript`/`typescript-react`/`typescript-react-tailwind` の階層プリセット（[`base.ts`](https://github.com/sapegin/oxlint-config-raccoon/blob/main/src/base.ts), [`typescript-react.ts`](https://github.com/sapegin/oxlint-config-raccoon/blob/main/src/typescript-react.ts) を参照）
- [`expo/oxlint-config-universe`](https://github.com/expo/oxlint-config-universe) — README に「oxlint でカバーできない eslint-config-universe のルール一覧」が明記されており、oxlint 未実装ルールの把握に有用
- [`schoero/eslint-plugin-better-tailwindcss`](https://github.com/schoero/eslint-plugin-better-tailwindcss) — oxlint 対応の公式 Tailwind CSS lint プラグイン。本プロジェクトの `better-tailwindcss/*` ルールの出典

この照合で採用・変更した設定:

| 設定 | 出典 | 内容 |
|---|---|---|
| `no-void: ["error", { allowAsStatement: true }]` | `oxlint-config-raccoon/base.ts` | `restriction` カテゴリ一括設定で暗黙有効化されていた `no-void`（`allowAsStatement: false`）が `void invalidateQueries(...)` 等の frontend-data-patterns.md パターンと矛盾するため明示修正 |
| `no-unreachable-loop: "error"` | `oxlint-config-raccoon/base.ts` | カテゴリ一括設定でも `default: false` のため個別追加 |
| `promise` プラグイン全体（`plugins` に追加） | `oxlint-config-raccoon/base.ts` | `promise/catch-or-return`, `promise/prefer-await-to-then`, `promise/no-nesting` 等、`.then()/.catch()` チェーンを検出し async/await への書き換えを促す |
| `promise/avoid-new: "off"` | 本プロジェクト独自の判断（raccoon には存在しない設定） | `await new Promise((resolve) => setTimeout(resolve, ms))` のような正当な Promise 化パターンまで一律禁止するため off |

`expo/oxlint-config-universe` が指摘する `no-dupe-args` / `no-octal` / `react/jsx-no-bind` 等は、本プロジェクトの oxlint バージョンでも同様に未実装（oxlint 自体の制約であり対応不可）。

`jsdoc` プラグインは見送った。本プロジェクトは「コンポーネント/Hook に JSDoc は不要」方針（`frontend-code-patterns.md`）のため導入価値が薄い。

上記以外の大半のルール（`react/forbid-dom-props`, `project-rules/no-button-inside-link` 等）は参考リポジトリの照合以前に、steering の既存規約に対応させる独自調査（Web 検索・自作カスタムルール）で導入したものであり、特定の外部リポジトリ由来ではない。

## `vercel-labs/agent-skills`（react-best-practices）は不採用

[`vercel-labs/agent-skills` の React Best Practices](https://github.com/vercel-labs/agent-skills/blob/main/skills/react-best-practices/SKILL.md)（Vercel 公式、パフォーマンス最適化ルール 70 件）を確認したが、本プロジェクトには採用しない。

- カテゴリの大半（Eliminating Waterfalls、Bundle Size Optimization、Server-Side Performance 等）は Next.js 固有（RSC・Server Actions・`next/dynamic`・`after()` 等）であり、本プロジェクトの技術スタック（Vite + TanStack Router、CSR）に適用対象がない
- Re-render Optimization カテゴリ（`rerender-memo`, `rerender-functional-setstate` 等の手動 `memo()`/`useMemo()`/`useCallback()` 最適化）は、ルール本文に「**React Compiler が有効な場合はこの最適化は不要**」と明記されている。本プロジェクトは `vite.config.ts` で `babel-plugin-react-compiler` を有効化済みのため、これらのルールを採用すると React Compiler の自動最適化と二重管理になる
- 残る JavaScript Performance カテゴリ（`js-set-map-lookups` 等の汎用最適化）は、現状 CRUD 中心の実装（大量データのループ処理等が発生していない）では適用箇所がなく、先に規約化する必要性がない

## Consequences

- 新しい lint ルールの導入を検討する際は、まず本ドキュメントに記載済みの参考リポジトリと照合し、重複調査を避ける
- 出典が明確な設定のみ本ドキュメントに記載する。steering ファイル（`.kiro/steering/frontend-dev-environment.md` 等）には AI の実装判断に直接関わる規約のみを置き、出典・経緯といったメタ情報は本 ADR に集約する
