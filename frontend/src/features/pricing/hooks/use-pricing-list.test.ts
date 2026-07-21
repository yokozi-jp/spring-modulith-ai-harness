import { describe, expect, it, vi, beforeEach } from "vite-plus/test";
import { renderHook, waitFor } from "@testing-library/react";
import { createElement } from "react";
import type { ReactNode } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { usePricingList } from "@/features/pricing/hooks/use-pricing-list";

vi.mock("@/api/pricing/pricing", () => ({
  useList1: vi.fn(),
}));

import { useList1 } from "@/api/pricing/pricing";

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });

  return function Wrapper({ children }: { readonly children: ReactNode }) {
    return createElement(QueryClientProvider, { client: queryClient }, children);
  };
}

describe("usePricingList", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it("取得成功時に pricings を返す", async () => {
    const mockPricings = [
      { id: "1", productId: "p-1", level: "REGION", areaCode: "JP", amount: 1000 },
    ];

    vi.mocked(useList1).mockReturnValue({
      data: { data: { content: mockPricings, totalPages: 1, totalElements: 1 } },
      isLoading: false,
      error: null,
    } as unknown as ReturnType<typeof useList1>);

    const { result } = renderHook(() => usePricingList(), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.pricings).toEqual(mockPricings);
  });

  it("ローディング中は isLoading が true", () => {
    vi.mocked(useList1).mockReturnValue({
      data: undefined,
      isLoading: true,
      error: null,
    } as unknown as ReturnType<typeof useList1>);

    const { result } = renderHook(() => usePricingList(), {
      wrapper: createWrapper(),
    });

    expect(result.current.isLoading).toBe(true);
    expect(result.current.pricings).toEqual([]);
  });
});
