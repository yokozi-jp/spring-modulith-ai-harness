import { describe, expect, it, vi, beforeEach } from "vite-plus/test";
import { renderHook } from "@testing-library/react";
import { createElement } from "react";
import type { ReactNode } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useProduct } from "@/features/product/hooks/use-product";

vi.mock("@/api/product/product", () => ({
  useFindById: vi.fn(),
  getFindByIdQueryKey: vi.fn((id: string) => [`/api/v1/products/${id}`]),
}));

import { useFindById } from "@/api/product/product";

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });

  return function Wrapper({ children }: { readonly children: ReactNode }) {
    return createElement(QueryClientProvider, { client: queryClient }, children);
  };
}

describe("useProduct", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it("isLoading が true のとき product は null", () => {
    vi.mocked(useFindById).mockReturnValue({
      data: undefined,
      isLoading: true,
      error: null,
      refetch: vi.fn(),
    } as unknown as ReturnType<typeof useFindById>);

    const { result } = renderHook(() => useProduct("prod-1"), {
      wrapper: createWrapper(),
    });

    expect(result.current.isLoading).toBe(true);
    expect(result.current.product).toBeNull();
  });

  it("取得成功時に product を返す", () => {
    vi.mocked(useFindById).mockReturnValue({
      data: {
        data: {
          id: "prod-1",
          name: "テスト商品",
          description: "説明",
          categoryId: "cat-1",
          sku: "SKU-001",
          status: "DRAFT",
          version: 0,
        },
        status: 200,
        headers: new Headers(),
      },
      isLoading: false,
      error: null,
      refetch: vi.fn(),
    } as unknown as ReturnType<typeof useFindById>);

    const { result } = renderHook(() => useProduct("prod-1"), {
      wrapper: createWrapper(),
    });

    expect(result.current.product).toEqual({
      id: "prod-1",
      name: "テスト商品",
      description: "説明",
      categoryId: "cat-1",
      sku: "SKU-001",
      status: "DRAFT",
      version: 0,
    });
  });
});
