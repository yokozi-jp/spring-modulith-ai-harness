# フロントエンド コードパターン規約

コードの書き方を決定的にするための規約。AI が同じ指示に対して常に同じ構造のコードを出力できるようにする。

---

## コンポーネント定義

### 関数宣言を使う（アロー関数禁止）

```tsx
// ✅
export function OrderList() {
  return <div />;
}

// ❌
export const OrderList = () => {
  return <div />;
};
```

### Props は interface で定義し、コンポーネントの直上に配置する

```tsx
// ✅
interface OrderListProps {
  readonly status: OrderStatus;
  readonly onSelect: (id: string) => void;
}

export function OrderList({ status, onSelect }: OrderListProps) {
  return <div />;
}

// ❌ inline 型
export function OrderList({ status }: { status: string }) { ... }

// ❌ type エイリアス
type OrderListProps = { ... };
```

### Props の命名規則

- Props interface 名: `<ComponentName>Props`
- コールバック Props: `on<Action>` (例: `onSelect`, `onDelete`, `onSubmit`)
- boolean Props: `is<State>` / `has<Thing>` (例: `isLoading`, `hasError`)
- readonly 修飾子を全フィールドに付ける

### 分割代入で Props を受け取る

```tsx
// ✅
export function OrderCard({ id, name, onSelect }: OrderCardProps) {

// ❌ props オブジェクトで受け取る
export function OrderCard(props: OrderCardProps) {
```

### 1 ファイル 1 コンポーネント

エクスポートするコンポーネントは 1 ファイルに 1 つだけ定義する。表示状態の分岐（Loading/Error/Empty/Content）や見た目の一部を切り出す際、実装の都合で同じファイル内に内部コンポーネントを追加で定義しない。

```tsx
// ❌ 同一ファイル内に2つ目のコンポーネントを定義する
export function OrderList({ orders, isLoading, error }: OrderListProps) {
  return (
    <div>
      <h1>注文一覧</h1>
      <OrderListContent orders={orders} isLoading={isLoading} error={error} />
    </div>
  );
}

// OrderList と同じファイル内に定義してしまっている
function OrderListContent({ orders, isLoading, error }: OrderListContentProps) {
  if (isLoading) return <OrderListSkeleton />;
  // ...
}
```

```tsx
// ✅ order-list-content.tsx に分離する
// order-list.tsx
import { OrderListContent } from "@/features/order/components/order-list-content";

export function OrderList({ orders, isLoading, error }: OrderListProps) {
  return (
    <div>
      <h1>注文一覧</h1>
      <OrderListContent orders={orders} isLoading={isLoading} error={error} />
    </div>
  );
}
```

理由:
- ファイル名とコンポーネント名の 1 対 1 対応が崩れると、grep やファイルジャンプでの発見性が下がる
- テストファイル（`<name>.test.tsx`）もコンポーネント単位で 1 対 1 に対応させる規約と一致させる
- 内部コンポーネントを後から他の画面で再利用したくなった際、ファイル分割のやり直しが発生する

「見出し + コンテンツ」のような単純な構成でも、コンテンツ側の表示分岐（Loading/Error/Empty/Content）が複数行にわたる場合は迷わず別ファイルに切り出す。`import/max-dependencies` 対策の事前分割（`frontend-dev-environment.md` 参照）と合わせて、実装前にファイル構成を決めておくとやり直しを防げる。

---

## Hook 定義

### Hooks パターン（UI とロジックの分離）

コンポーネントは Hook を呼び、Hook から受け取った値で条件分岐して表示する:

```
features/order/
├── components/
│   └── order-list.tsx       # Hook を呼び、返り値で表示を切り替える
└── hooks/
    └── use-order-list.ts    # ロジック（データ取得・状態管理・イベント処理）
```

- ロジックは Hook に書く
- コンポーネントは Hook を呼んで表示するだけ

### Hook も関数宣言を使う

```tsx
// ✅
export function useOrderList() {
  ...
}

// ❌
export const useOrderList = () => { ... };
```

### 返却値は明示的なオブジェクトで返す

```tsx
// ✅ オブジェクトで返す（プロパティ名が明確）
export function useOrderList() {
  const query = useQuery({ ... });

  return {
    orders: query.data ?? [],
    isLoading: query.isLoading,
    error: query.error,
    refetch: query.refetch,
  };
}

// ❌ 配列で返す（意味が不明瞭）
return [orders, isLoading];

// ❌ query オブジェクトをそのまま返す（内部実装の漏洩）
return query;
```

### Hook の責務分離

| Hook の種類 | 責務 | 命名 |
|------------|------|------|
| データ取得 | API 呼び出し + キャッシュ | `use<Resource>` (例: `useOrderList`) |
| ミューテーション | データ変更操作 | `use<Action><Resource>` (例: `useCreateOrder`) |
| UI ロジック | 表示制御・フィルタ・ソート | `use<Feature>Logic` (例: `useOrderFilterLogic`) |

---

## イベントハンドラ

### 命名規則: `handle<Action>`

```tsx
// ✅
function handleSubmit(event: SubmitEvent<HTMLFormElement>) { ... }
function handleDelete(id: string) { ... }
function handlePageChange(page: number) { ... }

// ❌ on プレフィックスはコールバック Props 用
function onSubmit() { ... }

// ❌ 動詞がない
function submit() { ... }
```

### Props として渡す場合は `on<Action>`

```tsx
// 親（呼び出し側）
<OrderCard onSelect={handleSelect} onDelete={handleDelete} />

// 子（受け取り側の Props）
interface OrderCardProps {
  readonly onSelect: (id: string) => void;
  readonly onDelete: (id: string) => void;
}
```

---

## 条件付きレンダリング

### early return パターンを優先する

```tsx
// ✅ early return（最も読みやすい）
export function OrderList({ orders, isLoading, error }: OrderListProps) {
  if (isLoading) {
    return <LoadingSkeleton />;
  }

  if (error) {
    return <ErrorMessage error={error} />;
  }

  if (orders.length === 0) {
    return <EmptyState message="注文がありません" />;
  }

  return (
    <ul>
      {orders.map((order) => (
        <OrderCard key={order.id} order={order} />
      ))}
    </ul>
  );
}

// ❌ 三項演算子のネスト
return isLoading ? <Spinner /> : error ? <Error /> : <List />;

// ❌ JSX 内の && チェーン
return (
  <div>
    {isLoading && <Spinner />}
    {!isLoading && error && <Error />}
    {!isLoading && !error && <List />}
  </div>
);
```

### JSX 内の条件付きレンダリングは単純なケースのみ

```tsx
// ✅ 単純な表示/非表示は && で OK
{hasPermission && <DeleteButton />}

// ✅ 二択は三項演算子で OK
{isEditing ? <EditForm /> : <DisplayView />}
```

一覧表示における Loading → Error → Empty → Content の具体的な表示順序ルールは `frontend-ui-patterns.md` の「一覧表示の定型パターン」を参照する。

---

## 定数

### feature 固有の定数は `types/` に配置

```tsx
// src/features/order/types/order.ts
export const ORDER_STATUS = {
  DRAFT: "DRAFT",
  CONFIRMED: "CONFIRMED",
  SHIPPED: "SHIPPED",
} as const;

export type OrderStatus = (typeof ORDER_STATUS)[keyof typeof ORDER_STATUS];
```

### ステータスラベル・カラー定義

backend の enum に対応する表示用定数（ラベル、カラー等）は feature 内の `types/` に配置する:

```tsx
// src/features/product/types/product-status.ts
export const PRODUCT_STATUS = {
  DRAFT: "DRAFT",
  PUBLISHED: "PUBLISHED",
  UNPUBLISHED: "UNPUBLISHED",
  ARCHIVED: "ARCHIVED",
} as const;

export type ProductStatus = (typeof PRODUCT_STATUS)[keyof typeof PRODUCT_STATUS];

export const STATUS_LABELS: Record<ProductStatus, string> = {
  DRAFT: "下書き",
  PUBLISHED: "公開",
  UNPUBLISHED: "非公開",
  ARCHIVED: "アーカイブ",
};

export const STATUS_COLORS: Record<ProductStatus, string> = {
  DRAFT: "bg-muted text-muted-foreground",
  PUBLISHED: "bg-emerald-100 text-emerald-700",
  UNPUBLISHED: "bg-amber-100 text-amber-700",
  ARCHIVED: "bg-zinc-100 text-zinc-500",
};
```

コンポーネント内にベタ書きせず、`types/` に切り出すことで:
- 型安全性が向上（`Record<ProductStatus, string>` で網羅性を保証）
- 複数コンポーネントで再利用可能
- backend の enum 変更時に一箇所で対応可能

### 区分値（参照データ）の管理方式

セレクトボックス等のプルダウン項目に使う区分値は、以下のいずれかで管理する:

- backend の enum に対応する固定値 → 上記「ステータスラベル・カラー定義」パターンに従い `types/` に定数として持つ（マスタAPIを都度呼ばない）
- 動的に変わる参照データ（DBで管理） → 専用の一覧取得APIから TanStack Query で取得する（Orval 生成 Hook 経由）

固定値か動的値かの判断基準: 「アプリケーションのデプロイなしに値を追加・変更する必要があるか」。必要ならDB管理、不要なら定数管理。

### アプリ共通の定数は `src/lib/constants.ts` に配置

---

## コメント

### コンポーネント/Hook に JSDoc は不要（ファイル名と Props 型が十分説明的）

```tsx
// ✅ コメント不要 — ファイル名 order-list.tsx + Props 型で十分
export function OrderList({ orders, onSelect }: OrderListProps) {

// ❌ 冗長
/** 注文一覧を表示するコンポーネント */
export function OrderList({ orders, onSelect }: OrderListProps) {
```

### ビジネスロジックには理由コメントを付ける

```tsx
// ✅ なぜそうするかを書く
// 同日注文は1件にまとめるビジネスルール
const grouped = groupBySameDay(orders);
```

---

## import ルール

- パスエイリアス `@/` を使う（`../` のような親ディレクトリへの相対パスは禁止）
- 同一ディレクトリ内の `./` は許可（例: `./use-member-list`）
- 型の import は `import type { X }` を使う（oxlint で強制）
- default export 禁止（ルートファイルと設定ファイルを除く）
- barrel export（`index.ts` からの re-export）禁止
- 循環 import 禁止（oxlint で強制）

---

## 禁止事項（oxlint で強制）

- `any` 型の使用
- `console.log`（デバッグ用途でも残さない）
- 配列の index を React の key に使用
- `dangerouslySetInnerHTML`
- `==` / `!=`（`===` / `!==` を使う）
- `var`（`const` / `let` を使う）
