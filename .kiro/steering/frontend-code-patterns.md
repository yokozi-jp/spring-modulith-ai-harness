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

### 関数宣言を使う

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
function handleSubmit(event: React.FormEvent<HTMLFormElement>) { ... }
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
