import type { ReactNode } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { renderHook, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vite-plus/test";
import { useCategoryList } from "@/features/category/hooks/use-category-list";
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

describe("useCategoryList", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it("初期状態で isLoading が true", () => {
    vi.mocked(apiClient).mockReturnValue(
      new Promise(() => {
        // 意図的に resolve/reject しない: pending 状態を維持するためのモック
      }),
    );

    const { result } = renderHook(() => useCategoryList(), { wrapper: createWrapper() });

    expect(result.current.isLoading).toBe(true);
    expect(result.current.categories).toEqual([]);
  });

  it("取得成功時に categories を返す", async () => {
    vi.mocked(apiClient).mockResolvedValue({
      data: { content: [{ id: "1", name: "衣料品", sortOrder: 1 }] },
      status: 200,
      headers: new Headers(),
    });

    const { result } = renderHook(() => useCategoryList(), { wrapper: createWrapper() });

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.categories).toEqual([{ id: "1", name: "衣料品", sortOrder: 1 }]);
  });

  it("取得失敗時に error を返す", async () => {
    vi.mocked(apiClient).mockRejectedValue(new Error("API Error"));

    const { result } = renderHook(() => useCategoryList(), { wrapper: createWrapper() });

    await waitFor(() => {
      expect(result.current.error).not.toBeNull();
    });
  });
});
