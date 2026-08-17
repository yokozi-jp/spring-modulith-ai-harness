import type { ReactNode } from "react";
import { act } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { renderHook, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vite-plus/test";
import { useUpdatePricing } from "@/features/pricing/hooks/use-update-pricing";
import { apiClient } from "@/lib/api-client";
import * as tanstackRouter from "@tanstack/react-router";

vi.mock("@/lib/api-client");
vi.mock("@tanstack/react-router", async () => {
  const actual = await vi.importActual("@tanstack/react-router");
  return { ...actual, useNavigate: vi.fn() };
});

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });

  return function Wrapper({ children }: { readonly children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  };
}

describe("useUpdatePricing", () => {
  const navigateMock = vi.fn();

  beforeEach(() => {
    vi.resetAllMocks();
    vi.mocked(tanstackRouter.useNavigate).mockReturnValue(navigateMock);
  });

  it("更新リクエストを正しい内容で送信する", async () => {
    vi.mocked(apiClient).mockResolvedValue({
      data: undefined,
      status: 200,
      headers: new Headers(),
    });

    const { result } = renderHook(() => useUpdatePricing("pricing-1"), {
      wrapper: createWrapper(),
    });

    act(() => {
      result.current.updatePricing({
        amount: 2000,
        validFrom: "2025-04-01",
        version: 1,
      });
    });

    await waitFor(() => {
      expect(apiClient).toHaveBeenCalledWith(
        "/api/v1/pricings/pricing-1",
        expect.objectContaining({ method: "PUT" }),
      );
    });
  });

  it("更新成功時に詳細ページへ遷移する", async () => {
    vi.mocked(apiClient).mockResolvedValue({
      data: undefined,
      status: 200,
      headers: new Headers(),
    });

    const { result } = renderHook(() => useUpdatePricing("pricing-1"), {
      wrapper: createWrapper(),
    });

    act(() => {
      result.current.updatePricing({
        amount: 2000,
        validFrom: "2025-04-01",
        version: 1,
      });
    });

    await waitFor(() => {
      expect(navigateMock).toHaveBeenCalledWith({
        to: "/pricings/$id",
        params: { id: "pricing-1" },
      });
    });
  });
});
