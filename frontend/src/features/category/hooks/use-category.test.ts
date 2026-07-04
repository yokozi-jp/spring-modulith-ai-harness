import { describe, expect, it, vi, beforeEach } from "vite-plus/test";
import { renderHook } from "@testing-library/react";
import { createElement } from "react";
import type { ReactNode } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useCategory } from "@/features/category/hooks/use-category";

vi.mock("@/api/category/category", () => ({
  useFindById2: vi.fn(),
  getFindById2QueryKey: vi.fn((id: string) => [`/api/v1/categories/${id}`]),
}));

import { useFindById2 } from "@/api/category/category";

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });

  return function Wrapper({ children }: { readonly children: ReactNode }) {
    return createElement(QueryClientProvider, { client: queryClient }, children);
  };
}

describe("useCategory", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it("isLoading が true のとき category は null", () => {
    vi.mocked(useFindById2).mockReturnValue({
      data: undefined,
      isLoading: true,
      error: null,
      refetch: vi.fn(),
    } as unknown as ReturnType<typeof useFindById2>);

    const { result } = renderHook(() => useCategory("cat-1"), {
      wrapper: createWrapper(),
    });

    expect(result.current.isLoading).toBe(true);
    expect(result.current.category).toBeNull();
  });

  it("取得成功時に category を返す", () => {
    vi.mocked(useFindById2).mockReturnValue({
      data: {
        data: {
          id: "cat-1",
          name: "テスト",
          sortOrder: 0,
          parentCategoryId: null,
          version: 1,
          ancestors: [],
        },
        status: 200,
        headers: new Headers(),
      },
      isLoading: false,
      error: null,
      refetch: vi.fn(),
    } as unknown as ReturnType<typeof useFindById2>);

    const { result } = renderHook(() => useCategory("cat-1"), {
      wrapper: createWrapper(),
    });

    expect(result.current.isLoading).toBe(false);
    expect(result.current.category).toEqual({
      id: "cat-1",
      name: "テスト",
      sortOrder: 0,
      parentCategoryId: null,
      version: 1,
      ancestors: [],
    });
  });
});
