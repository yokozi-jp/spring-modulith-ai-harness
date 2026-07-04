import { describe, expect, it, vi, beforeEach } from "vite-plus/test";
import { renderHook, waitFor } from "@testing-library/react";
import { createElement } from "react";
import type { ReactNode } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useProductOptions } from "@/features/product/hooks/use-product-options";

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

describe("useProductOptions", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it("取得成功時に options を返す", async () => {
    vi.mocked(useList).mockReturnValue({
      data: {
        data: {
          content: [
            { id: "p-1", name: "商品A", status: "DRAFT", categoryId: "c-1" },
            { id: "p-2", name: "商品B", status: "PUBLISHED", categoryId: "c-1" },
          ],
        },
      },
      isLoading: false,
      error: null,
    } as unknown as ReturnType<typeof useList>);

    const { result } = renderHook(() => useProductOptions(), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.options).toEqual([
      { id: "p-1", name: "商品A" },
      { id: "p-2", name: "商品B" },
    ]);
  });
});
