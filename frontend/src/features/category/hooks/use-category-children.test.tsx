import type { ReactNode } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { renderHook, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vite-plus/test";
import { useCategoryChildren } from "@/features/category/hooks/use-category-children";
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

describe("useCategoryChildren", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it("id が空の場合はリクエストしない", () => {
    vi.mocked(apiClient).mockResolvedValue({ data: [], status: 200, headers: new Headers() });

    const { result } = renderHook(() => useCategoryChildren(""), { wrapper: createWrapper() });

    expect(result.current.isLoading).toBe(false);
    expect(result.current.children).toEqual([]);
  });

  it("取得成功時に children を返す", async () => {
    vi.mocked(apiClient).mockResolvedValue({
      data: [{ id: "2", name: "トップス", sortOrder: 1 }],
      status: 200,
      headers: new Headers(),
    });

    const { result } = renderHook(() => useCategoryChildren("1"), { wrapper: createWrapper() });

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.children).toEqual([{ id: "2", name: "トップス", sortOrder: 1 }]);
  });

  it("取得失敗時に error を返す", async () => {
    vi.mocked(apiClient).mockRejectedValue(new Error("API Error"));

    const { result } = renderHook(() => useCategoryChildren("1"), { wrapper: createWrapper() });

    await waitFor(() => {
      expect(result.current.error).not.toBeNull();
    });
  });
});
