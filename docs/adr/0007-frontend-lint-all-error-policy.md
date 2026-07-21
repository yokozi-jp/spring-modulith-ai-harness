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

## Consequences

- 新しいルールが oxlint に追加された場合、カテゴリ設定により自動的に error になる。問題があれば個別に off にする
- off にするルールを追加する場合は、このADRの表に理由を追記する
- AI がルールに違反するコードを生成した場合、ステアリングを修正して対応する（ルールを緩めるのは最終手段）
