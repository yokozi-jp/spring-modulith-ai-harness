import { describe, expect, it, vi, beforeEach } from "vite-plus/test";
import { render, screen } from "@testing-library/react";
import { ProductListBody } from "@/features/product/components/product-list-body";

function noop() {
  // このテストでは呼び出しを検証しない
}

describe("ProductListBody", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it("ローディング中は Skeleton を表示する", () => {
    render(<ProductListBody products={[]} isLoading error={null} onRetry={noop} />);

    expect(screen.getByRole("status")).toBeTruthy();
  });

  it("エラー時は ErrorMessage を表示する", () => {
    render(
      <ProductListBody
        products={[]}
        isLoading={false}
        error={new Error("取得失敗")}
        onRetry={noop}
      />,
    );

    expect(screen.getByRole("alert")).toBeTruthy();
    expect(screen.getByText("取得失敗")).toBeTruthy();
  });

  it("空の場合は EmptyState を表示する", () => {
    render(<ProductListBody products={[]} isLoading={false} error={null} onRetry={noop} />);

    expect(screen.getByText("商品がありません")).toBeTruthy();
  });
});
