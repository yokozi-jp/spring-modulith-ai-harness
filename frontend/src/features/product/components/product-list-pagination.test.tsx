import { describe, expect, it, vi } from "vite-plus/test";
import { render, screen, fireEvent } from "@testing-library/react";
import { ProductListPagination } from "@/features/product/components/product-list-pagination";

function noop(_page: number) {
  // このテストでは呼び出しを検証しない
}

describe("ProductListPagination", () => {
  it("totalPages が 1 以下の場合は何も表示しない", () => {
    render(<ProductListPagination page={0} totalPages={1} onPageChange={noop} />);

    expect(screen.queryByText("前へ")).toBeNull();
  });

  it("現在ページと総ページ数を表示する", () => {
    render(<ProductListPagination page={1} totalPages={3} onPageChange={noop} />);

    expect(screen.getByText("2 / 3 ページ")).toBeTruthy();
  });

  it("最初のページでは前へボタンが disabled", () => {
    render(<ProductListPagination page={0} totalPages={3} onPageChange={noop} />);

    expect(screen.getByRole("button", { name: "前へ" })).toHaveProperty("disabled", true);
  });

  it("最後のページでは次へボタンが disabled", () => {
    render(<ProductListPagination page={2} totalPages={3} onPageChange={noop} />);

    expect(screen.getByRole("button", { name: "次へ" })).toHaveProperty("disabled", true);
  });

  it("次へボタン押下で page+1 を渡す", () => {
    const handlePageChange = vi.fn<(page: number) => void>();
    render(<ProductListPagination page={0} totalPages={3} onPageChange={handlePageChange} />);

    fireEvent.click(screen.getByRole("button", { name: "次へ" }));

    expect(handlePageChange).toHaveBeenCalledWith(1);
  });
});
