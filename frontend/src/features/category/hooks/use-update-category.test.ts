import { describe, expect, it, vi, beforeEach } from "vite-plus/test";
import { renderHook, act } from "@testing-library/react";
import { createElement } from "react";
import type { ReactNode } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useUpdateCategory } from "@/features/category/hooks/use-update-category";

vi.mock("@tanstack/react-router", () => ({
  useNavigate: () => vi.fn(),
}));

vi.mock("@/api/category/category", () => ({
  useUpdate2: vi.fn(),
  getList2QueryKey: vi.fn(() => ["/api/v1/categories"]),
  getFindById2QueryKey: vi.fn((id: string) => [`/api/v1/categories/${id}`]),
}));

import { useUpdate2 } from "@/api/category/category";

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });

  return function Wrapper({ children }: { readonly children: ReactNode }) {
    return createElement(QueryClientProvider, { client: queryClient }, children);
  };
}

describe("useUpdateCategory", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it("updateCategory が mutation を呼び出す", () => {
    const mutateFn = vi.fn();
    vi.mocked(useUpdate2).mockReturnValue({
      mutate: mutateFn,
      isPending: false,
      error: null,
    } as unknown as ReturnType<typeof useUpdate2>);

    const { result } = renderHook(() => useUpdateCategory("cat-1"), {
      wrapper: createWrapper(),
    });

    act(() => {
      result.current.updateCategory({
        name: "更新カテゴリ",
        sortOrder: 2,
        version: 1,
      });
    });

    expect(mutateFn).toHaveBeenCalledWith({
      id: "cat-1",
      data: { name: "更新カテゴリ", sortOrder: 2, version: 1 },
    });
  });

  it("送信中は isUpdating が true", () => {
    vi.mocked(useUpdate2).mockReturnValue({
      mutate: vi.fn(),
      isPending: true,
      error: null,
    } as unknown as ReturnType<typeof useUpdate2>);

    const { result } = renderHook(() => useUpdateCategory("cat-1"), {
      wrapper: createWrapper(),
    });

    expect(result.current.isUpdating).toBe(true);
  });
});
