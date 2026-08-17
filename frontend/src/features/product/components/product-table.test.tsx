import type { ComponentProps } from "react";
import { describe, expect, it, vi, beforeEach } from "vite-plus/test";
import { render, screen } from "@testing-library/react";
import { ProductTable } from "@/features/product/components/product-table";
import * as useCategoryOptionsModule from "@/features/category/hooks/use-category-options";

vi.mock("@/features/category/hooks/use-category-options");
vi.mock("@tanstack/react-router", async () => {
  const actual = await vi.importActual("@tanstack/react-router");
  return {
    ...actual,
    Link: ({ children, to }: ComponentProps<"a"> & { readonly to?: string }) => (
      <a href={to}>{children}</a>
    ),
  };
});

describe("ProductTable", () => {
  beforeEach(() => {
    vi.resetAllMocks();
    vi.mocked(useCategoryOptionsModule.useCategoryOptions).mockReturnValue({
      options: [{ id: "cat-1", name: "衣料品" }],
      isLoading: false,
      error: null,
    });
  });

  it("商品名・カテゴリ名・ステータスを表示する", () => {
    render(
      <ProductTable
        products={[{ id: "1", name: "Tシャツ", status: "DRAFT", categoryId: "cat-1" }]}
      />,
    );

    expect(screen.getByText("Tシャツ")).toBeTruthy();
    expect(screen.getByText("衣料品")).toBeTruthy();
    expect(screen.getByText("下書き")).toBeTruthy();
  });

  it("複数行を表示する", () => {
    render(
      <ProductTable
        products={[
          { id: "1", name: "Tシャツ", status: "DRAFT", categoryId: "cat-1" },
          { id: "2", name: "パンツ", status: "PUBLISHED", categoryId: "cat-1" },
        ]}
      />,
    );

    expect(screen.getByText("Tシャツ")).toBeTruthy();
    expect(screen.getByText("パンツ")).toBeTruthy();
  });
});
