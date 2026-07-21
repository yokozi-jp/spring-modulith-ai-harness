import { describe, expect, it, vi, beforeEach } from "vite-plus/test";
import { renderHook } from "@testing-library/react";
import { createElement } from "react";
import type { ReactNode } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { usePricing } from "@/features/pricing/hooks/use-pricing";

vi.mock("@/api/pricing/pricing", () => ({
  useFindById1: vi.fn(),
}));

import { useFindById1 } from "@/api/pricing/pricing";

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });

  return function Wrapper({ children }: { readonly children: ReactNode }) {
    return createElement(QueryClientProvider, { client: queryClient }, children);
  };
}

describe("usePricing", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it("isLoading が true のとき pricing は null", () => {
    vi.mocked(useFindById1).mockReturnValue({
      data: undefined,
      isLoading: true,
      error: null,
    } as unknown as ReturnType<typeof useFindById1>);

    const { result } = renderHook(() => usePricing("pr-1"), {
      wrapper: createWrapper(),
    });

    expect(result.current.isLoading).toBe(true);
    expect(result.current.pricing).toBeNull();
  });

  it("取得成功時に pricing を返す", () => {
    vi.mocked(useFindById1).mockReturnValue({
      data: {
        data: {
          id: "pr-1",
          productId: "p-1",
          level: "REGION",
          areaCode: "JP-13",
          amount: 1500,
          validFrom: "2025-01-01T00:00:00Z",
          validTo: "2025-12-31T23:59:59Z",
          version: 0,
        },
        status: 200,
        headers: new Headers(),
      },
      isLoading: false,
      error: null,
    } as unknown as ReturnType<typeof useFindById1>);

    const { result } = renderHook(() => usePricing("pr-1"), {
      wrapper: createWrapper(),
    });

    expect(result.current.pricing).toEqual({
      id: "pr-1",
      productId: "p-1",
      level: "REGION",
      areaCode: "JP-13",
      amount: 1500,
      validFrom: "2025-01-01T00:00:00Z",
      validTo: "2025-12-31T23:59:59Z",
      version: 0,
    });
  });
});
