import { describe, expect, it, vi, beforeEach } from "vite-plus/test";
import { renderHook } from "@testing-library/react";
import { createElement } from "react";
import type { ReactNode } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useCategoryChildren } from "@/features/category/hooks/use-category-children";

vi.mock("@/api/category/category", () => ({
  useFindChildren: vi.fn(),
}));

import { useFindChildren } from "@/api/category/category";

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });

  return function Wrapper({ children }: { readonly children: ReactNode }) {
    return createElement(QueryClientProvider, { client: queryClient }, children);
  };
}

describe("useCategoryChildren", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it("ロード中は空の children を返す", () => {
    vi.mocked(useFindChildren).mockReturnValue({
      data: undefined,
      isLoading: true,
      error: null,
    } as unknown as ReturnType<typeof useFindChildren>);

    const { result } = renderHook(() => useCategoryChildren("cat-1"), {
      wrapper: createWrapper(),
    });

    expect(result.current.isLoading).toBe(true);
    expect(result.current.children).toEqual([]);
  });

  it("取得成功時に children を返す", () => {
    const mockChildren = [
      { id: "child-1", name: "子A", sortOrder: 0 },
      { id: "child-2", name: "子B", sortOrder: 1 },
    ];

    vi.mocked(useFindChildren).mockReturnValue({
      data: {
        data: mockChildren,
        status: 200,
        headers: new Headers(),
      },
      isLoading: false,
      error: null,
    } as unknown as ReturnType<typeof useFindChildren>);

    const { result } = renderHook(() => useCategoryChildren("cat-1"), {
      wrapper: createWrapper(),
    });

    expect(result.current.isLoading).toBe(false);
    expect(result.current.children).toEqual(mockChildren);
  });
});
