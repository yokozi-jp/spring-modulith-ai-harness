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

## バックエンド連携

- バックエンドは `http://localhost:18080` で稼働（8080 は Keycloak）
- `/api/v1/*` は Vite プロキシ経由でアクセス

### Vite プロキシ設定（vite.config.ts）

```typescript
server: {
  proxy: {
    "/api": {
      target: "http://localhost:18080",
      changeOrigin: true,
    },
  },
}
```

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
├── features/            # 機能単位
│   └── <feature>/
│       ├── components/  # 機能固有コンポーネント
│       ├── hooks/       # 機能固有 Hook
│       └── types/       # 機能固有型（必要な場合のみ）
├── components/
│   ├── layout/         # レイアウト（Header, Sidebar, Footer）
│   └── ui/             # Shadcn/ui コンポーネント（自動生成、編集禁止）
├── api/                # Orval 自動生成（編集禁止、npx orval で再生成）
├── hooks/              # 汎用 Hooks（機能横断）
├── lib/                # ユーティリティ・API クライアント
├── types/              # 共有型定義
└── styles/
    └── globals.css     # Tailwind エントリポイント + デザイントークン
```

共通コンポーネント（複数 feature で使うもの）は `components/` 直下に配置する。

---

## ファイル命名規則

- **kebab-case** を使う（oxlint で強制）
- コンポーネント: `user-list.tsx`
- Hook: `use-user-list.ts`
- 型定義: `member.ts`
- ルートファイル: TanStack Router の規約に従う（`__root.tsx`, `_layout.tsx`, `$param.tsx`, `index.tsx`）

### TanStack Router ルート命名規則

| パターン | ファイル名 | URL | 親ルート |
|----------|-----------|-----|----------|
| 基本 | `products.tsx` | `/products` | `__root` |
| ネスト（レイアウトなし） | `products_.new.tsx` | `/products/new` | `__root`（独立） |
| ネスト（レイアウトあり） | `products/new.tsx` | `/products/new` | `products.tsx` が layout |
| 動的パラメータ | `products_.$id.tsx` | `/products/:id` | `__root`（独立） |
| 動的パラメータの子 | `products_.$id.edit.tsx` | `/products/:id/edit` | `products_.$id.tsx`（子ルート） |
| 動的パラメータ後の独立 | `products_.$id_.edit.tsx` | `/products/:id/edit` | `__root`（独立） |

- ネストルートでレイアウトを共有しない場合は `_` を使う（例: `products_.new.tsx`）
- `createFileRoute` の引数はファイル名と一致させる（例: `createFileRoute("/products_/new")`）
- **動的パラメータの後に独立ルートを作る場合は `$id_` のように `_` を追加する**
  - `products_.$id.edit.tsx` → 詳細ページの子ルート（詳細ページに `<Outlet />` が必要）
  - `products_.$id_.edit.tsx` → 独立ルート（詳細ページと並列、`<Outlet />` 不要）
- CRUD 画面では編集・削除は詳細ページと**独立**させる（`$id_.edit.tsx` 形式を使う）

---

## ページファイル（routes/）のルール

ページファイルは**薄く保つ**。以下のみ記述する:

- ルート定義（`createFileRoute`）
- loader（データ取得の宣言）
- features コンポーネントの組み合わせ

以下は書かない:

- ビジネスロジック → `features/<feature>/hooks/`
- UI 部品の実装 → `features/<feature>/components/`
- API 呼び出しの詳細 → `features/<feature>/hooks/`

---

## import ルール

- パスエイリアス `@/` を使う（`../` のような親ディレクトリへの相対パスは禁止）
- 同一ディレクトリ内の `./` は許可（例: `./use-member-list`）
- 型の import は `import type { X }` を使う（oxlint で強制）
- default export 禁止（ルートファイルと設定ファイルを除く）
- barrel export（`index.ts` からの re-export）禁止
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
- カスタマイズが必要な場合は `components/` 直下または `features/` 内に作成する

---

## 変更後の確認

コード変更後は必ず以下を実行する:

```bash
./scripts/verify.sh    # Lint + 型チェック + フォーマット + カスタムチェック全実行
```

エラーが出た場合は修正してからコミットする。
自動修正可能なものは `./scripts/verify.sh --fix` で修正できる。

---

## チェックの線引き（oxlint vs shell）

コードパターンと配置ルールで担当を分離する:

| チェック対象 | oxlint | shell |
|-------------|--------|-------|
| コードの書き方（any禁止、console禁止等） | ✅ | ❌ |
| import/export パターン（apiClient 直接禁止、Hook 定義場所） | ✅ | ❌ |
| ファイル・ディレクトリ配置（use-*.ts の配置場所） | ❌ | ✅ |
| Git 状態（src/api/ や components/ui/ の変更検出） | ❌ | ✅ |

### oxlint カスタムルール（`eslint-plugins/project-rules.js`）

- `project-rules/no-direct-api-client`: features/*/hooks/ 内で apiClient を直接 import することを禁止
- `project-rules/hook-in-dedicated-file`: Hook 関数（`export function use...`）は use-*.ts ファイルでのみ定義可能
- `project-rules/no-arrow-function-component`: コンポーネントはアロー関数ではなく関数宣言で定義
- `project-rules/no-arrow-function-hook`: Hook はアロー関数ではなく関数宣言で定義
- `project-rules/no-props-object-param`: Props は分割代入で受け取る（`props: XProps` 禁止）

### shell スクリプト（`scripts/checks/`）

- `check-hook-location.sh`: use-*.ts ファイルが正しいディレクトリ（hooks/）にあるか
- `check-features-structure.sh`: features/ ディレクトリ構造の検証
- `check-ui-readonly.sh`: components/ui/ の変更を検出
- `api-readonly.sh`: src/api/ の変更を検出

---

## 改修時のルール

既存機能を変更する場合:

1. **既存コードを読む** — 変更対象のファイルと関連ファイルを確認
2. **既存パターンに合わせる** — 命名、構造、スタイルを統一
3. **影響範囲を確認** — import 元、テスト、関連コンポーネント
4. **テストを更新** — 変更に応じてテストも修正

新規作成時はステアリングに従い、改修時は既存コード + ステアリングの両方に従う。

---

## ライブラリドキュメントの参照（Context7）

TanStack Router、TanStack Query、Radix UI 等のライブラリについて不明点がある場合は、**Context7 CLI** でドキュメントを参照する。

```bash
# ライブラリを検索
npx ctx7@latest library "TanStack Router" "createFileRoute の使い方"

# ドキュメントを取得（library コマンドで取得した ID を使用）
npx ctx7@latest docs /tanstack/router "createFileRoute params"
```

以下のケースで Context7 を使う:
- TanStack Router のルート定義（`createFileRoute` の引数、params の取り方）
- TanStack Query のオプション（`queryKey`、`staleTime`、`gcTime`）
- Radix UI / Shadcn/ui のコンポーネント Props（`asChild`、`onOpenChange`）
- Tailwind CSS v4 の新しい構文

---

## テスト

- テストは `vp test` で実行する（Vitest 内蔵）
- テストファイルは `*.test.ts` / `*.test.tsx` で命名する
- テストは対象ファイルと同じディレクトリに配置する（例: `lib/utils.test.ts`）
- import は `vite-plus/test` から行う（`vitest` を直接インストールしない）

```typescript
import { describe, expect, it } from "vite-plus/test";
```

---

## Lint エラー修正ワークフロー（AI 向け）

Lint エラーが発生した場合:

1. エラーメッセージのルール名を確認する
2. `.kiro/steering/frontend-lint-fix-guide.md` を参照して修正方法を特定する
3. 修正を適用する
4. `./scripts/verify.sh` で再確認する

ステアリング `frontend-lint-fix-guide.md` に各ルールの具体的な修正方法が記載されている。
