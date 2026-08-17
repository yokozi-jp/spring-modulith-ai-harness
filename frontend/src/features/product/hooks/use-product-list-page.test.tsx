import type { ReactNode } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { act, renderHook, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vite-plus/test";
import { useProductListPage } from "@/features/product/hooks/use-product-list-page";
import { apiClient } from "@/lib/api-client";

vi.mock("@/lib/api-client");

function createWrapper() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return function Wrapper({ children }: { readonly children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  };
}

describe("useProductListPage", () => {
  beforeEach(() => {
    vi.resetAllMocks();
    vi.mocked(apiClient).mockResolvedValue({
      data: { content: [], totalPages: 0, totalElements: 0 },
      status: 200,
      headers: new Headers(),
    });
  });

  it("初期状態でフィルタが未設定、ページが0", () => {
    const { result } = renderHook(() => useProductListPage(), { wrapper: createWrapper() });

    expect(result.current.filter).toEqual({ categoryId: undefined, status: undefined });
    expect(result.current.page).toBe(0);
  });

  it("フィルタ変更時にページを0にリセットする", async () => {
    const { result } = renderHook(() => useProductListPage(), { wrapper: createWrapper() });

    act(() => {
      result.current.onPageChange(2);
    });
    expect(result.current.page).toBe(2);

    act(() => {
      result.current.onFilterChange({ categoryId: "cat-1", status: undefined });
    });

    await waitFor(() => {
      expect(result.current.page).toBe(0);
    });
    expect(result.current.filter).toEqual({ categoryId: "cat-1", status: undefined });
  });
});
