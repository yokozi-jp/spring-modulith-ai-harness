# フロントエンド テストパターン規約

テストの書き方を決定的にする。何をテストするか、どう書くかを明確にする。

---

## テスト必須ルール

以下のファイルには **必ず対応するテストファイルを作成する**:

| 対象 | テストファイル |
|------|---------------|
| `src/features/*/hooks/*.ts` | `*.test.ts` |
| `src/hooks/*.ts` | `*.test.ts` |
| `src/lib/*.ts` | `*.test.ts` |

**verify.sh でチェックされる**。テストファイルがないと CI が失敗する。

除外対象:
- `*.test.ts`（テストファイル自体）
- `*.d.ts`（型定義ファイル）
- `index.ts`（barrel export）
- `api-client.ts`（Orval 設定）
- `query-client.ts`（QueryClient 設定）

---

## 基本ルール

- テストは `vp test` で実行する（Vitest 内蔵）
- import は `vite-plus/test` から行う（`vitest` を直接インストールしない）
- テストファイルは `*.test.ts` / `*.test.tsx` で命名し、対象と同じディレクトリに配置する（例: `lib/utils.test.ts`）
- `describe` でグループ化、`it` で個別ケース
- テスト名は日本語 OK（何をテストしているか明確にする）

```typescript
import { describe, expect, it } from "vite-plus/test";
```

---

## テスト対象とモック方針

| 対象 | テスト | モック |
|------|--------|--------|
| ユーティリティ関数 | **作る** | なし（純粋関数） |
| カスタム Hook（Orval 生成 Hook 経由） | **作る** | `@/lib/api-client` の `apiClient` を `vi.mock` |
| コンポーネント | **作る** | Hook を `vi.mock` |
| 純粋な見た目 | **作らない** | — |

**重要**: features の Hook は Orval 生成 Hook（`useListXxx`, `useCreateXxx` 等）をラップしている（`frontend-data-patterns.md` 参照）。モック対象は Orval 生成モジュール（`@/api/<tag>/<tag>`）ではなく `@/lib/api-client` の `apiClient` である。理由は次セクションで説明する。

---

## ユーティリティ関数のテスト

モックなし。入力→出力を検証。

```typescript
// src/lib/utils.test.ts
import { describe, expect, it } from "vite-plus/test";
import { cn } from "@/lib/utils";

describe("cn", () => {
  it("クラス名を結合する", () => {
    expect(cn("px-2", "py-1")).toBe("px-2 py-1");
  });

  it("Tailwind の競合を解決する", () => {
    expect(cn("px-2", "px-4")).toBe("px-4");
  });
});
```

---

## Hook のテスト

### ⛔ 禁止パターン — Orval 生成モジュールを丸ごとモックする

```typescript
// ❌ これは機能しない
vi.mock("@/api/order/order");
vi.mocked(orderApi.getOrders).mockResolvedValue(mockOrders);
```

**理由**: Orval 生成モジュール（`src/api/<tag>/<tag>.ts`）は、`useListXxx`/`useCreateXxx` 等の TanStack Query Hook と、fetch 関数（`listXxx`/`createXxx` 等）が**同一モジュール内で直接参照し合う**構造になっている。Hook 内部の `queryFn`/`mutationFn` は、モジュールのトップレベルで定義された fetch 関数をクロージャで直接呼び出す。

`vi.mock("@/api/order/order", factory)` で `factory` が `actual` をスプレッドしつつ fetch 関数だけを `vi.fn()` に差し替えても、**モジュール内部の相互参照までは差し替わらない**。外部から見える `orderApi.getOrders` は差し替わったモック関数だが、Hook 内部が呼ぶ実体は元のモジュールスコープの関数（本物の fetch 呼び出し）のままである。

この状態でテストを実行すると、モックの `mockResolvedValue` は一切使われず、本物の `fetch` が発火して以下のような実行時エラーになる（テストでしか発覚しない）:

```
TypeError: Failed to parse URL from /api/v1/orders/1
    at apiClient (src/lib/api-client.ts:17:26)
    at deleteOrder (src/api/order/order.ts:174:44)
    at Object.mutationFn (src/api/order/order.ts:198:10)
```

### ✅ 正しいパターン — `apiClient` をモックする

Orval 生成コードは最終的にすべて `@/lib/api-client` の `apiClient` 関数を経由して fetch する（`frontend-data-patterns.md` の「API クライアント」参照）。この最下層をモックすれば、Hook・fetch 関数の内部結合を気にせず確実にモックできる。

```typescript
// src/features/order/hooks/use-order-list.test.ts
import type { ReactNode } from "react";
import { describe, expect, it, vi, beforeEach } from "vite-plus/test";
import { renderHook, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useOrderList } from "@/features/order/hooks/use-order-list";
import { apiClient } from "@/lib/api-client";

// Orval 生成モジュールではなく apiClient をモックする
vi.mock("@/lib/api-client");

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });

  return function Wrapper({ children }: { readonly children: ReactNode }) {
    return (
      <QueryClientProvider client={queryClient}>
        {children}
      </QueryClientProvider>
    );
  };
}

describe("useOrderList", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it("初期状態で isLoading が true", () => {
    vi.mocked(apiClient).mockReturnValue(
      new Promise(() => {
        // 意図的に resolve/reject しない: pending 状態を維持するためのモック
      }),
    );

    const { result } = renderHook(() => useOrderList(), {
      wrapper: createWrapper(),
    });

    expect(result.current.isLoading).toBe(true);
    expect(result.current.orders).toEqual([]);
  });

  it("取得成功時に orders を返す", async () => {
    vi.mocked(apiClient).mockResolvedValue({
      data: { content: [{ id: "1", name: "注文A" }] },
      status: 200,
      headers: new Headers(),
    });

    const { result } = renderHook(() => useOrderList(), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.orders).toEqual([{ id: "1", name: "注文A" }]);
  });

  it("取得失敗時に error を返す", async () => {
    vi.mocked(apiClient).mockRejectedValue(new Error("API Error"));

    const { result } = renderHook(() => useOrderList(), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.error).not.toBeNull();
    });
  });
});
```

`apiClient` の戻り値は Orval 生成コードが期待する `{ data, status, headers }` 構造で返すこと（`frontend-data-patterns.md` の「api-client.ts の正しい実装」参照）。

### ミューテーション系 Hook のテスト（`mutate()` は `act()` でラップする）

作成・更新・削除の Hook をテストする際、`result.current.createOrder(...)` のような呼び出しは同期的に見えるが、TanStack Query の `mutate()` は内部でバッチングされるため、**`act()` でラップしないと `mutate` が実際にトリガーされない**。ラップを忘れると `apiClient` の呼び出し自体が発生せず、`toHaveBeenCalledWith` のアサーションが「呼ばれていない」で失敗する。

```typescript
// src/features/order/hooks/use-create-order.test.tsx
import type { ReactNode } from "react";
import { act } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { renderHook, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vite-plus/test";
import { useCreateOrder } from "@/features/order/hooks/use-create-order";
import { apiClient } from "@/lib/api-client";
import * as tanstackRouter from "@tanstack/react-router";

vi.mock("@/lib/api-client");
// navigate を使う Hook は useNavigate もモックする
vi.mock("@tanstack/react-router", async () => {
  const actual = await vi.importActual("@tanstack/react-router");
  return { ...actual, useNavigate: vi.fn() };
});

function createWrapper() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return function Wrapper({ children }: { readonly children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  };
}

describe("useCreateOrder", () => {
  const navigateMock = vi.fn();

  beforeEach(() => {
    vi.resetAllMocks();
    vi.mocked(tanstackRouter.useNavigate).mockReturnValue(navigateMock);
  });

  it("作成リクエストを正しい内容で送信する", async () => {
    vi.mocked(apiClient).mockResolvedValue({ data: undefined, status: 201, headers: new Headers() });

    const { result } = renderHook(() => useCreateOrder(), { wrapper: createWrapper() });

    // ❌ act() なしだと mutate が実行されず apiClient が呼ばれない
    // result.current.createOrder({ customerName: "山田" });

    // ✅ act() でラップする
    act(() => {
      result.current.createOrder({ customerName: "山田" });
    });

    await waitFor(() => {
      expect(apiClient).toHaveBeenCalledWith(
        "/api/v1/orders",
        expect.objectContaining({ method: "POST", body: JSON.stringify({ customerName: "山田" }) }),
      );
    });
  });

  it("作成成功時に一覧ページへ遷移する", async () => {
    vi.mocked(apiClient).mockResolvedValue({ data: undefined, status: 201, headers: new Headers() });

    const { result } = renderHook(() => useCreateOrder(), { wrapper: createWrapper() });

    act(() => {
      result.current.createOrder({ customerName: "山田" });
    });

    await waitFor(() => {
      expect(navigateMock).toHaveBeenCalledWith({ to: "/orders" });
    });
  });
});
```

### モックのルール

- `vi.mock("@/lib/api-client")` で `apiClient` をモックする（Orval 生成モジュールを直接モックしない）
- クエリ系: `vi.mocked(apiClient).mockResolvedValue({ data, status, headers })` で戻り値を設定
- ミューテーション系: `mutate()` を呼ぶ操作は必ず `act()` でラップする
- `beforeEach` で `vi.resetAllMocks()` を呼ぶ
- `useNavigate` を使う Hook をテストする場合は `@tanstack/react-router` も `actual` スプレッド + `useNavigate: vi.fn()` でモックする
- MSW は使わない（シンプルに `vi.mock` で統一）。`no-restricted-imports` で `msw`/`vitest`/`enzyme` の直接 import を検出する

---

## コンポーネントのテスト

### `@testing-library/jest-dom` は導入していない

本プロジェクトは `@testing-library/jest-dom` を依存に追加していない。`toBeInTheDocument()` 等の jest-dom 拡張マッチャーは**使用できない**。代わりに Vitest 標準の `expect` で以下のように書く。

| jest-dom（使用不可） | 標準 `expect`（本プロジェクトの書き方） |
|---|---|
| `expect(el).toBeInTheDocument()` | `expect(el).toBeTruthy()` |
| `expect(el).not.toBeInTheDocument()` | `expect(el).toBeNull()` （`queryBy*` と組み合わせる） |
| `expect(el).toHaveTextContent("x")` | `expect(el.textContent).toBe("x")` |

`screen.getBy*` 系は見つからない場合に例外を投げるため、`toBeTruthy()` と組み合わせるだけで「存在すること」を十分検証できる。存在しないことを検証する場合は例外を投げない `screen.queryBy*` を使い `toBeNull()` で確認する。

jest-dom の導入が必要な場面（より詳細なマッチャーが欲しい等）が出てきた場合は、依存追加とこのセクションの更新をセットで行うこと。

### Hook をモックする

```typescript
// src/features/order/components/order-list.test.tsx
import { describe, expect, it, vi, beforeEach } from "vite-plus/test";
import { render, screen } from "@testing-library/react";
import { OrderList } from "@/features/order/components/order-list";
import * as useOrderListModule from "@/features/order/hooks/use-order-list";

vi.mock("@/features/order/hooks/use-order-list");

describe("OrderList", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it("ローディング中は Skeleton を表示する", () => {
    vi.mocked(useOrderListModule.useOrderList).mockReturnValue({
      orders: [],
      isLoading: true,
      error: null,
    });

    render(<OrderList />);

    expect(screen.getByRole("status")).toBeTruthy();
  });

  it("エラー時は ErrorMessage を表示する", () => {
    vi.mocked(useOrderListModule.useOrderList).mockReturnValue({
      orders: [],
      isLoading: false,
      error: new Error("取得失敗"),
    });

    render(<OrderList />);

    expect(screen.getByRole("alert")).toBeTruthy();
    expect(screen.getByText("取得失敗")).toBeTruthy();
  });

  it("空の場合は EmptyState を表示する", () => {
    vi.mocked(useOrderListModule.useOrderList).mockReturnValue({
      orders: [],
      isLoading: false,
      error: null,
    });

    render(<OrderList />);

    expect(screen.getByText("注文がありません")).toBeTruthy();
  });

  it("データがある場合は一覧を表示する", () => {
    vi.mocked(useOrderListModule.useOrderList).mockReturnValue({
      orders: [{ id: "1", name: "注文A" }],
      isLoading: false,
      error: null,
    });

    render(<OrderList />);

    expect(screen.getByText("注文A")).toBeTruthy();
  });

  it("要素が存在しないことを確認する場合は queryBy* + toBeNull を使う", () => {
    vi.mocked(useOrderListModule.useOrderList).mockReturnValue({
      orders: [],
      isLoading: true,
      error: null,
    });

    render(<OrderList />);

    expect(screen.queryByRole("alert")).toBeNull();
  });
});
```

### コンポーネントテストのルール

- `vi.mock("@/features/.../hooks/use-xxx")` で Hook をモック
- `vi.mocked(hook).mockReturnValue(...)` で返り値を設定
- 要素の取得は `role` > `text` > `label` の順で優先（`data-testid` は最終手段）
- 存在確認は `getBy*` + `toBeTruthy()`、非存在確認は `queryBy*` + `toBeNull()`（jest-dom 未導入のため）

### `Link` を含むコンポーネントのテスト（`@tanstack/react-router` モック）

`@tanstack/react-router` の `Link` を使うコンポーネントは、テスト環境で `RouterProvider` が存在しないためそのままでは動かない。`@tanstack/react-router` モジュール全体を `vi.mock` + `importActual` でモックし、`Link` のみを単純な HTML 要素に差し替える。

```typescript
import type { ComponentProps } from "react";
import { describe, expect, it, vi, beforeEach } from "vite-plus/test";
import { render, screen } from "@testing-library/react";
import { OrderCard } from "@/features/order/components/order-card";

// Link を単純な <a> タグに差し替える
vi.mock("@tanstack/react-router", async () => {
  const actual = await vi.importActual("@tanstack/react-router");
  return {
    ...actual,
    Link: ({ children, to }: ComponentProps<"a"> & { readonly to?: string }) => (
      <a href={to}>{children}</a>
    ),
  };
});

describe("OrderCard", () => {
  it("注文名をリンクとして表示する", () => {
    render(<OrderCard id="1" name="注文A" />);

    expect(screen.getByText("注文A")).toBeTruthy();
  });
});
```

注意点:
- `actual` をスプレッドして他のエクスポート（`useNavigate`, `createFileRoute` 等）を保持する。`Link` だけを差し替える
- `Link` の props で `to` を受け取り `<a href={to}>` に渡すことで、リンク先の検証（`getByRole("link", { name: "..." })`）も可能にする
- `jsx-a11y/anchor-is-valid` に違反しないよう、`href` には実際の `to` 値を渡す（`"#"` は使わない）
- `Link` 内で `children` が文字列でない場合（`Button asChild` 等の組み合わせ）は `<span>{children}</span>` にフォールバックしてもよい
- `useNavigate` もモックする場合は同じ `vi.mock` ブロック内で `useNavigate: vi.fn()` を追加する（`frontend-test-patterns.md` の「ミューテーション系 Hook のテスト」参照）

---

## テストで使わないもの

| 使わない | 理由 | 代替 |
|----------|------|------|
| MSW | 過剰。`vi.mock` で十分 | `vi.mock` + `mockResolvedValue` |
| `jest` | Vitest を使う | `vite-plus/test` |
| `enzyme` | 非推奨 | `@testing-library/react` |
| スナップショットテスト | 脆く保守コスト高い | 具体的なアサーション |
| `data-testid` の乱用 | アクセシビリティを損なう | `role`, `text`, `label` で取得 |

---

## Storybook との役割分担

| 検証内容 | 担当 |
|----------|------|
| ロジック（Hook、ユーティリティ） | Vitest |
| 条件分岐による表示切替 | Vitest |
| 見た目・レイアウト・インタラクション | Storybook |
| Props のバリエーション確認 | Storybook |

Storybook で十分カバーできる「見た目の確認」は Vitest で重複して書かない。

---

## テストの命名規則

```typescript
describe("<対象の名前>", () => {
  it("<期待する振る舞い>", () => { ... });
});
```

例:
- `it("空の配列を返す")`
- `it("ローディング中は Skeleton を表示する")`
- `it("エラー時にリトライボタンを表示する")`

**「〜すること」ではなく「〜する」** で終わる（断定形）。
