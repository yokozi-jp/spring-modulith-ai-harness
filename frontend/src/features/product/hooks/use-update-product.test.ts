import { describe, expect, it, vi, beforeEach } from "vite-plus/test";
import { renderHook, act } from "@testing-library/react";
import { createElement } from "react";
import type { ReactNode } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useUpdateProduct } from "@/features/product/hooks/use-update-product";

vi.mock("@tanstack/react-router", () => ({
  useNavigate: () => vi.fn(),
}));

vi.mock("@/api/product/product", () => ({
  useUpdate: vi.fn(),
  getListQueryKey: vi.fn(() => ["/api/v1/products"]),
  getFindByIdQueryKey: vi.fn((id: string) => [`/api/v1/products/${id}`]),
}));

import { useUpdate } from "@/api/product/product";

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });

  return function Wrapper({ children }: { readonly children: ReactNode }) {
    return createElement(QueryClientProvider, { client: queryClient }, children);
  };
}

describe("useUpdateProduct", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it("updateProduct が mutation を呼び出す", () => {
    const mutateFn = vi.fn();
    vi.mocked(useUpdate).mockReturnValue({
      mutate: mutateFn,
      isPending: false,
      error: null,
    } as unknown as ReturnType<typeof useUpdate>);

    const { result } = renderHook(() => useUpdateProduct("prod-1"), {
      wrapper: createWrapper(),
    });

    act(() => {
      result.current.updateProduct({
        name: "更新商品",
        description: "新説明",
        categoryId: "cat-2",
        version: 1,
      });
    });

    expect(mutateFn).toHaveBeenCalledWith({
      id: "prod-1",
      data: {
        name: "更新商品",
        description: "新説明",
        categoryId: "cat-2",
        version: 1,
      },
    });
  });

  it("送信中は isUpdating が true", () => {
    vi.mocked(useUpdate).mockReturnValue({
      mutate: vi.fn(),
      isPending: true,
      error: null,
    } as unknown as ReturnType<typeof useUpdate>);

    const { result } = renderHook(() => useUpdateProduct("prod-1"), {
      wrapper: createWrapper(),
    });

    expect(result.current.isUpdating).toBe(true);
  });
});
