import { describe, expect, it, vi, beforeEach } from "vite-plus/test";
import { render, screen } from "@testing-library/react";
import { ProductDetailCard } from "@/features/product/components/product-detail-card";
import * as useCategoryNameResolverModule from "@/features/product/hooks/use-category-name-resolver";

vi.mock("@/features/product/hooks/use-category-name-resolver");

describe("ProductDetailCard", () => {
  beforeEach(() => {
    vi.resetAllMocks();
    vi.mocked(useCategoryNameResolverModule.useCategoryNameResolver).mockReturnValue({
      resolveCategoryName: () => "衣料品",
    });
  });

  it("商品名・カテゴリ名・SKU・説明を表示する", () => {
    render(
      <ProductDetailCard
        product={{
          id: "1",
          name: "Tシャツ",
          description: "綿100%",
          categoryId: "cat-1",
          sku: "SKU-1",
          status: "DRAFT",
          version: 0,
        }}
      />,
    );

    expect(screen.getByText("Tシャツ")).toBeTruthy();
    expect(screen.getByText("衣料品")).toBeTruthy();
    expect(screen.getByText("SKU-1")).toBeTruthy();
    expect(screen.getByText("綿100%")).toBeTruthy();
  });
});
