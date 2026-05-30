# フロントエンド データパターン規約

データの取得・変更・状態管理・エラー処理の書き方を決定的にする。

---

## データフェッチング（useQuery）

### 基本パターン

```tsx
// src/features/order/hooks/use-order-list.ts
import { useQuery } from "@tanstack/react-query";
import { getOrders } from "@/api/order";

export function useOrderList() {
  const query = useQuery({
    queryKey: ["orders"],
    queryFn: getOrders,
  });

  return {
    orders: query.data ?? [],
    isLoading: query.isLoading,
    error: query.error,
  };
}
```

### queryKey の命名規則

```tsx
// リソース一覧
queryKey: ["orders"]

// 単体取得（ID付き）
queryKey: ["orders", id]

// フィルタ付き一覧
queryKey: ["orders", { status, page }]

// ネストリソース
queryKey: ["orders", orderId, "items"]
```

ルール:
- 最初の要素はリソース名（複数形）
- ID はリソース名の直後
- フィルタはオブジェクトで渡す
- 階層はネストして表現する

### パラメータ付きクエリ

```tsx
export function useOrder(id: string) {
  const query = useQuery({
    queryKey: ["orders", id],
    queryFn: () => getOrder(id),
    enabled: id.length > 0,
  });

  return {
    order: query.data ?? null,
    isLoading: query.isLoading,
    error: query.error,
  };
}
```

### デフォルト値

- 配列: `query.data ?? []`
- 単体: `query.data ?? null`
- `undefined` を外に漏らさない

---

## ミューテーション（useMutation）

### 基本パターン

```tsx
// src/features/order/hooks/use-create-order.ts
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { createOrder } from "@/api/order";

export function useCreateOrder() {
  const queryClient = useQueryClient();

  const mutation = useMutation({
    mutationFn: createOrder,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["orders"] });
    },
  });

  return {
    createOrder: mutation.mutate,
    isCreating: mutation.isPending,
    error: mutation.error,
  };
}
```

### 命名規則

| 操作 | Hook 名 | 返却プロパティ |
|------|---------|--------------|
| 作成 | `useCreate<Resource>` | `create<Resource>`, `isCreating` |
| 更新 | `useUpdate<Resource>` | `update<Resource>`, `isUpdating` |
| 削除 | `useDelete<Resource>` | `delete<Resource>`, `isDeleting` |

### onSuccess での invalidation

```tsx
// 一覧を再取得
onSuccess: () => {
  queryClient.invalidateQueries({ queryKey: ["orders"] });
}

// 特定リソースを再取得
onSuccess: (_, variables) => {
  queryClient.invalidateQueries({ queryKey: ["orders", variables.id] });
}
```

---

## ローカル状態（useState）

### 使う場面

- フォーム入力値
- モーダルの開閉
- フィルタ/ソートの UI 状態

### 使わない場面（サーバーステートに任せる）

- API から取得したデータ → useQuery
- API への送信状態 → useMutation

### パターン

```tsx
// ✅ boolean は toggle 関数とセットにする
const [isOpen, setIsOpen] = useState(false);
function handleToggle() { setIsOpen((prev) => !prev); }

// ✅ フォーム値はオブジェクトでまとめる
const [form, setForm] = useState({ name: "", email: "" });
function handleChange(field: keyof typeof form, value: string) {
  setForm((prev) => ({ ...prev, [field]: value }));
}
```

### useState vs useReducer の判断基準

- 独立した値が 1〜2 個 → `useState`
- 関連する値が 3 個以上、または状態遷移が複雑 → `useReducer`

---

## エラーハンドリング

### API エラーの型

```tsx
// src/lib/api-error.ts
export interface ApiError {
  readonly status: number;
  readonly title: string;
  readonly detail: string;
}

export function isApiError(error: unknown): error is ApiError {
  return (
    typeof error === "object" &&
    error !== null &&
    "status" in error &&
    "title" in error
  );
}
```

### Hook でのエラー処理

```tsx
// Hook はエラーをそのまま返す（表示判断はコンポーネントに委ねる）
export function useOrderList() {
  const query = useQuery({ ... });

  return {
    orders: query.data ?? [],
    isLoading: query.isLoading,
    error: query.error,  // Error | null
  };
}
```

### コンポーネントでのエラー表示

```tsx
// コンポーネントが表示方法を決める
export function OrderList({ orders, isLoading, error }: OrderListProps) {
  if (error) {
    return <ErrorMessage error={error} />;
  }
  // ...
}
```

### ミューテーションのエラーは呼び出し元でハンドリング

```tsx
function handleSubmit() {
  createOrder(data, {
    onError: (error) => {
      // toast 表示等
    },
  });
}
```

---

## グローバル状態

### 原則: 使わない

- サーバーデータ → TanStack Query
- UI 状態 → 各コンポーネントの useState
- URL 状態 → TanStack Router の search params

### 例外的に必要な場合

認証情報やテーマ等、アプリ全体で参照する状態のみ React Context を使う:

```tsx
// src/lib/auth-context.ts（必要になったら作る、先に作らない）
```

**先に作らない**。必要になるまでグローバル状態は追加しない。
