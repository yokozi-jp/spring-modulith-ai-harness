import type { ReactNode } from "react";
import { act } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { renderHook, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vite-plus/test";
import { useCreatePricing } from "@/features/pricing/hooks/use-create-pricing";
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

describe("useCreatePricing", () => {
  const navigateMock = vi.fn();

  beforeEach(() => {
    vi.resetAllMocks();
    vi.mocked(tanstackRouter.useNavigate).mockReturnValue(navigateMock);
  });

  it("作成リクエストを正しい内容で送信する", async () => {
    vi.mocked(apiClient).mockResolvedValue({
      data: undefined,
      status: 201,
      headers: new Headers(),
    });

    const { result } = renderHook(() => useCreatePricing(), { wrapper: createWrapper() });

    act(() => {
      result.current.createPricing({
        productId: "p1",
        level: "REGION",
        areaCode: "01",
        amount: 1000,
        validFrom: "2025-01-01",
      });
    });

    await waitFor(() => {
      expect(apiClient).toHaveBeenCalledWith(
        "/api/v1/pricings",
        expect.objectContaining({ method: "POST" }),
      );
    });
  });

  it("作成成功時に一覧ページへ遷移する", async () => {
    vi.mocked(apiClient).mockResolvedValue({
      data: undefined,
      status: 201,
      headers: new Headers(),
    });

    const { result } = renderHook(() => useCreatePricing(), { wrapper: createWrapper() });

    act(() => {
      result.current.createPricing({
        productId: "p1",
        level: "REGION",
        areaCode: "01",
        amount: 1000,
        validFrom: "2025-01-01",
      });
    });

    await waitFor(() => {
      expect(navigateMock).toHaveBeenCalledWith({ to: "/pricings" });
    });
  });
});
