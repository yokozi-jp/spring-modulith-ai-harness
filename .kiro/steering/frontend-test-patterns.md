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

## ユーティリティ関数のテスト

最もシンプル。入力→出力を検証。

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

  it("undefined と空文字を無視する", () => {
    expect(cn("px-2", undefined, "", "py-1")).toBe("px-2 py-1");
  });
});
```

---

## Hook のテスト

### データ取得 Hook（useQuery ベース）

```typescript
// src/features/order/hooks/use-order-list.test.ts
import { describe, expect, it, vi } from "vite-plus/test";
import { renderHook, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";
import { useOrderList } from "@/features/order/hooks/use-order-list";

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
  it("初期状態で isLoading が true", () => {
    const { result } = renderHook(() => useOrderList(), {
      wrapper: createWrapper(),
    });

    expect(result.current.isLoading).toBe(true);
    expect(result.current.orders).toEqual([]);
  });
});
```

### ミューテーション Hook

```typescript
// src/features/order/hooks/use-create-order.test.ts
import { describe, expect, it, vi } from "vite-plus/test";
import { renderHook, act } from "@testing-library/react";
import { useCreateOrder } from "@/features/order/hooks/use-create-order";

describe("useCreateOrder", () => {
  it("初期状態で isCreating が false", () => {
    const { result } = renderHook(() => useCreateOrder(), {
      wrapper: createWrapper(),
    });

    expect(result.current.isCreating).toBe(false);
  });
});
```

---

## コンポーネントのテスト

### 表示コンポーネント（Props を渡して描画確認）

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

---

## テストで使わないもの

| 使わない | 理由 | 代替 |
|----------|------|------|
| `jest` | Vitest を使う | `vite-plus/test` |
| `enzyme` | 非推奨 | `@testing-library/react` |
| スナップショットテスト | 脆く保守コスト高い | 具体的なアサーション |
| `data-testid` の乱用 | アクセシビリティを損なう | `role`, `text`, `label` で取得 |

---

## テスト対象の優先度

| 優先度 | 対象 | テスト内容 |
|:------:|------|-----------|
| 高 | ユーティリティ関数 | 入力→出力 |
| 高 | カスタム Hook（ロジック） | 状態遷移、副作用 |
| 中 | 表示コンポーネント | 条件分岐による表示切替 |
| — | 純粋な見た目 | Storybook に任せる（Vitest では書かない） |

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

---

## testing-library の導入

プロジェクトセットアップ時に追加する:

```bash
cd frontend && vp install -D @testing-library/react @testing-library/jest-dom
```

tsconfig に型を追加:

```json
"types": ["vite/client", "@testing-library/jest-dom"]
```
