import type { ReactNode } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { renderHook, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vite-plus/test";
import { usePricing } from "@/features/pricing/hooks/use-pricing";
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

describe("usePricing", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it("初期状態で isLoading が true", () => {
    vi.mocked(apiClient).mockReturnValue(
      new Promise(() => {
        // pending 状態を維持
      }),
    );

    const { result } = renderHook(() => usePricing("pricing-1"), {
      wrapper: createWrapper(),
    });

    expect(result.current.isLoading).toBe(true);
    expect(result.current.pricing).toBeNull();
  });

  it("取得成功時に pricing を返す", async () => {
    vi.mocked(apiClient).mockResolvedValue({
      data: {
        id: "pricing-1",
        productId: "p1",
        level: "REGION",
        areaCode: "01",
        amount: 1000,
        validFrom: "2025-01-01",
        validTo: null,
        version: 0,
      },
      status: 200,
      headers: new Headers(),
    });

    const { result } = renderHook(() => usePricing("pricing-1"), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.pricing?.id).toBe("pricing-1");
    expect(result.current.pricing?.amount).toBe(1000);
  });

  it("id が空文字の場合は実行しない", () => {
    const { result } = renderHook(() => usePricing(""), {
      wrapper: createWrapper(),
    });

    expect(result.current.isLoading).toBe(false);
    expect(result.current.pricing).toBeNull();
  });
});
