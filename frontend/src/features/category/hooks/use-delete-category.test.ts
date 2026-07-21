import { describe, expect, it, vi, beforeEach } from "vite-plus/test";
import { renderHook, act } from "@testing-library/react";
import { createElement } from "react";
import type { ReactNode } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useDeleteCategory } from "@/features/category/hooks/use-delete-category";

vi.mock("@tanstack/react-router", () => ({
  useNavigate: () => vi.fn(),
}));

vi.mock("@/api/category/category", () => ({
  useDelete2: vi.fn(),
  getList2QueryKey: vi.fn(() => ["/api/v1/categories"]),
}));

import { useDelete2 } from "@/api/category/category";

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });

  return function Wrapper({ children }: { readonly children: ReactNode }) {
    return createElement(QueryClientProvider, { client: queryClient }, children);
  };
}

describe("useDeleteCategory", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it("deleteCategory が mutation を呼び出す", () => {
    const mutateFn = vi.fn();
    vi.mocked(useDelete2).mockReturnValue({
      mutate: mutateFn,
      isPending: false,
      error: null,
    } as unknown as ReturnType<typeof useDelete2>);

    const { result } = renderHook(() => useDeleteCategory(), {
      wrapper: createWrapper(),
    });

    act(() => {
      result.current.deleteCategory("cat-1", 2);
    });

    expect(mutateFn).toHaveBeenCalledWith({
      id: "cat-1",
      data: { version: 2 },
    });
  });

  it("削除中は isDeleting が true", () => {
    vi.mocked(useDelete2).mockReturnValue({
      mutate: vi.fn(),
      isPending: true,
      error: null,
    } as unknown as ReturnType<typeof useDelete2>);

    const { result } = renderHook(() => useDeleteCategory(), {
      wrapper: createWrapper(),
    });

    expect(result.current.isDeleting).toBe(true);
  });
});
