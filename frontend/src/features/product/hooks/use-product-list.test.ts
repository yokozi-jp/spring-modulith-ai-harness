import { describe, expect, it, vi, beforeEach } from "vite-plus/test";
import { renderHook, waitFor } from "@testing-library/react";
import { createElement } from "react";
import type { ReactNode } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useProductList } from "@/features/product/hooks/use-product-list";

vi.mock("@/api/product/product", () => ({
  useList: vi.fn(),
}));

import { useList } from "@/api/product/product";

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });

  return function Wrapper({ children }: { readonly children: ReactNode }) {
    return createElement(QueryClientProvider, { client: queryClient }, children);
  };
}

describe("useProductList", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it("取得成功時に products を返す", async () => {
    const mockProducts = [{ id: "1", name: "商品A", status: "DRAFT", categoryId: "cat-1" }];

    vi.mocked(useList).mockReturnValue({
      data: { data: { content: mockProducts, totalPages: 1, totalElements: 1 } },
      isLoading: false,
      error: null,
    } as unknown as ReturnType<typeof useList>);

    const { result } = renderHook(() => useProductList(), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.products).toEqual(mockProducts);
  });

  it("ローディング中は isLoading が true", () => {
    vi.mocked(useList).mockReturnValue({
      data: undefined,
      isLoading: true,
      error: null,
    } as unknown as ReturnType<typeof useList>);

    const { result } = renderHook(() => useProductList(), {
      wrapper: createWrapper(),
    });

    expect(result.current.isLoading).toBe(true);
    expect(result.current.products).toEqual([]);
  });
});
