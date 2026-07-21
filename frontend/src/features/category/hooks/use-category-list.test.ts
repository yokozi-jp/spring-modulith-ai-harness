import { describe, expect, it, vi, beforeEach } from "vite-plus/test";
import { renderHook, waitFor } from "@testing-library/react";
import { createElement } from "react";
import type { ReactNode } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useCategoryList } from "@/features/category/hooks/use-category-list";

vi.mock("@/api/category/category", () => ({
  useList2: vi.fn(),
}));

import { useList2 } from "@/api/category/category";

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });

  return function Wrapper({ children }: { readonly children: ReactNode }) {
    return createElement(QueryClientProvider, { client: queryClient }, children);
  };
}

describe("useCategoryList", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it("取得成功時に categories を返す", async () => {
    const mockCategories = [
      { id: "1", name: "カテゴリA", sortOrder: 0 },
      { id: "2", name: "カテゴリB", sortOrder: 1 },
    ];

    vi.mocked(useList2).mockReturnValue({
      data: { data: { content: mockCategories, totalPages: 1, totalElements: 2 } },
      isLoading: false,
      error: null,
    } as unknown as ReturnType<typeof useList2>);

    const { result } = renderHook(() => useCategoryList(), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.categories).toEqual(mockCategories);
  });

  it("ローディング中は isLoading が true", () => {
    vi.mocked(useList2).mockReturnValue({
      data: undefined,
      isLoading: true,
      error: null,
    } as unknown as ReturnType<typeof useList2>);

    const { result } = renderHook(() => useCategoryList(), {
      wrapper: createWrapper(),
    });

    expect(result.current.isLoading).toBe(true);
    expect(result.current.categories).toEqual([]);
  });
});
