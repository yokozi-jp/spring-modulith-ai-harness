# フロントエンド UI パターン規約

繰り返し登場する UI 状態の表現を決定的にする。

---

## UI コンポーネントの選択

1. **Shadcn/ui に存在するものは Shadcn/ui を使う**（未追加なら先に `vp dlx shadcn@latest add <name>` で追加）
2. **Shadcn/ui にないものは `src/components/` に自作する**
3. **生の HTML タグ + Tailwind でのスタイリングは避ける**

```bash
# 例: Table コンポーネントを追加
vp dlx shadcn@latest add table
```

- `src/components/ui/` に生成される
- 生成されたファイルは原則編集しない
- カスタマイズが必要な場合は `components/` 直下または `features/` 内に作成する

---

## Shadcn/ui コンポーネント使用時の必須ルール

Shadcn/ui 公式の Agent Skill（<https://github.com/shadcn-ui/ui/blob/main/skills/shadcn/SKILL.md>）から、本プロジェクトの慣習を補強・具体化するルールを採用した。

### スタイリング

- **Tailwind CSS のユーティリティクラスのみ使用する**。インラインスタイル（`style={}`）禁止（`react/forbid-dom-props` で検証）、CSS ファイルの追加禁止（`globals.css` のみ）
- **`className` はレイアウトのみに使う**。コンポーネントの色・タイポグラフィを上書きしない
- **`space-x-*` / `space-y-*` は使わない**。`flex` + `gap-*` を使う（縦積みは `flex flex-col gap-*`）
- **幅と高さが同じ場合は `size-*` を使う**（`size-10` であり `w-10 h-10` ではない）。`vp lint`（`better-tailwindcss/enforce-shorthand-classes`）が検出・自動修正する
- **`truncate` の省略形を使う**（`overflow-hidden text-ellipsis whitespace-nowrap` ではない）。同上、`vp lint --fix` で自動修正される
- **セマンティックカラーを使う**（`bg-primary`, `text-muted-foreground` 等）。`bg-blue-500` のような直接値は使わない
- **条件付きクラスは `cn()` を使う**（`@/lib/utils`）。手動のテンプレートリテラル三項演算子は書かない
- **オーバーレイ系コンポーネント（Dialog, Sheet, Popover 等）に手動で `z-index` を指定しない**（コンポーネントが自身のスタッキングを管理する）

```tsx
// ❌
<div className="space-y-4">...</div>
<div className="w-10 h-10">...</div>

// ✅
<div className="flex flex-col gap-4">...</div>
<div className="size-10">...</div>
```

**機械チェック**: `eslint-plugin-better-tailwindcss`（oxlint jsPlugin）を導入済み。`vp lint` で以下を検証する:

- `better-tailwindcss/enforce-shorthand-classes` — `w-10 h-10` → `size-10` 等の省略形強制（自動修正可）
- `better-tailwindcss/no-unknown-classes` — 存在しないクラス名（タイポ）の検出
- `better-tailwindcss/no-duplicate-classes` — 同一クラスの重複検出
- `better-tailwindcss/no-conflicting-classes` — `p-2 p-3` 等の矛盾するクラスの検出

**機械チェックの対象外**: `space-x-*`/`space-y-*` 禁止（`gap-*` 推奨）は目視確認とする。`enforce-shorthand-classes` は `w-10 h-10` → `size-10` のような同一プロパティの複数指定を単一のショートハンドクラスに統合する構文的な等価変換のみを扱う。`space-x`（子要素への `margin` 付与）と `gap`（Flexbox/Grid のネイティブなギャップ機構）は異なる CSS プロパティによる別のレイアウト手法であり、両者を機械的に等価とみなして自動変換することはできない。AI/レビュアーが目視で確認すること。

### 動的な数値に応じたクラスは事前定義配列から選択する

再帰コンポーネント（ツリー表示の階層深度 `depth` 等）やループ内で、変数の値に応じてクラス名を変える場合、テンプレートリテラルで動的にクラス名を生成しない。Tailwind CSS は静的解析でクラス名を検出してビルドするため、動的な文字列結合（`` `pl-${depth}` `` 等）は実行時に正しいクラス名になっても、ビルド時に該当クラスが CSS として生成されない、または `better-tailwindcss/no-unknown-classes` が誤検知するリスクがある。

```tsx
// ❌ テンプレートリテラルで動的にクラス名を生成する
function TreeRow({ depth }: { readonly depth: number }) {
  return <div className={`pl-${depth * 4}`}>...</div>;
}

// ✅ 事前定義した配列から選択する
const INDENT_CLASSES = ["pl-2", "pl-7", "pl-12", "pl-17", "pl-22", "pl-27"] as const;

function getIndentClass(depth: number): string {
  return INDENT_CLASSES[Math.min(depth, INDENT_CLASSES.length - 1)] ?? "pl-2";
}

function TreeRow({ depth }: { readonly depth: number }) {
  return <div className={getIndentClass(depth)}>...</div>;
}
```

配列の最大インデックスを超える depth に対しては `Math.min()` で頭打ちにし、配列外アクセス（`noUncheckedIndexedAccess` により `undefined` になる）に対してフォールバック値（`?? "pl-2"`）を用意する。

`cn()` と組み合わせる場合も同様に、事前定義済みのクラス文字列を渡す:

```tsx
<div className={cn("flex items-center gap-2 rounded-md py-1.5 hover:bg-accent", getIndentClass(depth))} />
```

### レスポンシブ対応（Tailwind CSS ブレークポイント）

#### メディアクエリファースト、コンテナクエリは必要な箇所のみ

- サイト全体・ページ単位のレイアウト変更（ナビゲーションの切り替え等）→ Tailwind のデフォルトブレークポイント（`sm`/`md`/`lg`/`xl`/`2xl`）を使う
- 個別コンポーネント単位のサイズ依存レイアウト → コンテナクエリ（`@container`, `@sm`, `@md` 等）を使う。多用すると性能・保守性に影響するため、必要な箇所のみに限定する

#### モバイルファースト

プレフィックスなしのクラスをモバイル用のデフォルトとし、大きい画面ではプレフィックス付きで上書きする。

```tsx
// ✅ プレフィックスなし = モバイル、sm以上で上書き
<div className="text-center sm:text-left" />

// ❌ sm をモバイルターゲットに使う（Tailwindの設計と逆）
<div className="sm:text-center" />
```

#### ブレークポイントの命名

デフォルトの `sm`/`md`/`lg`/`xl`/`2xl` を基本とし、独自ブレークポイントを追加する場合も命名は簡潔で直感的にする（`mobile`/`tablet`/`desktop` 等）。既存プレフィックスの意味を変える上書き（例: `sm` を極端に大きい値にする）は避ける。

### コンポーネント構成

- **Dialog / Sheet / Drawer には必ず `Title` を付ける**（`DialogTitle`, `SheetTitle`, `DrawerTitle`）。アクセシビリティ要件のため必須。視覚的に隠す場合は `className="sr-only"` を使う
- **Card は完全な構成で使う**（`CardHeader`/`CardTitle`/`CardDescription`/`CardContent`/`CardFooter`）。`CardContent` に全て詰め込まない
- **既存の Shadcn/ui コンポーネントを使う**。カスタムマークアップを書く前に対応するコンポーネントが存在しないか確認する
  - コールアウト表示 → `Alert`（自作の styled div は禁止）
  - 空状態 → `EmptyState`（本プロジェクト独自コンポーネント、下記「空状態」セクション参照）
  - 区切り線 → `Separator`（生の `<hr>` は禁止）
  - ローディングプレースホルダー → `Skeleton`（`animate-pulse` の自作 div は禁止）
  - ステータス表示 → `Badge`（styled span は禁止）
- **ダイアログ内のボタンは Shadcn/ui の `Button` を使う**。生の `<button>` タグは使わない（UI コンポーネントの選択の原則1と一致）

### Link と Button の組み合わせ

`<Link>` 内に `<Button>` をネストしない。HTML 仕様上、`<a>` 内に `<button>` は配置できず、クリックイベントが正しく動作しない。

```tsx
// ❌ 動かない（HTML 仕様違反）
<Link to={`/orders/${id}/edit`}>
  <Button>編集</Button>
</Link>

// ✅ asChild を使う（Button が Link の子要素としてレンダリング）
<Button asChild>
  <Link to={`/orders/${id}/edit`}>編集</Link>
</Button>
```

**機械チェック**: `project-rules/no-button-inside-link`（oxlintカスタムルール）が `<Link>` 内への `<Button>` のネストを検出する。

- `asChild` は Radix UI / Shadcn/ui の prop で、子要素にスタイルと振る舞いを委譲する
- `Button` のスタイルが適用された `<a>` タグがレンダリングされる

### カスタマイズ時のアクセシビリティ保持

Shadcn/ui コンポーネントは Radix UI プリミティブ上に構築されており、キーボード操作・スクリーンリーダー対応・フォーカス管理が標準で組み込まれている。`src/components/` にラッパーコンポーネントを作成する等でカスタマイズする際は、これらを損なわないこと:

- `aria-*` 属性を保持する（独自の `className` で上書きしない）
- キーボードイベントハンドラを保持する（独自の `onClick` 等で置き換えない）
- フォーカスインジケータを保持する（`focus-visible` のスタイルを消さない）

出典: google-labs-code/stitch-skills の shadcn-ui skill（<https://github.com/google-labs-code/stitch-skills>）の Accessibility セクション

---

## 共通 UI コンポーネントの作成

**原則: 同じ構造の UI パターンを 2 箇所以上に書く前に、`src/components/` に共通コンポーネントを作成する。**

判断基準:

- JSX 構造が同じ（props の値だけ違う）→ 共通化する
- 構造が異なる → feature 固有で OK

```text
src/components/
├── error-message.tsx    # エラー表示
├── empty-state.tsx      # 空状態表示
├── confirm-dialog.tsx   # 削除確認等のダイアログ
├── list-skeleton.tsx    # 一覧ローディング
└── layout/              # レイアウト
```

初回の feature 作成時に必要な共通コンポーネントがなければ作成する。
2 つ目の feature で同じパターンが必要になったら、先に共通化してから使う。

---

## レイアウト構成

### 基本構造

```
┌─────────────────────────────────────┐
│ Header（ロゴ、ユーザー名、ログアウト）│
├──────────┬──────────────────────────┤
│ Sidebar  │ Main Content             │
│ (メニュー)│ <Outlet />               │
├──────────┴──────────────────────────┤
│ Footer（省略可）                     │
└─────────────────────────────────────┘
```

### ファイル配置

```
src/components/layout/
├── app-layout.tsx       # 全体レイアウト（Header + Sidebar + Main）
├── header.tsx           # ヘッダー
├── sidebar.tsx          # サイドバーメニュー
└── footer.tsx           # フッター（必要な場合のみ）
```

### __root.tsx のパターン

```tsx
// src/routes/__root.tsx
import type { QueryClient } from "@tanstack/react-query";
import { Outlet, createRootRouteWithContext } from "@tanstack/react-router";
import { AppLayout } from "@/components/layout/app-layout";

interface RouterContext {
  readonly queryClient: QueryClient;
}

export const Route = createRootRouteWithContext<RouterContext>()({
  component: RootLayout,
});

function RootLayout() {
  return (
    <AppLayout>
      <Outlet />
    </AppLayout>
  );
}
```

### AppLayout コンポーネント

```tsx
// src/components/layout/app-layout.tsx
import type { ReactNode } from "react";
import { Header } from "@/components/layout/header";
import { Sidebar } from "@/components/layout/sidebar";

interface AppLayoutProps {
  readonly children: ReactNode;
}

export function AppLayout({ children }: AppLayoutProps) {
  return (
    <div className="flex min-h-screen flex-col">
      <Header />
      <div className="flex flex-1">
        <Sidebar />
        <main className="flex-1 p-6">{children}</main>
      </div>
    </div>
  );
}
```

### Header コンポーネント

```tsx
// src/components/layout/header.tsx
export function Header() {
  return (
    <header className="flex h-14 items-center justify-between border-b px-6">
      <div className="font-semibold">アプリ名</div>
      <div className="flex items-center gap-4">
        <span className="text-sm text-muted-foreground">ユーザー名</span>
        <a href="/logout" className="text-sm underline">ログアウト</a>
      </div>
    </header>
  );
}
```

### Sidebar コンポーネント

```tsx
// src/components/layout/sidebar.tsx
import { Link } from "@tanstack/react-router";

const MENU_ITEMS = [
  { to: "/", label: "ホーム" },
  { to: "/products", label: "商品管理" },
  { to: "/categories", label: "カテゴリ管理" },
] as const;

export function Sidebar() {
  return (
    <aside className="w-56 border-r bg-muted/40 p-4">
      <nav className="space-y-1">
        {MENU_ITEMS.map((item) => (
          <Link
            key={item.to}
            to={item.to}
            className="block rounded-md px-3 py-2 text-sm hover:bg-muted"
            activeProps={{ className: "bg-muted font-medium" }}
          >
            {item.label}
          </Link>
        ))}
      </nav>
    </aside>
  );
}
```

### 認証情報の取得（将来）

認証状態（ユーザー名等）が必要になったら:

1. backend に `/api/v1/me` エンドポイントを追加
2. `useCurrentUser` Hook を作成
3. Header でユーザー名を表示

**先に作らない**。必要になるまで実装しない。

---

## ローディング状態

### Skeleton を使う（Spinner は使わない）

```tsx
// ✅ Skeleton（灰色ブロックで「もうすぐ表示される」感を出す）
function OrderListSkeleton() {
  return (
    <output className="flex flex-col gap-4">
      {Array.from({ length: 5 }).map((_, i) => (
        <div key={`skeleton-${String(i)}`} className="h-16 animate-pulse rounded-md bg-muted" />
      ))}
    </output>
  );
}

// ❌ Spinner
return <Spinner />;
```

Skeleton はコンポーネントと同じディレクトリに `<name>-skeleton.tsx` で配置する。

### ルート要素は `role="status"` ではなく `<output>` タグを使う

ローディング中であることを支援技術に伝えるため、Skeleton のルート要素には ARIA live region が必要。これを `<div role="status">` で表現すると `jsx-a11y/prefer-tag-over-role` に違反する（`role="status"` に対応するネイティブ HTML 要素 `<output>` が存在するため）。

```tsx
// ❌ role="status" を div に付与（jsx-a11y/prefer-tag-over-role 違反）
function OrderListSkeleton() {
  return (
    <div role="status" className="flex flex-col gap-4">
      ...
    </div>
  );
}

// ✅ <output> タグを使う（暗黙的に role="status" を持つ）
function OrderListSkeleton() {
  return (
    <output className="flex flex-col gap-4">
      ...
    </output>
  );
}
```

`<output>` はブロック要素ではなくインライン要素だが、`className` で `flex`/`flex-col` 等を指定すれば見た目上は `div` と同様に振る舞う。テストで存在確認する場合は `screen.getByRole("status")` で取得できる（`<output>` が暗黙的に `role="status"` を持つため）。

---

## エラー状態

### ErrorMessage コンポーネントを使う

```tsx
// src/components/error-message.tsx
import { Button } from "@/components/ui/button";

interface ErrorMessageProps {
  readonly error: Error | null;
  readonly onRetry?: () => void;
}

export function ErrorMessage({ error, onRetry }: ErrorMessageProps) {
  if (error === null) {
    return null;
  }

  return (
    <div role="alert" className="rounded-md border border-destructive/50 p-4">
      <p className="text-sm text-destructive">{error.message}</p>
      {onRetry !== undefined && (
        <Button variant="link" size="sm" onClick={onRetry} className="mt-2 h-auto p-0">
          再試行
        </Button>
      )}
    </div>
  );
}
```

### 使い方

```tsx
export function OrderList({ orders, isLoading, error, refetch }: OrderListProps) {
  if (error) {
    return <ErrorMessage error={error} onRetry={refetch} />;
  }
  // ...
}
```

---

## 空状態

### EmptyState コンポーネントを使う

```tsx
// src/components/empty-state.tsx
import { Button } from "@/components/ui/button";

interface EmptyStateProps {
  readonly message: string;
  readonly action?: {
    readonly label: string;
    readonly onClick: () => void;
  };
}

export function EmptyState({ message, action }: EmptyStateProps) {
  return (
    <div className="flex flex-col items-center justify-center py-12 text-center">
      <p className="text-muted-foreground">{message}</p>
      {action !== undefined && (
        <Button variant="link" onClick={action.onClick} className="mt-4">
          {action.label}
        </Button>
      )}
    </div>
  );
}
```

---

## 一覧表示の定型パターン

```tsx
export function OrderList({ orders, isLoading, error, refetch }: OrderListProps) {
  if (isLoading) {
    return <OrderListSkeleton />;
  }

  if (error) {
    return <ErrorMessage error={error} onRetry={refetch} />;
  }

  if (orders.length === 0) {
    return <EmptyState message="注文がありません" />;
  }

  return (
    <ul className="flex flex-col gap-2">
      {orders.map((order) => (
        <li key={order.id}>
          <OrderCard order={order} />
        </li>
      ))}
    </ul>
  );
}
```

順序は常に: **Loading → Error → Empty → Content**

---

## フォーム

### HTML ネイティブ + Shadcn/ui を使う（フォームライブラリは使わない）

```tsx
import type { SubmitEvent } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

interface OrderFormProps {
  readonly onSubmit: (data: CreateOrderInput) => void;
  readonly isSubmitting: boolean;
}

export function OrderForm({ onSubmit, isSubmitting }: OrderFormProps) {
  const [name, setName] = useState("");

  function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault();
    onSubmit({ name });
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-4">
      <div className="flex flex-col gap-1">
        <Label htmlFor="name">名前</Label>
        <Input
          id="name"
          type="text"
          value={name}
          onChange={(e) => { setName(e.target.value); }}
          required
        />
      </div>
      <Button type="submit" disabled={isSubmitting}>
        {isSubmitting ? "送信中..." : "作成"}
      </Button>
    </form>
  );
}
```

### フォームのルール

- `<form>` タグを使う（`onSubmit` で送信処理）
- `event.preventDefault()` は `handleSubmit` 内で呼ぶ
- ボタンには `type="submit"` を付ける
- **enum / FK（外部キー）フィールドは Shadcn/ui の `Select` で選択させる**（手入力させない、生の `<select>` は使わない）
- 送信中は `disabled` で二重送信防止
- バリデーションは HTML 属性（`required`, `pattern`, `min` 等）を優先
- 複雑なバリデーションが必要になったら `zod` を導入する（先に入れない）

### ボタンを disabled にしてバリデーションエラーを表現しない

**アクセシビリティの観点**: `disabled` 属性が付いたボタンはキーボード操作のタブフォーカスでアクセスできず、スクリーンリーダーユーザーが位置を把握できない問題がある。

```tsx
// ❌ バリデーションエラー時にボタンを無効化
<Button disabled={!isValid}>送信</Button>

// ✅ 常に有効化し、クリック時にバリデーション結果を表示
<Button onClick={handleSubmit}>送信</Button>
```

**注意**: これは上記「送信中は `disabled` で二重送信防止」とは別の論点。`isSubmitting` による disabled は二重送信防止が目的であり、本項目と矛盾しない。バリデーションエラーの表現は `ErrorMessage` コンポーネント等で行う。

### Instant 型フィールドのフォーム入出力変換

バックエンドが `java.time.Instant` を使うフィールド（`validFrom`, `validTo`, `createdAt` 等）は、API 上で ISO 8601 形式（`2026-08-22T00:00:00Z`）としてやり取りされる。一方 HTML の `<input type="date">` は `YYYY-MM-DD` のみを返す。フォームの送信・表示時に以下の変換を行う。

```tsx
// 送信時: date入力値（YYYY-MM-DD）→ Instant形式（ISO 8601）
function toInstant(dateString: string): string {
  return `${dateString}T00:00:00Z`;
}

// 表示・初期値設定時: Instant形式 → date入力値
function toDateString(instant: string): string {
  return instant.slice(0, 10);
}
```

**フォーム送信例**:
```tsx
function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
  event.preventDefault();
  onSubmit({
    amount,
    validFrom: toInstant(validFrom),          // "2026-08-22" → "2026-08-22T00:00:00Z"
    ...(validTo.length > 0 && { validTo: toInstant(validTo) }),
  });
}
```

**編集フォームの初期値設定例**:
```tsx
const [validFrom, setValidFrom] = useState(
  initialValues?.validFrom !== undefined ? toDateString(initialValues.validFrom) : "",
);
```

**詳細表示例**:
```tsx
<dd className="text-sm">{toDateString(pricing.validFrom ?? "")}</dd>
```

注意点:
- `T00:00:00Z` は UTC 0時を意味する。タイムゾーン変換が必要な場合は `timezone-rules.md` の方針に従い、フロントエンドで変換する
- `validTo` のような optional フィールドは、空文字の場合にプロパティ自体を省略する（`exactOptionalPropertyTypes` 対応、`frontend-lint-fix-guide.md` 参照）
- これらの変換関数は各フォームコンポーネント内のローカル関数として定義してよい（共通化は 2 箇所以上で使う場合に `src/lib/` に切り出す）

---

## モーダル / ダイアログ

### Shadcn/ui の Dialog を使う

```tsx
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";

interface ConfirmDialogProps {
  readonly isOpen: boolean;
  readonly onClose: () => void;
  readonly onConfirm: () => void;
  readonly title: string;
  readonly message: string;
}

export function ConfirmDialog({ isOpen, onClose, onConfirm, title, message }: ConfirmDialogProps) {
  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
        </DialogHeader>
        <p className="text-sm text-muted-foreground">{message}</p>
        <div className="flex justify-end gap-2">
          <Button type="button" variant="outline" onClick={onClose}>キャンセル</Button>
          <Button type="button" variant="destructive" onClick={onConfirm}>確認</Button>
        </div>
      </DialogContent>
    </Dialog>
  );
}
```

### モーダルの状態管理

```tsx
// 親コンポーネントで開閉を管理
const [isDialogOpen, setIsDialogOpen] = useState(false);

function handleDelete() {
  setIsDialogOpen(true);
}

function handleConfirmDelete() {
  deleteOrder(id);
  setIsDialogOpen(false);
}
```

---

## 共通コンポーネントの配置

| コンポーネント | 配置先 |
|---------------|--------|
| ErrorMessage | `src/components/error-message.tsx` |
| EmptyState | `src/components/empty-state.tsx` |
| Skeleton（機能固有） | `src/features/<feature>/components/<name>-skeleton.tsx` |
| Dialog（機能固有） | `src/features/<feature>/components/<name>-dialog.tsx` |
| Shadcn/ui | `src/components/ui/`（編集禁止） |
