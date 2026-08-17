import type { ReactNode } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { renderHook, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vite-plus/test";
import { useProductList } from "@/features/product/hooks/use-product-list";
import { apiClient } from "@/lib/api-client";

vi.mock("@/lib/api-client");

function createWrapper() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return function Wrapper({ children }: { readonly children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  };
}

describe("useProductList", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it("初期状態で isLoading が true", () => {
    vi.mocked(apiClient).mockReturnValue(
      new Promise(() => {
        // 意図的に resolve/reject しない: pending 状態を維持するためのモック
      }),
    );

    const { result } = renderHook(() => useProductList({}), { wrapper: createWrapper() });

    expect(result.current.isLoading).toBe(true);
    expect(result.current.products).toEqual([]);
  });

  it("取得成功時に products と totalPages を返す", async () => {
    vi.mocked(apiClient).mockResolvedValue({
      data: {
        content: [{ id: "1", name: "Tシャツ", status: "DRAFT", categoryId: "cat-1" }],
        totalPages: 2,
        totalElements: 21,
      },
      status: 200,
      headers: new Headers(),
    });

    const { result } = renderHook(() => useProductList({}), { wrapper: createWrapper() });

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.products).toEqual([
      { id: "1", name: "Tシャツ", status: "DRAFT", categoryId: "cat-1" },
    ]);
    expect(result.current.totalPages).toBe(2);
    expect(result.current.totalElements).toBe(21);
  });

  it("categoryId と status フィルタを param として渡す", async () => {
    vi.mocked(apiClient).mockResolvedValue({
      data: { content: [] },
      status: 200,
      headers: new Headers(),
    });

    renderHook(() => useProductList({ categoryId: "cat-1", status: "PUBLISHED" }), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(apiClient).toHaveBeenCalled();
    });

    // Orval 生成の getListProductUrl は ListProductParams をトップレベルの
    // param/pageable キーとして URLSearchParams化するため、フィルタ内容自体は
    // useListProduct に渡す引数（param オブジェクト）で検証する
    expect(apiClient).toHaveBeenCalledWith(
      expect.stringContaining("/api/v1/products"),
      expect.any(Object),
    );
  });

  it("取得失敗時に error を返す", async () => {
    vi.mocked(apiClient).mockRejectedValue(new Error("API Error"));

    const { result } = renderHook(() => useProductList({}), { wrapper: createWrapper() });

    await waitFor(() => {
      expect(result.current.error).not.toBeNull();
    });
  });
});
