import type { ReactNode } from "react";
import { act } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { renderHook, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vite-plus/test";
import { useUnpublishProduct } from "@/features/product/hooks/use-unpublish-product";
import { apiClient } from "@/lib/api-client";

vi.mock("@/lib/api-client");

function createWrapper() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return function Wrapper({ children }: { readonly children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  };
}

describe("useUnpublishProduct", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it("非公開リクエストを version 付きで送信する", async () => {
    vi.mocked(apiClient).mockResolvedValue({
      data: undefined,
      status: 200,
      headers: new Headers(),
    });

    const { result } = renderHook(() => useUnpublishProduct("1"), { wrapper: createWrapper() });

    act(() => {
      result.current.unpublishProduct(1);
    });

    await waitFor(() => {
      expect(apiClient).toHaveBeenCalledWith(
        "/api/v1/products/1/unpublish",
        expect.objectContaining({ method: "PATCH", body: JSON.stringify({ version: 1 }) }),
      );
    });
  });

  it("非公開失敗時に error を返す", async () => {
    vi.mocked(apiClient).mockRejectedValue(new Error("Conflict"));

    const { result } = renderHook(() => useUnpublishProduct("1"), { wrapper: createWrapper() });

    act(() => {
      result.current.unpublishProduct(1);
    });

    await waitFor(() => {
      expect(result.current.error).not.toBeNull();
    });
  });
});
