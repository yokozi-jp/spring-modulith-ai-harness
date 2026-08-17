import type { ReactNode } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { renderHook, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vite-plus/test";
import { useRecentPricings } from "@/features/dashboard/hooks/use-recent-pricings";
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

describe("useRecentPricings", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it("最近の価格を5件まで取得する", async () => {
    vi.mocked(apiClient).mockResolvedValue({
      data: {
        content: [{ id: "1", productId: "p1", level: "REGION", areaCode: "01", amount: 1000 }],
      },
      status: 200,
      headers: new Headers(),
    });

    const { result } = renderHook(() => useRecentPricings(), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.pricings).toHaveLength(1);
  });
});
