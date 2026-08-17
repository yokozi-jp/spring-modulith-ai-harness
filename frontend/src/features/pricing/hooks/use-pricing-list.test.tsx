import type { ReactNode } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { renderHook, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vite-plus/test";
import { usePricingList } from "@/features/pricing/hooks/use-pricing-list";
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

describe("usePricingList", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it("初期状態で isLoading が true", () => {
    vi.mocked(apiClient).mockReturnValue(
      new Promise(() => {
        // pending 状態を維持
      }),
    );

    const { result } = renderHook(() => usePricingList({}), {
      wrapper: createWrapper(),
    });

    expect(result.current.isLoading).toBe(true);
    expect(result.current.pricings).toEqual([]);
  });

  it("取得成功時に pricings を返す", async () => {
    vi.mocked(apiClient).mockResolvedValue({
      data: {
        content: [{ id: "1", productId: "p1", level: "REGION", areaCode: "01", amount: 1000 }],
      },
      status: 200,
      headers: new Headers(),
    });

    const { result } = renderHook(() => usePricingList({}), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.pricings).toEqual([
      { id: "1", productId: "p1", level: "REGION", areaCode: "01", amount: 1000 },
    ]);
  });

  it("取得失敗時に error を返す", async () => {
    vi.mocked(apiClient).mockRejectedValue(new Error("API Error"));

    const { result } = renderHook(() => usePricingList({}), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.error).not.toBeNull();
    });
  });
});
