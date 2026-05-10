# Frontend 開発ルール

本プロジェクトのフロントエンドコード変更時に従うルール。
すべての作業は `frontend/` ディレクトリを起点とする。

---

## 技術スタック

- React 19 + TypeScript (strict)
- TanStack Router（ファイルベースルーティング）
- Tailwind CSS v4 + Shadcn/ui
- Vite+（`vp` コマンドで操作）
- oxlint（Lint）+ oxfmt（フォーマッター）

---

## コマンド

```bash
cd frontend
vp install      # 依存インストール
vp dev          # 開発サーバー起動
vp check        # Lint + 型チェック + フォーマットチェック
vp test         # テスト実行
vp build        # 本番ビルド
```

- `npm`, `pnpm`, `yarn` を直接使わない。すべて `vp` 経由。
- `vitest`, `oxlint`, `oxfmt` を直接インストールしない。Vite+ に内蔵されている。

---

## ディレクトリ構成

```
frontend/src/
├── routes/              # ページ（TanStack Router が自動検出）
├── components/
│   ├── ui/             # Shadcn/ui コンポーネント（自動生成、編集しない）
│   └── features/       # 機能単位のコンポーネント
│       └── <feature>/
│           ├── <feature>.tsx       # 表示（プレゼンテーション）
│           └── use-<feature>.ts    # ロジック（Hook）
├── hooks/              # 汎用 Hooks（機能横断）
├── lib/                # ユーティリティ・API クライアント
├── types/              # 共有型定義
└── styles/
    └── globals.css     # Tailwind エントリポイント + デザイントークン
```

---

## ファイル命名規則

- **kebab-case** を使う（oxlint で強制）
- コンポーネント: `user-list.tsx`
- Hook: `use-user-list.ts`
- 型定義: `member.ts`
- ルートファイル: TanStack Router の規約に従う（`__root.tsx`, `_layout.tsx`, `$param.tsx`, `index.tsx`）

---

## ページファイル（routes/）のルール

ページファイルは**薄く保つ**。以下のみ記述する:

- ルート定義（`createFileRoute`）
- loader（データ取得の宣言）
- features コンポーネントの組み合わせ

以下は書かない:

- ビジネスロジック → `components/features/<feature>/use-<feature>.ts`
- UI 部品の実装 → `components/features/` or `components/ui/`
- API 呼び出しの詳細 → `lib/`

---

## Hooks パターン（UI とロジックの分離）

機能コンポーネントは必ず表示と Hook を分離する:

```
components/features/member-list/
├── member-list.tsx      # 表示のみ。Hook から受け取ったデータを描画する
└── use-member-list.ts   # ロジック。データ取得・状態管理・イベント処理
```

- Hook は `use-` プレフィックス必須
- Hook は表示に依存しない（テスト可能）
- 表示コンポーネントは Hook を呼ぶだけ（Storybook 対応しやすい）

---

## import ルール

- パスエイリアス `@/` を使う（`../../` のような相対パスは禁止）
- 型の import は `import type { X }` を使う（oxlint で強制）
- default export 禁止（ルートファイルと設定ファイルを除く）
- 循環 import 禁止（oxlint で強制）

---

## スタイリング

- Tailwind CSS のユーティリティクラスのみ使用する
- インラインスタイル（`style={}`）禁止
- CSS ファイルの追加禁止（`globals.css` のみ）
- クラスの結合には `cn()` を使う（`@/lib/utils`）
- Shadcn/ui コンポーネントは `vp dlx shadcn@latest add <component>` で追加する

---

## 禁止事項（oxlint で強制）

- `any` 型の使用
- `console.log`（デバッグ用途でも残さない）
- 配列の index を React の key に使用
- `dangerouslySetInnerHTML`
- `==` / `!=`（`===` / `!==` を使う）
- `var`（`const` / `let` を使う）

---

## Shadcn/ui コンポーネントの追加方法

```bash
cd frontend
vp dlx shadcn@latest add button    # 例: Button コンポーネント追加
```

- `src/components/ui/` に生成される
- 生成されたファイルは原則編集しない
- カスタマイズが必要な場合は `components/features/` にラッパーを作る

---

## 変更後の確認

コード変更後は必ず以下を実行する:

```bash
./scripts/verify.sh    # Lint + 型チェック + フォーマット + カスタムチェック全実行
```

エラーが出た場合は修正してからコミットする。
自動修正可能なものは `./scripts/verify.sh --fix` で修正できる。
