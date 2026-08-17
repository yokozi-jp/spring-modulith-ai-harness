import type { ReactNode } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { renderHook, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vite-plus/test";
import { useProduct } from "@/features/product/hooks/use-product";
import { apiClient } from "@/lib/api-client";

vi.mock("@/lib/api-client");

function createWrapper() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return function Wrapper({ children }: { readonly children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  };
}

describe("useProduct", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it("id が空の場合はリクエストしない", () => {
    vi.mocked(apiClient).mockResolvedValue({
      data: undefined,
      status: 200,
      headers: new Headers(),
    });

    const { result } = renderHook(() => useProduct(""), { wrapper: createWrapper() });

    expect(result.current.isLoading).toBe(false);
    expect(result.current.product).toBeNull();
  });

  it("取得成功時に product を返す", async () => {
    vi.mocked(apiClient).mockResolvedValue({
      data: {
        id: "1",
        name: "Tシャツ",
        description: "説明",
        categoryId: "cat-1",
        sku: "SKU-1",
        status: "DRAFT",
        version: 0,
      },
      status: 200,
      headers: new Headers(),
    });

    const { result } = renderHook(() => useProduct("1"), { wrapper: createWrapper() });

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.product).toEqual({
      id: "1",
      name: "Tシャツ",
      description: "説明",
      categoryId: "cat-1",
      sku: "SKU-1",
      status: "DRAFT",
      version: 0,
    });
  });

  it("取得失敗時に error を返す", async () => {
    vi.mocked(apiClient).mockRejectedValue(new Error("Not Found"));

    const { result } = renderHook(() => useProduct("1"), { wrapper: createWrapper() });

    await waitFor(() => {
      expect(result.current.error).not.toBeNull();
    });
  });
});
