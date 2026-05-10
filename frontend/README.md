# Frontend

React + TanStack Router + Tailwind CSS + Shadcn/ui のフロントエンド基盤。

## セットアップ

```bash
cd frontend
vp install
vp dev        # http://localhost:5173 で起動
```

## コマンド一覧

| コマンド | 用途 |
|---|---|
| `vp install` | 依存インストール |
| `vp dev` | 開発サーバー起動 |
| `vp check` | Lint + 型チェック + フォーマットチェック |
| `vp check --fix` | 自動修正 |
| `./scripts/verify.sh` | 全検証（vp check + カスタムチェック） |
| `vp test` | テスト実行 |
| `vp build` | 本番ビルド |

## ディレクトリ構成

```
src/
├── routes/              # ページ（ファイル構造 = URL構造）
├── components/
│   ├── ui/             # Shadcn/ui（自動生成、編集禁止）
│   └── features/       # 機能単位（表示 + Hook のペア）
├── hooks/              # 汎用 Hooks
├── lib/                # ユーティリティ
├── types/              # 共有型定義
└── styles/             # Tailwind CSS
```

## コンポーネント追加

```bash
vp dlx shadcn@latest add button
vp dlx shadcn@latest add dialog
```

## ルール

- 詳細は `.kiro/steering/frontend-rules.md` を参照
- Lint 方針は `docs/adr/0007-frontend-lint-all-error-policy.md` を参照
