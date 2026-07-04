import { describe, expect, it, vi, beforeEach } from "vite-plus/test";
import { renderHook, waitFor } from "@testing-library/react";
import { createElement } from "react";
import type { ReactNode } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useCategoryOptions } from "@/features/category/hooks/use-category-options";

vi.mock("@/api/category/category", () => ({
  list2: vi.fn(),
  findChildren: vi.fn(),
  getList2Url: vi.fn(),
  getFindChildrenUrl: vi.fn(),
}));

import { list2, findChildren } from "@/api/category/category";

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });

  return function Wrapper({ children }: { readonly children: ReactNode }) {
    return createElement(QueryClientProvider, { client: queryClient }, children);
  };
}

describe("useCategoryOptions", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it("ツリーをフラットに取得する", async () => {
    vi.mocked(list2).mockResolvedValue({
      data: {
        content: [
          { id: "root-1", name: "ルートA", sortOrder: 0 },
          { id: "root-2", name: "ルートB", sortOrder: 1 },
        ],
      },
      status: 200,
      headers: new Headers(),
    } as Awaited<ReturnType<typeof list2>>);

    vi.mocked(findChildren).mockImplementation(async (id: string) => {
      if (id === "root-1") {
        return {
          data: [{ id: "child-1", name: "子A", sortOrder: 0 }],
          status: 200,
          headers: new Headers(),
        } as Awaited<ReturnType<typeof findChildren>>;
      }
      return {
        data: [],
        status: 200,
        headers: new Headers(),
      } as Awaited<ReturnType<typeof findChildren>>;
    });

    const { result } = renderHook(() => useCategoryOptions(), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.options).toEqual([
      { id: "root-1", name: "ルートA", depth: 0 },
      { id: "child-1", name: "子A", depth: 1 },
      { id: "root-2", name: "ルートB", depth: 0 },
    ]);
  });
});
