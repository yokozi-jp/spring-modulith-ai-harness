import { describe, expect, it, vi, beforeEach } from "vite-plus/test";
import { renderHook, act } from "@testing-library/react";
import { createElement } from "react";
import type { ReactNode } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useDeleteProduct } from "@/features/product/hooks/use-delete-product";

vi.mock("@tanstack/react-router", () => ({
  useNavigate: () => vi.fn(),
}));

vi.mock("@/api/product/product", () => ({
  useDelete: vi.fn(),
  getListQueryKey: vi.fn(() => ["/api/v1/products"]),
}));

import { useDelete } from "@/api/product/product";

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });

  return function Wrapper({ children }: { readonly children: ReactNode }) {
    return createElement(QueryClientProvider, { client: queryClient }, children);
  };
}

describe("useDeleteProduct", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it("deleteProduct が mutation を呼び出す", () => {
    const mutateFn = vi.fn();
    vi.mocked(useDelete).mockReturnValue({
      mutate: mutateFn,
      isPending: false,
      error: null,
    } as unknown as ReturnType<typeof useDelete>);

    const { result } = renderHook(() => useDeleteProduct(), {
      wrapper: createWrapper(),
    });

    act(() => {
      result.current.deleteProduct("prod-1", 2);
    });

    expect(mutateFn).toHaveBeenCalledWith({
      id: "prod-1",
      data: { version: 2 },
    });
  });

  it("削除中は isDeleting が true", () => {
    vi.mocked(useDelete).mockReturnValue({
      mutate: vi.fn(),
      isPending: true,
      error: null,
    } as unknown as ReturnType<typeof useDelete>);

    const { result } = renderHook(() => useDeleteProduct(), {
      wrapper: createWrapper(),
    });

    expect(result.current.isDeleting).toBe(true);
  });
});
