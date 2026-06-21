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
      void queryClient.invalidateQueries({ queryKey: ["orders"] });
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

`invalidateQueries` は Promise を返すため、`void` 演算子を付けて `no-floating-promises` 警告を防ぐ:

```tsx
// 一覧を再取得
onSuccess: () => {
  void queryClient.invalidateQueries({ queryKey: ["orders"] });
}

// 特定リソースを再取得
onSuccess: (_, variables) => {
  void queryClient.invalidateQueries({ queryKey: ["orders"] });
  void queryClient.invalidateQueries({ queryKey: ["orders", variables.id] });
}
```

### navigate も void を付ける

`useNavigate` の `navigate()` も Promise を返すため同様:

```tsx
onSuccess: () => {
  void navigate({ to: "/orders" });
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

---

## API クライアント（api-client.ts）

### Content-Type ヘッダーの設定

`Content-Type: application/json` は **body がある場合のみ** 付ける。GETリクエストにこのヘッダーを付けると、Spring が body を読もうとして `"Failed to read request"` エラーになる。

```typescript
const headers: Record<string, string> = {};

// body がある場合のみ Content-Type を付ける
if (config.data !== undefined) {
  headers["Content-Type"] = "application/json";
  init.body = JSON.stringify(config.data);
}
```

### CSRF トークン送信（Spring Security 連携）

Spring Security の CSRF 保護と連携するため、Cookie から `XSRF-TOKEN` を取得してヘッダーに送信する:

```typescript
// src/lib/api-client.ts
function getCsrfToken(): string | null {
  const match = document.cookie.match(/XSRF-TOKEN=([^;]+)/);
  const token = match?.[1];
  return token !== undefined ? decodeURIComponent(token) : null;
}

export async function apiClient<T>(url: string, options?: RequestInit): Promise<T> {
  const csrfToken = getCsrfToken();
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    ...(options?.headers as Record<string, string> | undefined),
  };
  if (csrfToken !== null) {
    headers["X-XSRF-TOKEN"] = csrfToken;
  }
  const response = await fetch(url, { ...options, headers, credentials: "include" });
  // ...
}
```

- `credentials: "include"` で Cookie を送信する
- POST/PUT/DELETE リクエスト時に `X-XSRF-TOKEN` ヘッダーが必須

### 201/204 レスポンスの考慮

REST API 規約では POST は `201 Created`（ボディなし）を返す。api-client でボディなしレスポンスを考慮する:

```typescript
if (response.status === 204 || response.status === 201) {
  return { data: undefined, status: response.status, headers: response.headers } as T;
}
const data = await response.json();
return { data, status: response.status, headers: response.headers } as T;
```

- 201: 作成成功。`Location` ヘッダーから作成リソースの URI を取得可能
- 204: 削除成功。レスポンスボディなし

---

## 楽観ロック（version）対応

更新・削除 API は `version` を必須とする。

### 削除・更新は詳細画面から行う

- 詳細取得時に `version` を取得済みなので、そのまま使う
- **一覧画面に削除ボタンを置かない**（version がないため）

```typescript
// 詳細画面から削除
const { category } = useCategory(id);  // version 取得済み
await deleteCategory(id, category.version);

// 詳細画面から更新
await updateCategory(id, { ...data, version: category.version });
```

### 409 Conflict の処理

他ユーザーが先に更新した場合、サーバーは 409 を返す:

```typescript
try {
  await updateCategory(id, data);
} catch (error) {
  if (isApiError(error) && error.status === 409) {
    setError("データが更新されました。再読み込みしてください");
    await refetch();
  }
}
```

### 削除時の 409 Conflict（関連データ存在）

削除時に関連データ（子カテゴリ、紐づく商品等）が存在する場合も 409 を返す。
エラーメッセージをユーザーに分かりやすく表示する:

```typescript
function handleDelete() {
  deleteCategory(id, version, {
    onError: (error) => {
      if (isApiError(error)) {
        // サーバーからのメッセージをそのまま表示、または変換
        setDeleteError(error.detail ?? "削除できませんでした");
      } else {
        setDeleteError("削除中にエラーが発生しました");
      }
    },
  });
}
```

- 409 エラーの `detail` フィールドには理由が含まれる（例: "このカテゴリには商品が紐づいています"）
- JSON 全文ではなく、ユーザーが理解できるメッセージに変換して表示する
