import type { ComponentProps } from "react";
import { describe, expect, it, vi, beforeEach } from "vite-plus/test";
import { render, screen } from "@testing-library/react";
import { CategoryTree } from "@/features/category/components/category-tree";
import * as useCategoryListModule from "@/features/category/hooks/use-category-list";

vi.mock("@/features/category/hooks/use-category-list");
vi.mock("@/features/category/hooks/use-category-children", () => ({
  useCategoryChildren: () => ({ children: [], isLoading: false, error: null, refetch: vi.fn() }),
}));
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

describe("CategoryTree", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it("ローディング中は Skeleton を表示する", () => {
    vi.mocked(useCategoryListModule.useCategoryList).mockReturnValue({
      categories: [],
      isLoading: true,
      error: null,
      refetch: vi.fn(),
    });

    render(<CategoryTree />);

    expect(screen.getByRole("status")).toBeTruthy();
  });

  it("エラー時は ErrorMessage を表示する", () => {
    vi.mocked(useCategoryListModule.useCategoryList).mockReturnValue({
      categories: [],
      isLoading: false,
      error: new Error("取得失敗"),
      refetch: vi.fn(),
    });

    render(<CategoryTree />);

    expect(screen.getByRole("alert")).toBeTruthy();
    expect(screen.getByText("取得失敗")).toBeTruthy();
  });

  it("空の場合は EmptyState を表示する", () => {
    vi.mocked(useCategoryListModule.useCategoryList).mockReturnValue({
      categories: [],
      isLoading: false,
      error: null,
      refetch: vi.fn(),
    });

    render(<CategoryTree />);

    expect(screen.getByText("カテゴリがありません")).toBeTruthy();
  });

  it("データがある場合はルートカテゴリ一覧を表示する", () => {
    vi.mocked(useCategoryListModule.useCategoryList).mockReturnValue({
      categories: [{ id: "1", name: "衣料品", sortOrder: 1 }],
      isLoading: false,
      error: null,
      refetch: vi.fn(),
    });

    render(<CategoryTree />);

    expect(screen.getByText("衣料品")).toBeTruthy();
  });
});
