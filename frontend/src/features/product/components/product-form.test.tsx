import { describe, expect, it, vi, beforeEach } from "vite-plus/test";
import { render, screen, fireEvent } from "@testing-library/react";
import { ProductForm } from "@/features/product/components/product-form";
import * as useCategoryOptionsModule from "@/features/category/hooks/use-category-options";

vi.mock("@/features/category/hooks/use-category-options");
Element.prototype.scrollIntoView = vi.fn<() => void>();

describe("ProductForm", () => {
  beforeEach(() => {
    vi.resetAllMocks();
    vi.mocked(useCategoryOptionsModule.useCategoryOptions).mockReturnValue({
      options: [{ id: "cat-1", name: "衣料品" }],
      isLoading: false,
      error: null,
    });
  });

  it("SKU 非表示の場合、sku を含まず送信する", () => {
    const handleSubmit =
      vi.fn<
        (values: { name: string; description: string; categoryId: string; sku?: string }) => void
      >();
    render(
      <ProductForm
        initialValues={{ name: "Tシャツ", description: "説明", categoryId: "cat-1" }}
        showSkuField={false}
        onSubmit={handleSubmit}
        isSubmitting={false}
        submitLabel="更新"
      />,
    );

    fireEvent.click(screen.getByRole("button", { name: "更新" }));

    expect(handleSubmit).toHaveBeenCalledWith({
      name: "Tシャツ",
      description: "説明",
      categoryId: "cat-1",
    });
  });

  it("SKU 表示の場合、入力した SKU を含めて送信する", () => {
    const handleSubmit =
      vi.fn<
        (values: { name: string; description: string; categoryId: string; sku?: string }) => void
      >();
    render(
      <ProductForm showSkuField onSubmit={handleSubmit} isSubmitting={false} submitLabel="作成" />,
    );

    fireEvent.change(screen.getByLabelText("商品名"), { target: { value: "Tシャツ" } });
    fireEvent.change(screen.getByLabelText("商品説明"), { target: { value: "説明" } });
    fireEvent.change(screen.getByLabelText("SKU"), { target: { value: "SKU-1" } });
    fireEvent.click(screen.getByRole("button", { name: "作成" }));

    expect(handleSubmit).toHaveBeenCalledWith({
      name: "Tシャツ",
      description: "説明",
      categoryId: "",
      sku: "SKU-1",
    });
  });

  it("送信中はボタンが disabled になる", () => {
    render(
      <ProductForm
        showSkuField
        onSubmit={() => {
          // このテストでは送信結果を検証しない
        }}
        isSubmitting
        submitLabel="作成"
      />,
    );

    expect(screen.getByRole("button", { name: "送信中..." })).toHaveProperty("disabled", true);
  });
});
