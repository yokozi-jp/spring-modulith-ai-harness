import { describe, expect, it, vi, beforeEach } from "vite-plus/test";
import { render, screen, fireEvent } from "@testing-library/react";
import { CategoryForm } from "@/features/category/components/category-form";
import * as useCategoryOptionsModule from "@/features/category/hooks/use-category-options";

vi.mock("@/features/category/hooks/use-category-options");

describe("CategoryForm", () => {
  beforeEach(() => {
    vi.resetAllMocks();
    vi.mocked(useCategoryOptionsModule.useCategoryOptions).mockReturnValue({
      options: [{ id: "1", name: "衣料品" }],
      isLoading: false,
      error: null,
    });
  });

  it("親カテゴリ選択を表示しない場合、name と sortOrder のみ送信する", () => {
    const handleSubmit =
      vi.fn<(values: { name: string; sortOrder: number; parentCategoryId?: string }) => void>();
    render(
      <CategoryForm
        showParentSelect={false}
        onSubmit={handleSubmit}
        isSubmitting={false}
        submitLabel="更新"
      />,
    );

    fireEvent.change(screen.getByLabelText("カテゴリ名"), { target: { value: "衣料品" } });
    fireEvent.change(screen.getByLabelText("並び順"), { target: { value: "2" } });
    fireEvent.click(screen.getByRole("button", { name: "更新" }));

    expect(handleSubmit).toHaveBeenCalledWith({ name: "衣料品", sortOrder: 2 });
  });

  it("初期値を入力欄に反映する", () => {
    render(
      <CategoryForm
        initialValues={{ name: "衣料品", sortOrder: 3 }}
        showParentSelect={false}
        onSubmit={() => {
          // このテストでは送信結果を検証しない
        }}
        isSubmitting={false}
        submitLabel="更新"
      />,
    );

    expect(screen.getByLabelText("カテゴリ名")).toHaveProperty("value", "衣料品");
    expect(screen.getByLabelText("並び順")).toHaveProperty("value", "3");
  });

  it("送信中はボタンが disabled になる", () => {
    render(
      <CategoryForm
        showParentSelect={false}
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
