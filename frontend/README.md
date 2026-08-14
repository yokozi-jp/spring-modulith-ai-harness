# Frontend

React + TanStack Router + Tailwind CSS + Shadcn/ui のフロントエンド基盤。

## 目次

- [セットアップ](#セットアップ)
- [ディレクトリ構成](#ディレクトリ構成)
- [API クライアント生成（Orval）](#api-クライアント生成orval)
- [コンポーネント追加](#コンポーネント追加)
- [スタイリングの仕組み](#スタイリングの仕組み)
- [コード品質の仕組み](#コード品質の仕組み)
- [ルール](#ルール)

## セットアップ

```bash
cd frontend
vp install
vp dev        # http://localhost:5173 で起動
```

| コマンド | 用途 |
| --- | --- |
| `vp install` | 依存インストール |
| `vp dev` | 開発サーバー起動 |
| `vp build` | 本番ビルド |

検証コマンド（`vp check` / `verify.sh` / `vp test`）は [コード品質の仕組み](#コード品質の仕組み) を参照。

## ディレクトリ構成

```text
src/
├── routes/              # ページ（ファイル構造 = URL構造）
├── features/            # 機能単位（components/, hooks/, types/）
├── components/
│   └── ui/             # Shadcn/ui（自動生成、編集禁止）
├── api/                 # Orval 自動生成（編集禁止）
├── hooks/              # 汎用 Hooks
├── lib/                # ユーティリティ
├── types/              # 共有型定義
└── styles/             # Tailwind CSS
```

## API クライアント生成（Orval）

backend の OpenAPI spec から TanStack Query 対応の API クライアントを自動生成する。手書き API は禁止。
採用理由は `docs/adr/0008-openapi-orval-codegen.md` を参照。

```bash
# 1. backend が起動しているか確認
docker ps | grep smah-backend

# 2. 起動していなければ起動
docker compose up -d

# 3. OpenAPI spec を取得（Basic認証: admin:admin）
curl -s -u admin:admin http://localhost:18080/v3/api-docs -o frontend/openapi.json

# 4. Orval でクライアント生成
cd frontend && npx orval
```

生成物は `src/api/` 配下（タグごとにディレクトリ分割）。**手動編集しない**（再生成で上書きされる）。
`src/features/*/hooks/` から生成された Hook をラップして使う。

## コンポーネント追加

```bash
vp dlx shadcn@latest add button     # → src/components/ui/ に生成
vp dlx shadcn@latest add dialog
```

## スタイリングの仕組み

Tailwind CSS v4 + Shadcn/ui を使用する。採用理由は `docs/adr/0005-tailwind-css-and-shadcn-ui.md` を参照。

### 設定ファイルが存在しない理由

Tailwind CSS v4 は `tailwind.config.ts` を必須としない CSS-first 設計になった。
本プロジェクトではデザイントークン（色・角丸等）を `src/styles/globals.css` の `@theme` ブロックに直接定義している。

```css
/* src/styles/globals.css */
@import "tailwindcss";

@theme {
  --color-primary: hsl(240 5.9% 10%);
  --color-background: hsl(0 0% 100%);
  --radius-md: 0.375rem;
  /* ... */
}
```

`@theme` で定義した CSS 変数は `bg-primary`, `text-background`, `rounded-md` 等のユーティリティクラスとして
自動的に使えるようになる。個別の `tailwind.config.ts` にテーマ設定を書く必要はない。

### クラスの適用フロー

```text
globals.css の @theme でトークン定義
  ↓
コンポーネントで className="bg-primary text-primary-foreground rounded-md" のように使用
  ↓
cn() でクラスを結合・重複解決（src/lib/utils.ts、clsx + tailwind-merge）
```

`cn()` は複数のクラス文字列を結合し、Tailwind のクラス競合（例: `px-2` と `px-4` の同時指定）を
`tailwind-merge` で解決する。条件付きクラスや Props 経由のクラス上書きには必ず `cn()` を使う。

```tsx
<div className={cn("rounded-md border p-4", isActive && "border-primary", className)} />
```

### Shadcn/ui コンポーネントの位置づけ

- `src/components/ui/` に生成されるのは「ソースコードがそのままプロジェクトにコピーされたコンポーネント」であり、
  npm パッケージとして import する外部ライブラリではない
- 内部で Radix UI（アクセシビリティ対応済みのヘッドレスコンポーネント）+ Tailwind のクラスで構成されている
- 生成後は自プロジェクトのコードとして扱われ、直接編集はしない（再生成で上書きされるため）。
  カスタマイズが必要な場合は `src/components/` 直下または `src/features/*/components/` にラッパーを作る

## コード品質の仕組み

フロントエンドのコードは、複数の仕組みが層になって品質を保っている。全体像は以下の通り。

### レイヤー構成

```text
┌─────────────────────────────────────────────┐
│ 1. 型システム（TypeScript strict）            │  ← 書いている最中
├─────────────────────────────────────────────┤
│ 2. oxlint（vp check に内包）                  │  ← ファイル単体の静的解析
├─────────────────────────────────────────────┤
│ 3. shell スクリプト（scripts/checks/）        │  ← ファイル間・Git状態の検証
├─────────────────────────────────────────────┤
│ 4. oxfmt（vp check に内包）                   │  ← フォーマット統一
├─────────────────────────────────────────────┤
│ 5. Vitest（vp test）                          │  ← 振る舞いの検証
├─────────────────────────────────────────────┤
│ 6. Git pre-commit フック（vp staged）         │  ← コミット前の強制
└─────────────────────────────────────────────┘
```

上位レイヤーほど早いフィードバック（型エラーはエディタ上で即時）、下位レイヤーほど強制力が強い（pre-commitはコミット自体をブロックする）。

### 2. oxlint と 3. shell の役割分担

判定に必要な情報の種類で担当が分かれる。

| 判定対象 | 担当 | 理由 |
|---|---|---|
| 1ファイルの構文・パターン（`any`禁止、関数宣言強制、Props分割代入等） | oxlint | AST解析で1ファイル内に閉じて判定できる |
| ファイル名とファイルの配置場所（`use-*.ts`が`hooks/`にあるか） | shell | ファイルシステム上の配置はAST解析の対象外 |
| ファイル間の対応関係（Hookファイルに対応するテストファイルの有無） | shell | 複数ファイルの存在確認はoxlintの管轄外 |
| Git追跡状態（`src/api/`や`components/ui/`が意図せず変更されていないか） | shell | oxlintは1ファイルの中身しか見ず、Git状態は判定できない |

`oxlint-plugins/project-rules.js` にカスタムルールを追加する前に、oxlint組み込みルールで同等の検証ができないか必ず確認する（`frontend-rules.md`の「カスタム oxlint ルール vs 組み込みルール」参照）。

### 検証コマンドの使い分けと自動実行の実態

| コマンド | 実行内容 | 使う場面 |
|---|---|---|
| `vp check` | フォーマット + lint + 型チェック | エディタ保存時、開発中の高速フィードバック |
| `./scripts/verify.sh` | `vp check` + shell カスタムチェック5種 | **コード変更後、コミット前に必ず手動実行する** |
| `./scripts/verify.sh --fix` | 上記 + 自動修正 | 修正を一括反映したいとき |
| `vp test` | Vitest 実行 | Hook・ユーティリティ・コンポーネントの振る舞い確認 |

`vp check`だけでは以下の5つのshellチェックが素通りしてしまう:

- `check-hook-location.sh` — Hookファイルの配置場所
- `check-features-structure.sh` — `features/`ディレクトリ構造
- `check-ui-readonly.sh` — `components/ui/`（Shadcn/ui自動生成）への誤編集
- `api-readonly.sh` — `src/api/`（Orval自動生成）への誤編集
- `check-test-exists.sh` — Hook/utilに対応するテストファイルの有無

**`verify.sh`はどのタイミングでも自動実行されない。** 手動で実行することが前提のコマンドであり、以下の自動化ポイントはそれぞれ異なるサブセットしかカバーしていない。

| タイミング | 仕組み | 実行内容 | `verify.sh`との差分 |
|---|---|---|---|
| Kiro CLI: ファイル書き込み前 | `preToolUse(write)` フック（`frontend-write-guard.sh`） | `check-features-structure.sh --file`、`check-hook-location.sh --file`（**書き込み対象の1ファイルのみ検証**、違反時は書き込み自体をブロック） | `check-ui-readonly.sh`・`api-readonly.sh`・`check-test-exists.sh`は非対応 |
| Kiro CLI: ファイル書き込み後 | `postToolUse(write)` フック（`frontend-test-prompt.sh`, `orval-regen-prompt.sh`） | テスト未作成の通知、Orval再生成の提案（**通知のみ、ブロックしない**） | `check-test-exists.sh`と目的は同じだが独立実装、強制力なし |
| Kiro CLI: 応答終了時 | `stop` フック（`frontend-lint-check.sh`） | `vp check`のみ実行し、違反パターンを通知（**通知のみ、ブロックしない**） | shellチェック5種は一切実行されない |
| 人間のコミット時 | Git pre-commit（`core.hooksPath` → `vp staged`） | `vite.config.ts`の`staged`設定に従い、lint --fix + fmt + shellチェック5種を**全て実行**（違反時はコミットをブロック） | `verify.sh`と同等の網羅性 |

つまりAIがコード変更中に強制されるのは配置ルール2種のみで、`components/ui/`・`src/api/`の誤編集やテスト未作成は**AIの応答中には検出されず**、人間がコミットしようとした瞬間（pre-commit）で初めて弾かれる。そのため、AIはコード変更後に**必ず`./scripts/verify.sh`を自主的に実行する**必要がある（steeringで明示されているが、フックによる強制はない）。

### まとめ

- **書く瞬間**: TypeScript strict + エディタのoxlint連携で即時フィードバック
- **AIの書き込み時**: `preToolUse`フックが配置ルール2種のみを強制ブロック
- **変更後（手動）**: `./scripts/verify.sh`で全チェックを横断実行
- **コミット時**: pre-commitフックが`verify.sh`と同等のチェックを強制
- **意味的な正しさ**: `vp test`でHook・コンポーネントの振る舞いを検証

## ルール

- 詳細は `.kiro/steering/frontend-rules.md` を参照
- Lint 方針は `docs/adr/0007-frontend-lint-all-error-policy.md` を参照
- API クライアント生成方針は `docs/adr/0008-openapi-orval-codegen.md` を参照
- `oxlint-plugins/` ディレクトリの実体（oxlint の JS plugin）は `docs/adr/0009-oxlint-plugins-directory-naming.md` を参照
