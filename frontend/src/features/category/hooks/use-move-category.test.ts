import { describe, expect, it, vi, beforeEach } from "vite-plus/test";
import { renderHook, act } from "@testing-library/react";
import { createElement } from "react";
import type { ReactNode } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useMoveCategory } from "@/features/category/hooks/use-move-category";

vi.mock("@/api/category/category", () => ({
  useMove: vi.fn(),
  getList2QueryKey: vi.fn(() => ["/api/v1/categories"]),
  getFindById2QueryKey: vi.fn((id: string) => [`/api/v1/categories/${id}`]),
}));

import { useMove } from "@/api/category/category";

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });

  return function Wrapper({ children }: { readonly children: ReactNode }) {
    return createElement(QueryClientProvider, { client: queryClient }, children);
  };
}

describe("useMoveCategory", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it("moveCategory が mutation を呼び出す（親指定）", () => {
    const mutateFn = vi.fn();
    vi.mocked(useMove).mockReturnValue({
      mutate: mutateFn,
      isPending: false,
      isSuccess: false,
      error: null,
      reset: vi.fn(),
    } as unknown as ReturnType<typeof useMove>);

    const { result } = renderHook(() => useMoveCategory("cat-1"), {
      wrapper: createWrapper(),
    });

    act(() => {
      result.current.moveCategory("parent-2", 3);
    });

    expect(mutateFn).toHaveBeenCalledWith({
      id: "cat-1",
      data: { version: 3, newParentCategoryId: "parent-2" },
    });
  });

  it("moveCategory が mutation を呼び出す（ルートに移動）", () => {
    const mutateFn = vi.fn();
    vi.mocked(useMove).mockReturnValue({
      mutate: mutateFn,
      isPending: false,
      isSuccess: false,
      error: null,
      reset: vi.fn(),
    } as unknown as ReturnType<typeof useMove>);

    const { result } = renderHook(() => useMoveCategory("cat-1"), {
      wrapper: createWrapper(),
    });

    act(() => {
      result.current.moveCategory(null, 3);
    });

    expect(mutateFn).toHaveBeenCalledWith({
      id: "cat-1",
      data: { version: 3 },
    });
  });

  it("移動中は isMoving が true", () => {
    vi.mocked(useMove).mockReturnValue({
      mutate: vi.fn(),
      isPending: true,
      isSuccess: false,
      error: null,
      reset: vi.fn(),
    } as unknown as ReturnType<typeof useMove>);

    const { result } = renderHook(() => useMoveCategory("cat-1"), {
      wrapper: createWrapper(),
    });

    expect(result.current.isMoving).toBe(true);
  });
});
