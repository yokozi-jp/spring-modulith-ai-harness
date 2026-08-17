import { describe, expect, it, vi, beforeEach } from "vite-plus/test";
import { render, screen, fireEvent } from "@testing-library/react";
import { ProductFilter } from "@/features/product/components/product-filter";
import * as useCategoryOptionsModule from "@/features/category/hooks/use-category-options";

vi.mock("@/features/category/hooks/use-category-options");

// jsdom は scrollIntoView を実装していないため、Radix Select のドロップダウン展開に必要
Element.prototype.scrollIntoView = vi.fn<() => void>();

function noop() {
  // このテストでは呼び出しを検証しない
}

describe("ProductFilter", () => {
  beforeEach(() => {
    vi.resetAllMocks();
    vi.mocked(useCategoryOptionsModule.useCategoryOptions).mockReturnValue({
      options: [{ id: "cat-1", name: "衣料品" }],
      isLoading: false,
      error: null,
    });
  });

  it("カテゴリとステータスのセレクトを表示する", () => {
    render(<ProductFilter filter={{ categoryId: undefined, status: undefined }} onChange={noop} />);

    expect(screen.getByText("すべてのカテゴリ")).toBeTruthy();
    expect(screen.getByText("すべてのステータス")).toBeTruthy();
  });

  it("カテゴリ選択で onChange が呼ばれる", () => {
    const handleChange =
      vi.fn<(filter: { categoryId: string | undefined; status: string | undefined }) => void>();
    render(
      <ProductFilter
        filter={{ categoryId: undefined, status: undefined }}
        onChange={handleChange}
      />,
    );

    fireEvent.click(screen.getByText("すべてのカテゴリ"));
    fireEvent.click(screen.getByText("衣料品"));

    expect(handleChange).toHaveBeenCalledWith({ categoryId: "cat-1", status: undefined });
  });
});
