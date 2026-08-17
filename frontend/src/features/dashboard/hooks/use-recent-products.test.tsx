import type { ReactNode } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { renderHook, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vite-plus/test";
import { useRecentProducts } from "@/features/dashboard/hooks/use-recent-products";
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

describe("useRecentProducts", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it("最近の商品を5件まで取得する", async () => {
    vi.mocked(apiClient).mockResolvedValue({
      data: {
        content: [
          { id: "1", name: "商品A", status: "DRAFT", categoryId: "c1" },
          { id: "2", name: "商品B", status: "PUBLISHED", categoryId: "c2" },
        ],
      },
      status: 200,
      headers: new Headers(),
    });

    const { result } = renderHook(() => useRecentProducts(), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.products).toHaveLength(2);
  });
});
