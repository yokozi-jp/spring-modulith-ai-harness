import { describe, expect, it, vi, beforeEach } from "vite-plus/test";
import { renderHook, act } from "@testing-library/react";
import { createElement } from "react";
import type { ReactNode } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useUpdatePricing } from "@/features/pricing/hooks/use-update-pricing";

vi.mock("@/api/pricing/pricing", () => ({
  useUpdate1: vi.fn(),
  getList1QueryKey: vi.fn(() => ["/api/v1/pricings"]),
  getFindById1QueryKey: vi.fn((id: string) => [`/api/v1/pricings/${id}`]),
}));

import { useUpdate1 } from "@/api/pricing/pricing";

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });

  return function Wrapper({ children }: { readonly children: ReactNode }) {
    return createElement(QueryClientProvider, { client: queryClient }, children);
  };
}

describe("useUpdatePricing", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it("updatePricing が mutation を呼び出す", () => {
    const mutateFn = vi.fn();
    vi.mocked(useUpdate1).mockReturnValue({
      mutate: mutateFn,
      isPending: false,
      isSuccess: false,
      error: null,
      reset: vi.fn(),
    } as unknown as ReturnType<typeof useUpdate1>);

    const { result } = renderHook(() => useUpdatePricing("pr-1"), {
      wrapper: createWrapper(),
    });

    act(() => {
      result.current.updatePricing({
        amount: 2000,
        validFrom: "2025-06-01T00:00:00.000Z",
        version: 1,
      });
    });

    expect(mutateFn).toHaveBeenCalled();
  });

  it("送信中は isUpdating が true", () => {
    vi.mocked(useUpdate1).mockReturnValue({
      mutate: vi.fn(),
      isPending: true,
      isSuccess: false,
      error: null,
      reset: vi.fn(),
    } as unknown as ReturnType<typeof useUpdate1>);

    const { result } = renderHook(() => useUpdatePricing("pr-1"), {
      wrapper: createWrapper(),
    });

    expect(result.current.isUpdating).toBe(true);
  });
});
