import type { ReactNode } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { renderHook, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vite-plus/test";
import { useProductOptions } from "@/features/pricing/hooks/use-product-options";
import { apiClient } from "@/lib/api-client";

vi.mock("@/lib/api-client");

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });

  return function Wrapper({ children }: { readonly children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  };
}

describe("useProductOptions", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it("商品一覧を選択肢として返す", async () => {
    vi.mocked(apiClient).mockResolvedValue({
      data: {
        content: [
          { id: "p1", name: "商品A", status: "PUBLISHED", categoryId: "c1" },
          { id: "p2", name: "商品B", status: "DRAFT", categoryId: "c2" },
        ],
      },
      status: 200,
      headers: new Headers(),
    });

    const { result } = renderHook(() => useProductOptions(), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.options).toEqual([
      { id: "p1", name: "商品A" },
      { id: "p2", name: "商品B" },
    ]);
  });
});
