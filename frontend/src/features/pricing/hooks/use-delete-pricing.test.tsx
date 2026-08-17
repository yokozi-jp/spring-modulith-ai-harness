import type { ReactNode } from "react";
import { act } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { renderHook, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vite-plus/test";
import { useDeletePricing } from "@/features/pricing/hooks/use-delete-pricing";
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

describe("useDeletePricing", () => {
  const navigateMock = vi.fn();

  beforeEach(() => {
    vi.resetAllMocks();
    vi.mocked(tanstackRouter.useNavigate).mockReturnValue(navigateMock);
  });

  it("削除リクエストを正しい内容で送信する", async () => {
    vi.mocked(apiClient).mockResolvedValue({
      data: undefined,
      status: 204,
      headers: new Headers(),
    });

    const { result } = renderHook(() => useDeletePricing(), { wrapper: createWrapper() });

    act(() => {
      result.current.deletePricing("pricing-1", 0);
    });

    await waitFor(() => {
      expect(apiClient).toHaveBeenCalledWith(
        "/api/v1/pricings/pricing-1",
        expect.objectContaining({ method: "DELETE" }),
      );
    });
  });

  it("削除成功時に一覧ページへ遷移する", async () => {
    vi.mocked(apiClient).mockResolvedValue({
      data: undefined,
      status: 204,
      headers: new Headers(),
    });

    const { result } = renderHook(() => useDeletePricing(), { wrapper: createWrapper() });

    act(() => {
      result.current.deletePricing("pricing-1", 0);
    });

    await waitFor(() => {
      expect(navigateMock).toHaveBeenCalledWith({ to: "/pricings" });
    });
  });
});
