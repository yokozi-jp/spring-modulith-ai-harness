import { describe, expect, it, vi, beforeEach } from "vite-plus/test";
import { renderHook, act } from "@testing-library/react";
import { createElement } from "react";
import type { ReactNode } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useCreatePricing } from "@/features/pricing/hooks/use-create-pricing";

vi.mock("@/api/pricing/pricing", () => ({
  useCreate1: vi.fn(),
  getList1QueryKey: vi.fn(() => ["/api/v1/pricings"]),
}));

import { useCreate1 } from "@/api/pricing/pricing";

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });

  return function Wrapper({ children }: { readonly children: ReactNode }) {
    return createElement(QueryClientProvider, { client: queryClient }, children);
  };
}

describe("useCreatePricing", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it("createPricing が mutation を呼び出す", () => {
    const mutateFn = vi.fn();
    vi.mocked(useCreate1).mockReturnValue({
      mutate: mutateFn,
      isPending: false,
      isSuccess: false,
      error: null,
      reset: vi.fn(),
    } as unknown as ReturnType<typeof useCreate1>);

    const { result } = renderHook(() => useCreatePricing(), {
      wrapper: createWrapper(),
    });

    act(() => {
      result.current.createPricing({
        productId: "p-1",
        level: "REGION",
        areaCode: "JP",
        amount: 1000,
        validFrom: "2025-01-01T00:00:00.000Z",
      });
    });

    expect(mutateFn).toHaveBeenCalled();
  });

  it("送信中は isCreating が true", () => {
    vi.mocked(useCreate1).mockReturnValue({
      mutate: vi.fn(),
      isPending: true,
      isSuccess: false,
      error: null,
      reset: vi.fn(),
    } as unknown as ReturnType<typeof useCreate1>);

    const { result } = renderHook(() => useCreatePricing(), {
      wrapper: createWrapper(),
    });

    expect(result.current.isCreating).toBe(true);
  });
});
