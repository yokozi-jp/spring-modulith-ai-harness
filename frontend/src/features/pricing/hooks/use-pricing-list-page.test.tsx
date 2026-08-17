import type { ReactNode } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { renderHook, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vite-plus/test";
import { usePricingListPage } from "@/features/pricing/hooks/use-pricing-list-page";
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

describe("usePricingListPage", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it("初期状態で page が 0", () => {
    vi.mocked(apiClient).mockReturnValue(
      new Promise(() => {
        // pending 状態を維持
      }),
    );

    const { result } = renderHook(() => usePricingListPage(), {
      wrapper: createWrapper(),
    });

    expect(result.current.page).toBe(0);
    expect(result.current.filter).toEqual({ productId: undefined, level: undefined });
  });

  it("データ取得成功時に pricings を返す", async () => {
    vi.mocked(apiClient).mockResolvedValue({
      data: {
        content: [{ id: "1", productId: "p1", level: "REGION", areaCode: "01", amount: 500 }],
        totalPages: 1,
      },
      status: 200,
      headers: new Headers(),
    });

    const { result } = renderHook(() => usePricingListPage(), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.pricings).toHaveLength(1);
  });
});
