import type { ReactNode } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { renderHook, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vite-plus/test";
import { useCategory } from "@/features/category/hooks/use-category";
import { apiClient } from "@/lib/api-client";

vi.mock("@/lib/api-client");

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });

  return function Wrapper({ children }: { readonly children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  };
}

describe("useCategory", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it("id が空の場合はリクエストしない", () => {
    vi.mocked(apiClient).mockResolvedValue({
      data: undefined,
      status: 200,
      headers: new Headers(),
    });

    const { result } = renderHook(() => useCategory(""), { wrapper: createWrapper() });

    expect(result.current.isLoading).toBe(false);
    expect(result.current.category).toBeNull();
  });

  it("取得成功時に category を返す", async () => {
    vi.mocked(apiClient).mockResolvedValue({
      data: {
        id: "1",
        name: "衣料品",
        sortOrder: 1,
        version: 0,
        ancestors: [],
      },
      status: 200,
      headers: new Headers(),
    });

    const { result } = renderHook(() => useCategory("1"), { wrapper: createWrapper() });

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.category).toEqual({
      id: "1",
      name: "衣料品",
      sortOrder: 1,
      version: 0,
      ancestors: [],
    });
  });

  it("取得失敗時に error を返す", async () => {
    vi.mocked(apiClient).mockRejectedValue(new Error("Not Found"));

    const { result } = renderHook(() => useCategory("1"), { wrapper: createWrapper() });

    await waitFor(() => {
      expect(result.current.error).not.toBeNull();
    });
  });
});
