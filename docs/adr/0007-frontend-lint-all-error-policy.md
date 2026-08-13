# フロントエンド Lint 方針: 全カテゴリ error、個別に緩和

oxlint の全カテゴリ（correctness, suspicious, pedantic, perf, style, restriction）を error に設定し、AI 駆動開発で問題が出るルールだけ個別に off にする。「デフォルト厳格、必要に応じて緩和」の方針。

## Considered Options

| 選択肢 | 不採用理由 |
|---|---|
| correctness/suspicious のみ error、他は warn | AI が warn を無視してコードを生成する。warn は事実上無意味 |
| restriction を off のまま | restriction カテゴリには `no-console`, `eqeqeq` 等の有用なルールが多数含まれる。カテゴリごと off にすると個別有効化の管理コストが高い |
| 全ルール error、例外なし | React パターンと非互換なルールが存在し、正しいコードがエラーになる |

## off にしたルールと理由

| ルール | 理由 |
|---|---|
| `unicorn/no-null` | React で `null` 返却（条件付きレンダリング）が必須パターン |
| `unicorn/prevent-abbreviations` | `props`, `ref`, `fn`, `e` 等の React 標準的な略語を禁止してしまう |
| `unicorn/no-negated-condition` | `if (!x)` を禁止するが、可読性が下がるケースがある |
| `unicorn/no-keyword-prefix` | `newUser`, `className` 等を禁止してしまう |
| `unicorn/string-content` | 設定なしでは機能しない |
| `unicorn/no-useless-undefined` | React の明示的な `useState<T>(undefined)` を禁止してしまう |
| `max-lines` / `max-lines-per-function` / `max-params` | 数値制限は AI に不向き。コンテキストで判断すべき |
| `id-length` | `i`, `e`, `x` 等の慣用的な1文字変数を禁止してしまう |
| `no-undefined` | TypeScript 環境では `undefined` は安全。代替（`void 0`, `typeof`）の方が読みにくい |
| `no-magic-numbers` / `typescript/no-magic-numbers` | `0`, `1`, `100` まで定数化を強制するのは冗長。AI 生成コードが不必要に肥大化する |
| `no-ternary` | React JSX 内の条件付きレンダリング（`{x ? <A/> : <B/>}`）が書けなくなる |
| `no-plusplus` | `i++` を `i += 1` に強制する実益がない |
| `sort-keys` / `sort-imports` | oxfmt が自動ソートするため Lint で強制する必要がない |
| `react/no-set-state` | クラスコンポーネント用ルール。Hooks 時代には無関係 |
| `oxc/no-optional-chaining` | `?.` は TypeScript の標準パターン |
| `jsx-a11y/no-autofocus` | 業務アプリではフォームの autofocus が必要な場面がある |
| `func-style` | 関数宣言（`function X() {}`）を使う本プロジェクトの方針と矛盾（アロー関数式を強制してしまう） |
| `import/no-named-export` | named export 強制方針（`import/no-default-export: error`）と矛盾する汎用ルール |
| `import/prefer-default-export` | 同上。単一 export 時に default export を推奨するが、本プロジェクトは default export 禁止方針 |
| `typescript/explicit-function-return-type` | Google TS Style Guide も「戻り値型注釈の要否は著者判断に委ねる」と明記しており、一般的な TS 規約ではない |
| `typescript/explicit-module-boundary-types` | 同上の理由 |
| `typescript/prefer-readonly-parameter-types` | `React.ComponentProps` 等の外部型に readonly を強制すると大量の書き換えが必要になり過剰 |
| `import/no-namespace` | Shadcn/ui 生成コードの標準パターン（`import * as React from "react"`）と衝突。`src/components/ui/**` は ignorePatterns で除外済みだが、汎用ルールとして off |
| `import/group-exports` | 1関数1exportの慣習を保つため、複数 named export の統合を強制しない |
| `import/exports-last` | TanStack Router の `export const Route = createFileRoute(...)` をファイル先頭に書く規約と矛盾 |
| `max-statements` | `max-lines-per-function` と同様の理由で数値制限は AI に不向き |
| `capitalized-comments` | 日本語コメントに英語の大文字開始規則を適用するのは不適切 |
| `no-negated-condition` | `unicorn/no-negated-condition` と同様の理由 |
| `init-declarations` | 条件分岐で後から初期化する変数宣言パターン（`let x: T; if (...) { x = ... }`）を許容 |
| `prefer-named-capture-group` / `require-unicode-regexp` / `typescript/prefer-regexp-exec` | シンプルな正規表現（Cookie 解析等）に過剰な厳格化を強制しない |
| `import/no-unassigned-import` | CSS 等のサイドエフェクト import（`import "@/styles/globals.css"`）を許容。Google TS Style Guide も明示的に許容 |
| `typescript/no-unsafe-type-assertion` | Orval 生成コードとの互換性のための意図的な型アサーション（`api-client.ts` の `as T`）に必要 |
| `react/react-in-jsx-scope` | React 17+ の新 JSX 変換では `import React` が不要（現代の標準） |
| `react/jsx-max-depth` | JSX ネスト深さ制限は現実的な React コンポーネントに対して過剰 |
| `react/jsx-filename-extension` | 本プロジェクトは `.tsx` 拡張子を使用する規約（`.jsx` ではない） |
| `react/jsx-no-literals` | 日本語 UI テキストを JSX 内に直接書くのは正当なパターン（i18n 未導入の現状） |
| `react/forbid-component-props` | `className` を Shadcn/ui コンポーネントに渡すのは標準パターン |
| `react/jsx-handler-names` | ネイティブ HTML 要素の `onClick` はハンドラ名規約（`handle<Action>`）の対象外 |
| `oxc/no-rest-spread-properties` | rest/spread 構文（Props 分割代入等）は本プロジェクトで標準的に使用する ES2018 機能 |
| `oxc/no-async-await` | `async`/`await` は標準的に使用（禁止は非現実的） |
| `react/only-export-components` | TanStack Router のファイルベースルーティング規約（`export const Route` とコンポーネントを同一ファイルに書く）と矛盾 |
| `unicorn/relative-url-style` | `new URL("./src", import.meta.url)` の `./` は相対パスであることを明示するため必要 |

### オプションで解決したルール

| ルール | 設定 | 理由 |
|---|---|---|
| `no-duplicate-imports` | `["error", { "allowSeparateTypeImports": true }]` | `import { X } from "m"` と `import type { Y } from "m"` の分離記法（`consistent-type-imports` 方針）を許容しつつ、それ以外の重複 import はエラーにする |

### カテゴリ丸ごと緩和ではなく個別 off で対応する理由

`restriction`/`style` カテゴリには相互に矛盾するルールが混在している（例: `import/no-named-export` と `import/prefer-default-export` が同じ `style` カテゴリに存在し、両方を `error` にすると常に片方が違反になる）。矛盾するルールを `warn` に落として様子を見るのではなく、矛盾の原因を特定して該当ルールのみ `off` にする。カテゴリ一括の `warn` 化は「AI が warn を無視する」という本 ADR の前提と矛盾するため採用しない。

## Consequences

- 新しいルールが oxlint に追加された場合、カテゴリ設定により自動的に error になる。問題があれば個別に off にする
- off にするルールを追加する場合は、このADRの表に理由を追記する
- AI がルールに違反するコードを生成した場合、ステアリングを修正して対応する（ルールを緩めるのは最終手段）
