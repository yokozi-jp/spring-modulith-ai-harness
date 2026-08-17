import type { ComponentProps } from "react";
import { describe, expect, it, vi, beforeEach } from "vite-plus/test";
import { render, screen, fireEvent } from "@testing-library/react";
import { ProductDetailHeader } from "@/features/product/components/product-detail-header";
import * as useDeleteProductModule from "@/features/product/hooks/use-delete-product";
import * as usePublishProductModule from "@/features/product/hooks/use-publish-product";
import * as useUnpublishProductModule from "@/features/product/hooks/use-unpublish-product";
import * as useArchiveProductModule from "@/features/product/hooks/use-archive-product";

vi.mock("@/features/product/hooks/use-delete-product");
vi.mock("@/features/product/hooks/use-publish-product");
vi.mock("@/features/product/hooks/use-unpublish-product");
vi.mock("@/features/product/hooks/use-archive-product");
vi.mock("@tanstack/react-router", async () => {
  const actual = await vi.importActual("@tanstack/react-router");
  return {
    ...actual,
    Link: ({ children, to }: ComponentProps<"a"> & { readonly to?: string }) => (
      <a href={to}>{children}</a>
    ),
  };
});

describe("ProductDetailHeader", () => {
  const deleteProductMock = vi.fn<(id: string, version: number) => void>();
  const publishProductMock = vi.fn<(version: number) => void>();
  const unpublishProductMock = vi.fn<(version: number) => void>();
  const archiveProductMock = vi.fn<(version: number) => void>();

  beforeEach(() => {
    vi.resetAllMocks();
    vi.mocked(useDeleteProductModule.useDeleteProduct).mockReturnValue({
      deleteProduct: deleteProductMock,
      isDeleting: false,
      error: null,
    });
    vi.mocked(usePublishProductModule.usePublishProduct).mockReturnValue({
      publishProduct: publishProductMock,
      isPublishing: false,
      error: null,
    });
    vi.mocked(useUnpublishProductModule.useUnpublishProduct).mockReturnValue({
      unpublishProduct: unpublishProductMock,
      isUnpublishing: false,
      error: null,
    });
    vi.mocked(useArchiveProductModule.useArchiveProduct).mockReturnValue({
      archiveProduct: archiveProductMock,
      isArchiving: false,
      error: null,
    });
  });

  it("商品名とステータスバッジを表示する", () => {
    render(<ProductDetailHeader productId="1" productName="Tシャツ" status="DRAFT" version={0} />);

    expect(screen.getByRole("heading", { name: "Tシャツ" })).toBeTruthy();
    expect(screen.getByText("下書き")).toBeTruthy();
  });

  it("DRAFT の場合は公開ボタンを表示する", () => {
    render(<ProductDetailHeader productId="1" productName="Tシャツ" status="DRAFT" version={0} />);

    expect(screen.getByRole("button", { name: "公開" })).toBeTruthy();
    expect(screen.queryByRole("button", { name: "非公開にする" })).toBeNull();
  });

  it("PUBLISHED の場合は非公開ボタンを表示する", () => {
    render(
      <ProductDetailHeader productId="1" productName="Tシャツ" status="PUBLISHED" version={0} />,
    );

    expect(screen.getByRole("button", { name: "非公開にする" })).toBeTruthy();
    expect(screen.queryByRole("button", { name: "公開" })).toBeNull();
  });

  it("ARCHIVED の場合はアーカイブボタンを表示しない", () => {
    render(
      <ProductDetailHeader productId="1" productName="Tシャツ" status="ARCHIVED" version={0} />,
    );

    expect(screen.queryByRole("button", { name: "アーカイブ" })).toBeNull();
  });

  it("公開ボタン押下で publishProduct を呼ぶ", () => {
    render(<ProductDetailHeader productId="1" productName="Tシャツ" status="DRAFT" version={2} />);

    fireEvent.click(screen.getByRole("button", { name: "公開" }));

    expect(publishProductMock).toHaveBeenCalledWith(2);
  });

  it("削除ボタン押下で確認ダイアログを表示する", () => {
    render(<ProductDetailHeader productId="1" productName="Tシャツ" status="DRAFT" version={0} />);

    fireEvent.click(screen.getByRole("button", { name: "削除" }));

    expect(screen.getByText("商品を削除")).toBeTruthy();
  });

  it("削除確認で deleteProduct を呼ぶ", () => {
    render(<ProductDetailHeader productId="1" productName="Tシャツ" status="DRAFT" version={3} />);

    fireEvent.click(screen.getByRole("button", { name: "削除" }));
    fireEvent.click(screen.getByRole("button", { name: "削除する" }));

    expect(deleteProductMock).toHaveBeenCalledWith("1", 3);
  });
});
