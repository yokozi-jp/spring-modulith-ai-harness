import { describe, expect, it, vi, beforeEach } from "vite-plus/test";
import { render, screen, fireEvent } from "@testing-library/react";
import { MoveCategoryDialog } from "@/features/category/components/move-category-dialog";
import * as useCategoryOptionsModule from "@/features/category/hooks/use-category-options";

vi.mock("@/features/category/hooks/use-category-options");

function noop() {
  // このテストでは呼び出しを検証しない
}

describe("MoveCategoryDialog", () => {
  beforeEach(() => {
    vi.resetAllMocks();
    vi.mocked(useCategoryOptionsModule.useCategoryOptions).mockReturnValue({
      options: [
        { id: "1", name: "衣料品" },
        { id: "2", name: "食品" },
      ],
      isLoading: false,
      error: null,
    });
  });

  it("isOpen が false の場合はダイアログを表示しない", () => {
    render(
      <MoveCategoryDialog
        isOpen={false}
        onClose={noop}
        onMove={noop}
        categoryId="1"
        isMoving={false}
      />,
    );

    expect(screen.queryByText("カテゴリを移動")).toBeNull();
  });

  it("isOpen が true の場合はダイアログを表示する", () => {
    render(
      <MoveCategoryDialog isOpen onClose={noop} onMove={noop} categoryId="1" isMoving={false} />,
    );

    expect(screen.getByText("カテゴリを移動")).toBeTruthy();
  });

  it("移動先未選択のまま移動する場合は undefined を渡す", () => {
    const handleMove = vi.fn<(newParentCategoryId?: string) => void>();
    render(
      <MoveCategoryDialog
        isOpen
        onClose={noop}
        onMove={handleMove}
        categoryId="1"
        isMoving={false}
      />,
    );

    fireEvent.click(screen.getByRole("button", { name: "移動する" }));

    expect(handleMove).toHaveBeenCalledWith(undefined);
  });

  it("キャンセルボタン押下で onClose を呼ぶ", () => {
    const handleClose = vi.fn<() => void>();
    render(
      <MoveCategoryDialog
        isOpen
        onClose={handleClose}
        onMove={noop}
        categoryId="1"
        isMoving={false}
      />,
    );

    fireEvent.click(screen.getByRole("button", { name: "キャンセル" }));

    expect(handleClose).toHaveBeenCalled();
  });
});
