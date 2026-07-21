import { describe, expect, it, vi, beforeEach } from "vite-plus/test";
import { renderHook, act } from "@testing-library/react";
import { createElement } from "react";
import type { ReactNode } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useCreateProduct } from "@/features/product/hooks/use-create-product";

vi.mock("@tanstack/react-router", () => ({
  useNavigate: () => vi.fn(),
}));

vi.mock("@/api/product/product", () => ({
  useCreate: vi.fn(),
  getListQueryKey: vi.fn(() => ["/api/v1/products"]),
}));

import { useCreate } from "@/api/product/product";

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });

  return function Wrapper({ children }: { readonly children: ReactNode }) {
    return createElement(QueryClientProvider, { client: queryClient }, children);
  };
}

describe("useCreateProduct", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it("createProduct が mutation を呼び出す", () => {
    const mutateFn = vi.fn();
    vi.mocked(useCreate).mockReturnValue({
      mutate: mutateFn,
      isPending: false,
      error: null,
    } as unknown as ReturnType<typeof useCreate>);

    const { result } = renderHook(() => useCreateProduct(), {
      wrapper: createWrapper(),
    });

    act(() => {
      result.current.createProduct({
        name: "新商品",
        description: "説明",
        categoryId: "cat-1",
        sku: "SKU-NEW",
      });
    });

    expect(mutateFn).toHaveBeenCalledWith({
      data: {
        name: "新商品",
        description: "説明",
        categoryId: "cat-1",
        sku: "SKU-NEW",
      },
    });
  });

  it("送信中は isCreating が true", () => {
    vi.mocked(useCreate).mockReturnValue({
      mutate: vi.fn(),
      isPending: true,
      error: null,
    } as unknown as ReturnType<typeof useCreate>);

    const { result } = renderHook(() => useCreateProduct(), {
      wrapper: createWrapper(),
    });

    expect(result.current.isCreating).toBe(true);
  });
});
