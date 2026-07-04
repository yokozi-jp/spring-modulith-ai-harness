# フロントエンド データパターン規約

データの取得・変更・状態管理・エラー処理の書き方を決定的にする。

---

## データフェッチング（useQuery）

### 基本パターン

Orval 生成 Hook をラップして使う:

```tsx
// src/features/order/hooks/use-order-list.ts
import { useList } from "@/api/order/order";  // Orval 生成 Hook
import type { OrderSummaryResponse } from "@/api/openAPIDefinition.schemas";

export function useOrderList() {
  const query = useList({
    param: {},
    pageable: { page: 0, size: 20, sort: ["createdAt,desc"] },
  });

  return {
    orders: (query.data?.data?.content ?? []) as OrderSummaryResponse[],
    isLoading: query.isLoading,
    error: query.error,
  };
}
```

**重要**: `useQuery` を直接使わず、Orval 生成の Hook（`useList`, `useFindById` 等）をラップする。

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

### Orval 生成コードの error 型について

Orval 生成 Hook の `error` は `TError` ジェネリクス（多くの場合 `ResponseType` や `unknown`）であり、JavaScript の `Error` 型ではない。
`ErrorMessage` コンポーネントに渡す際は以下のパターンで変換する:

```tsx
// ❌ 型エラーになる（Orval の error は Error 型ではない）
<ErrorMessage error={error} />

// ✅ Error に変換して渡す
<ErrorMessage
  error={error instanceof Error ? error : error !== null ? new Error(String(error)) : null}
/>
```

この変換が頻繁に必要な場合、ヘルパー関数を `src/lib/utils.ts` に追加する:

```tsx
export function toError(error: unknown): Error | null {
  if (error === null || error === undefined) {
    return null;
  }
  if (error instanceof Error) {
    return error;
  }
  return new Error(String(error));
}

// 使い方
<ErrorMessage error={toError(error)} />
```

### Hook でのエラー処理

```tsx
// Hook はエラーをそのまま返す（表示判断はコンポーネントに委ねる）
export function useOrderList() {
  const query = useQuery({ ... });

  return {
    orders: query.data ?? [],
    isLoading: query.isLoading,
    error: query.error,  // unknown 型（Orval 生成時）
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

## API クライアント（Orval 自動生成）

**Orval で生成する。手書き API は禁止。**

### 前提条件

Orval 実行前に backend の OpenAPI spec が必要。**OpenAPI spec がない状態で手書き API を作成しない。**

```bash
# 1. backend が起動しているか確認
docker ps | grep smah-backend

# 2. 起動していなければ起動（開発用コンテナ一式）
docker compose up -d

# 3. OpenAPI spec を取得（Basic認証: admin:admin）
curl -s -u admin:admin http://localhost:18080/v3/api-docs -o frontend/openapi.json

# 4. Orval でクライアント生成
cd frontend && npx orval
```

**AI は上記を自動実行してよい。** backend が起動していれば手順 3-4 のみ、起動していなければ手順 2 から実行する。

### ワークフロー

1. backend が起動していることを確認（`docker ps | grep smah-backend`）
2. `curl -s -u admin:admin http://localhost:18080/v3/api-docs -o frontend/openapi.json` で spec を取得
3. `cd frontend && npx orval` で `src/api/` に TanStack Query Hook + 型が自動生成される
4. features の Hook から `src/api/` の生成 Hook をラップして使う

### 生成されるファイル

```
src/api/
├── <tag>/
│   └── <tag>.ts             # API の Hook + 関数（タグごとにディレクトリ分割）
└── openAPIDefinition.schemas.ts  # 共通型
```

`mode: "tags-split"` 設定により、OpenAPI の tag ごとにディレクトリが作成される。

### features Hook での使い方

```tsx
// src/features/<resource>/hooks/use-<resource>-list.ts
import { useList } from "@/api/<resource>/<resource>";  // Orval 生成 Hook

export function use<Resource>List() {
  const query = useList({
    param: {},
    pageable: { page: 0, size: 20, sort: ["createdAt,desc"] },
  });

  return {
    <resources>: query.data?.data?.content ?? [],
    isLoading: query.isLoading,
    error: query.error,
  };
}
```

Orval 生成の Hook 名は OpenAPI の operationId に基づく（例: `useList`, `useList2`, `useFindById`, `useCreate`）。
番号が付く場合は同名の操作が複数タグにある場合。

### 禁止

以下は **絶対禁止** であり、いかなる理由でも回避策を発明してはいけない:

- `src/lib/*-api.ts` に API 関数を手書きしない
- `src/features/*/hooks/` で fetch を直接呼ばない
- `src/features/*/hooks/` で apiClient を直接呼ばない（Orval 生成 Hook を使う）
- `src/api/` を手動編集しない（再生成で上書きされる）

**「Orval がないから apiClient を直接使う」は禁止**。Orval がなければ frontend 実装を中断する。

**CI による強制**: `vp lint` の oxlint カスタムルール `project-rules/no-direct-api-client` が `src/features/*/hooks/` 内で `apiClient` を直接 import しているファイルを検出し、ビルドを失敗させる。この CI チェックは回避できない。

### API が存在しない・生成できない場合

**⛔ 絶対禁止: 回避策を発明しない**

backend が起動していない、または OpenAPI spec が生成されていない場合:

1. **frontend の実装を即座に中断する**
2. ユーザーに「backend を先に起動して `npx orval` で API を生成してください」と伝える
3. **絶対に以下をしない**:
   - `apiClient` を直接呼び出す Hook を書く
   - `fetch` を直接呼び出す
   - 手書きの API 関数を作成する
   - 「パターンに従って」と称して Orval 以外の方法を使う
   - 「一時的に」「暫定的に」と称して代替実装を作る

**理由**: 手書き API は型安全性がなく、OpenAPI spec との乖離を招く。Orval 生成コードと混在すると保守が困難になる。

**正しい対応**:
```
AI: 「src/api/ に Orval 生成コードがありません。
     backend を起動して以下を実行してください:
     1. make be-up
     2. make be-test  
     3. cd frontend && npx orval
     その後、frontend 実装を再開します。」
```

**誤った対応（絶対禁止）**:
```
AI: 「Orval 生成コードがないので、パターンに従って
     apiClient を直接使う Hook を作成します...」  ← ❌ これは禁止
```

### api-client.ts の役割

`src/lib/api-client.ts` は Orval のカスタム fetch 実装として使う。以下を担当:

- CSRF トークン送信（Spring Security 連携）
- `credentials: "include"` で Cookie 送信
- エラーレスポンスの ProblemDetail パース
- **Orval 生成コードが期待する `{ data, status, headers }` 形式でレスポンスを返す**

```typescript
// orval.config.ts で指定
export default defineConfig({
  api: {
    output: {
      client: "react-query",
      httpClient: "fetch",
      override: {
        mutator: {
          path: "./src/lib/api-client.ts",
          name: "apiClient",
        },
      },
    },
  },
});
```

### api-client.ts の正しい実装

Orval 生成コードは `apiClient<T>(url: string, options?: RequestInit): Promise<T>` 形式で呼び出し、
戻り値として `{ data, status, headers }` 構造を期待する。**この形式を守らないと型エラーや実行時エラーになる。**

```typescript
// src/lib/api-client.ts — Orval 互換実装
function getCsrfToken(): string | null {
  const match = document.cookie.match(/XSRF-TOKEN=([^;]+)/);
  const token = match?.[1];
  return token !== undefined ? decodeURIComponent(token) : null;
}

export async function apiClient<T>(url: string, options?: RequestInit): Promise<T> {
  const headers = new Headers(options?.headers);

  const csrfToken = getCsrfToken();
  if (csrfToken !== null) {
    headers.set("X-XSRF-TOKEN", csrfToken);
  }

  const response = await fetch(url, {
    ...options,
    headers,
    credentials: "include",
  });

  if (!response.ok) {
    const text = await response.text();
    let message = `${String(response.status)} ${response.statusText}`;
    if (text.length > 0) {
      try {
        const problem = JSON.parse(text) as { detail?: string };
        if (problem.detail !== undefined) {
          message = problem.detail;
        }
      } catch {
        message = text;
      }
    }
    throw new Error(message);
  }

  const text = await response.text();
  const data = text.length > 0 ? JSON.parse(text) : undefined;

  // Orval 生成コードが期待する形式で返す
  return { data, status: response.status, headers: response.headers } as T;
}
```

**重要**: 
- シグネチャ: `apiClient<T>(url: string, options?: RequestInit): Promise<T>`
- 戻り値: `{ data, status, headers }` 構造（Orval の `httpClient: "fetch"` が期待する形式）
- この実装を変更する場合は Orval 生成コードとの互換性を確認すること

### `src/api/` の lint warning について

`verify.sh` 実行時に `src/api/` から `no-misused-spread` や `no-base-to-string` の warning が出ることがある。
**これらは Orval 生成コードの問題であり、無視してよい。**

判断基準:
- **エラー 0 件** で `verify.sh` が成功 → OK
- **warning のみ**（全て `src/api/` から）→ OK
- `src/features/` や `src/components/` からエラー → 修正必須

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
