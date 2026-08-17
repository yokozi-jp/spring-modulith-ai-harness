import type { ComponentProps } from "react";
import { describe, expect, it, vi, beforeEach } from "vite-plus/test";
import { render, screen, fireEvent } from "@testing-library/react";
import { CategoryTreeNodeItem } from "@/features/category/components/category-tree-node";
import * as useCategoryTreeNodeChildrenModule from "@/features/category/hooks/use-category-tree-node-children";

vi.mock("@/features/category/hooks/use-category-tree-node-children");
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

describe("CategoryTreeNodeItem", () => {
  beforeEach(() => {
    vi.resetAllMocks();
    vi.mocked(useCategoryTreeNodeChildrenModule.useCategoryTreeNodeChildren).mockReturnValue({
      childNodes: [],
      isLoading: false,
      error: null,
    });
  });

  it("カテゴリ名をリンクとして表示する", () => {
    render(<CategoryTreeNodeItem id="1" name="衣料品" depth={0} />);

    expect(screen.getByText("衣料品")).toBeTruthy();
  });

  it("初期状態では子カテゴリを表示しない", () => {
    render(<CategoryTreeNodeItem id="1" name="衣料品" depth={0} />);

    expect(screen.queryByText("トップス")).toBeNull();
  });

  it("展開ボタン押下で子カテゴリを表示する", () => {
    vi.mocked(useCategoryTreeNodeChildrenModule.useCategoryTreeNodeChildren).mockReturnValue({
      childNodes: [
        {
          id: "2",
          name: "トップス",
          sortOrder: 1,
          children: [],
          isExpanded: false,
          isLoaded: true,
        },
      ],
      isLoading: false,
      error: null,
    });

    render(<CategoryTreeNodeItem id="1" name="衣料品" depth={0} />);

    fireEvent.click(screen.getByRole("button", { name: "展開する" }));

    expect(screen.getByText("トップス")).toBeTruthy();
  });
});
