import type { ComponentProps } from "react";
import { describe, expect, it, vi, beforeEach } from "vite-plus/test";
import { render, screen } from "@testing-library/react";
import { ProductListContent } from "@/features/product/components/product-list-content";
import * as useProductListPageModule from "@/features/product/hooks/use-product-list-page";
import * as useCategoryOptionsModule from "@/features/category/hooks/use-category-options";

vi.mock("@/features/product/hooks/use-product-list-page");
vi.mock("@/features/category/hooks/use-category-options");
// テスト環境では RouterProvider がないため Link を単純な a タグに差し替える
vi.mock("@tanstack/react-router", async () => {
  const actual = await vi.importActual("@tanstack/react-router");
  return {
    ...actual,
    Link: ({ children, to }: ComponentProps<"a"> & { readonly to?: string }) => (
      <a href={to}>{children}</a>
    ),
  };
});

function noop() {
  // このテストでは呼び出しを検証しない
}

describe("ProductListContent", () => {
  beforeEach(() => {
    vi.resetAllMocks();
    vi.mocked(useCategoryOptionsModule.useCategoryOptions).mockReturnValue({
      options: [],
      isLoading: false,
      error: null,
    });
  });

  it("データがある場合は一覧を表示する", () => {
    vi.mocked(useProductListPageModule.useProductListPage).mockReturnValue({
      filter: { categoryId: undefined, status: undefined },
      page: 0,
      products: [{ id: "1", name: "Tシャツ", status: "DRAFT", categoryId: "cat-1" }],
      totalPages: 1,
      isLoading: false,
      error: null,
      refetch: vi.fn(),
      onFilterChange: noop,
      onPageChange: noop,
    });

    render(<ProductListContent />);

    expect(screen.getByText("Tシャツ")).toBeTruthy();
  });

  it("空の場合は EmptyState を表示する", () => {
    vi.mocked(useProductListPageModule.useProductListPage).mockReturnValue({
      filter: { categoryId: undefined, status: undefined },
      page: 0,
      products: [],
      totalPages: 0,
      isLoading: false,
      error: null,
      refetch: vi.fn(),
      onFilterChange: noop,
      onPageChange: noop,
    });

    render(<ProductListContent />);

    expect(screen.getByText("商品がありません")).toBeTruthy();
  });
});
