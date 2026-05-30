# フロントエンド テストパターン規約

テストの書き方を決定的にする。何をテストするか、どう書くかを明確にする。

---

## 基本ルール

- import は `vite-plus/test` から行う
- テストファイルは対象と同じディレクトリに `<name>.test.ts(x)` で配置
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
| カスタム Hook | **作る** | API 関数を `vi.mock` |
| 表示コンポーネント | **作る** | Props で状態を注入（モック不要） |
| 純粋な見た目 | **作らない** | — |

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

### API 関数をモックする

```typescript
// src/features/order/hooks/use-order-list.test.ts
import { describe, expect, it, vi, beforeEach } from "vite-plus/test";
import { renderHook, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";
import { useOrderList } from "@/features/order/hooks/use-order-list";
import * as orderApi from "@/api/order";

// API モジュールをモック
vi.mock("@/api/order");

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
    vi.mocked(orderApi.getOrders).mockReturnValue(new Promise(() => {})); // pending

    const { result } = renderHook(() => useOrderList(), {
      wrapper: createWrapper(),
    });

    expect(result.current.isLoading).toBe(true);
    expect(result.current.orders).toEqual([]);
  });

  it("取得成功時に orders を返す", async () => {
    const mockOrders = [{ id: "1", name: "注文A" }];
    vi.mocked(orderApi.getOrders).mockResolvedValue(mockOrders);

    const { result } = renderHook(() => useOrderList(), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.orders).toEqual(mockOrders);
  });

  it("取得失敗時に error を返す", async () => {
    vi.mocked(orderApi.getOrders).mockRejectedValue(new Error("API Error"));

    const { result } = renderHook(() => useOrderList(), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.error).not.toBeNull();
    });
  });
});
```

### モックのルール

- `vi.mock("@/api/...")` で API モジュール全体をモック
- `vi.mocked(fn).mockResolvedValue(...)` で戻り値を設定
- `beforeEach` で `vi.resetAllMocks()` を呼ぶ
- MSW は使わない（シンプルに `vi.mock` で統一）

---

## コンポーネントのテスト

### Props で状態を注入する（モック不要）

```typescript
// src/features/order/components/order-list.test.tsx
import { describe, expect, it } from "vite-plus/test";
import { render, screen } from "@testing-library/react";
import { OrderList } from "@/features/order/components/order-list";

describe("OrderList", () => {
  it("ローディング中は Skeleton を表示する", () => {
    render(<OrderList orders={[]} isLoading={true} error={null} />);

    expect(screen.getByRole("status")).toBeInTheDocument();
  });

  it("エラー時は ErrorMessage を表示する", () => {
    const error = new Error("取得失敗");
    render(<OrderList orders={[]} isLoading={false} error={error} />);

    expect(screen.getByRole("alert")).toBeInTheDocument();
    expect(screen.getByText("取得失敗")).toBeInTheDocument();
  });

  it("空の場合は EmptyState を表示する", () => {
    render(<OrderList orders={[]} isLoading={false} error={null} />);

    expect(screen.getByText("注文がありません")).toBeInTheDocument();
  });

  it("データがある場合は一覧を表示する", () => {
    const orders = [{ id: "1", name: "注文A" }];
    render(<OrderList orders={orders} isLoading={false} error={null} />);

    expect(screen.getByText("注文A")).toBeInTheDocument();
  });
});
```

### コンポーネントテストのルール

- Hook を内部で呼ぶコンポーネントはテストしない（Hook と表示を分離する設計）
- 表示コンポーネントは Props で全状態を受け取る → モック不要
- 要素の取得は `role` > `text` > `label` の順で優先（`data-testid` は最終手段）

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
