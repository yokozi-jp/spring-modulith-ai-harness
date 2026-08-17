import type { ComponentProps } from "react";
import { describe, expect, it, vi, beforeEach } from "vite-plus/test";
import { render, screen, fireEvent } from "@testing-library/react";
import { CategoryDetailHeader } from "@/features/category/components/category-detail-header";
import * as useDeleteCategoryModule from "@/features/category/hooks/use-delete-category";
import * as useMoveCategoryModule from "@/features/category/hooks/use-move-category";
import * as useCategoryOptionsModule from "@/features/category/hooks/use-category-options";

vi.mock("@/features/category/hooks/use-delete-category");
vi.mock("@/features/category/hooks/use-move-category");
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

describe("CategoryDetailHeader", () => {
  const deleteCategoryMock = vi.fn<(id: string, version: number) => void>();
  const moveCategoryMock =
    vi.fn<(input: { readonly newParentCategoryId?: string; readonly version: number }) => void>();

  beforeEach(() => {
    vi.resetAllMocks();
    vi.mocked(useDeleteCategoryModule.useDeleteCategory).mockReturnValue({
      deleteCategory: deleteCategoryMock,
      isDeleting: false,
      error: null,
    });
    vi.mocked(useMoveCategoryModule.useMoveCategory).mockReturnValue({
      moveCategory: moveCategoryMock,
      isMoving: false,
      error: null,
    });
    vi.mocked(useCategoryOptionsModule.useCategoryOptions).mockReturnValue({
      options: [],
      isLoading: false,
      error: null,
    });
  });

  it("カテゴリ名を見出しとして表示する", () => {
    render(<CategoryDetailHeader categoryId="1" categoryName="衣料品" version={0} />);

    expect(screen.getByRole("heading", { name: "衣料品" })).toBeTruthy();
  });

  it("削除ボタン押下で確認ダイアログを表示する", () => {
    render(<CategoryDetailHeader categoryId="1" categoryName="衣料品" version={0} />);

    fireEvent.click(screen.getByRole("button", { name: "削除" }));

    expect(screen.getByText("カテゴリを削除")).toBeTruthy();
  });

  it("削除確認で deleteCategory を呼ぶ", () => {
    render(<CategoryDetailHeader categoryId="1" categoryName="衣料品" version={0} />);

    fireEvent.click(screen.getByRole("button", { name: "削除" }));
    fireEvent.click(screen.getByRole("button", { name: "削除する" }));

    expect(deleteCategoryMock).toHaveBeenCalledWith("1", 0);
  });

  it("移動ボタン押下で移動ダイアログを表示する", () => {
    render(<CategoryDetailHeader categoryId="1" categoryName="衣料品" version={0} />);

    fireEvent.click(screen.getByRole("button", { name: "移動" }));

    expect(screen.getByText("カテゴリを移動")).toBeTruthy();
  });
});
