# Frontend

React + TanStack Router + Tailwind CSS + Shadcn/ui のフロントエンド基盤。

## セットアップ

```bash
cd frontend
vp install
vp dev        # http://localhost:5173 で起動
```

## コマンド一覧

| コマンド              | 用途                                     |
| --------------------- | ---------------------------------------- |
| `vp install`          | 依存インストール                         |
| `vp dev`              | 開発サーバー起動                         |
| `vp check`            | Lint + 型チェック + フォーマットチェック |
| `vp check --fix`      | 自動修正                                 |
| `./scripts/verify.sh` | 全検証（vp check + カスタムチェック）    |
| `vp test`             | テスト実行                               |
| `vp build`            | 本番ビルド                               |

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

## ルール

- 詳細は `.kiro/steering/frontend-rules.md` を参照
- Lint 方針は `docs/adr/0007-frontend-lint-all-error-policy.md` を参照
- API クライアント生成方針は `docs/adr/0008-openapi-orval-codegen.md` を参照
- `oxlint-plugins/` ディレクトリの実体（oxlint の JS plugin）は `docs/adr/0009-oxlint-plugins-directory-naming.md` を参照
