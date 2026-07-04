import { describe, expect, it, vi, beforeEach } from "vite-plus/test";
import { renderHook, act } from "@testing-library/react";
import { createElement } from "react";
import type { ReactNode } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useProductStatus } from "@/features/product/hooks/use-product-status";

vi.mock("@/api/product/product", () => ({
  usePublish: vi.fn(),
  useUnpublish: vi.fn(),
  useArchive: vi.fn(),
  getListQueryKey: vi.fn(() => ["/api/v1/products"]),
  getFindByIdQueryKey: vi.fn((id: string) => [`/api/v1/products/${id}`]),
}));

import { usePublish, useUnpublish, useArchive } from "@/api/product/product";

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });

  return function Wrapper({ children }: { readonly children: ReactNode }) {
    return createElement(QueryClientProvider, { client: queryClient }, children);
  };
}

describe("useProductStatus", () => {
  beforeEach(() => {
    vi.resetAllMocks();
    const baseMock = { mutate: vi.fn(), isPending: false, error: null };
    vi.mocked(usePublish).mockReturnValue(baseMock as unknown as ReturnType<typeof usePublish>);
    vi.mocked(useUnpublish).mockReturnValue(baseMock as unknown as ReturnType<typeof useUnpublish>);
    vi.mocked(useArchive).mockReturnValue(baseMock as unknown as ReturnType<typeof useArchive>);
  });

  it("publishProduct が publish mutation を呼び出す", () => {
    const mutateFn = vi.fn();
    vi.mocked(usePublish).mockReturnValue({
      mutate: mutateFn,
      isPending: false,
      error: null,
    } as unknown as ReturnType<typeof usePublish>);

    const { result } = renderHook(() => useProductStatus("prod-1"), {
      wrapper: createWrapper(),
    });

    act(() => {
      result.current.publishProduct(1);
    });

    expect(mutateFn).toHaveBeenCalledWith({
      id: "prod-1",
      data: { version: 1 },
    });
  });

  it("isPending が true のとき isPending を返す", () => {
    vi.mocked(usePublish).mockReturnValue({
      mutate: vi.fn(),
      isPending: true,
      error: null,
    } as unknown as ReturnType<typeof usePublish>);

    const { result } = renderHook(() => useProductStatus("prod-1"), {
      wrapper: createWrapper(),
    });

    expect(result.current.isPending).toBe(true);
  });
});
