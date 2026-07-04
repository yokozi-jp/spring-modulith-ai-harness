import { describe, expect, it, vi, beforeEach } from "vite-plus/test";
import { renderHook, act } from "@testing-library/react";
import { createElement } from "react";
import type { ReactNode } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useDeletePricing } from "@/features/pricing/hooks/use-delete-pricing";

vi.mock("@/api/pricing/pricing", () => ({
  useDelete1: vi.fn(),
  getList1QueryKey: vi.fn(() => ["/api/v1/pricings"]),
}));

import { useDelete1 } from "@/api/pricing/pricing";

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });

  return function Wrapper({ children }: { readonly children: ReactNode }) {
    return createElement(QueryClientProvider, { client: queryClient }, children);
  };
}

describe("useDeletePricing", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it("deletePricing が mutation を呼び出す", () => {
    const mutateFn = vi.fn();
    vi.mocked(useDelete1).mockReturnValue({
      mutate: mutateFn,
      isPending: false,
      error: null,
    } as unknown as ReturnType<typeof useDelete1>);

    const { result } = renderHook(() => useDeletePricing(), {
      wrapper: createWrapper(),
    });

    act(() => {
      result.current.deletePricing("pr-1", 2);
    });

    expect(mutateFn).toHaveBeenCalledWith({
      id: "pr-1",
      data: { version: 2 },
    });
  });

  it("削除中は isDeleting が true", () => {
    vi.mocked(useDelete1).mockReturnValue({
      mutate: vi.fn(),
      isPending: true,
      error: null,
    } as unknown as ReturnType<typeof useDelete1>);

    const { result } = renderHook(() => useDeletePricing(), {
      wrapper: createWrapper(),
    });

    expect(result.current.isDeleting).toBe(true);
  });
});
