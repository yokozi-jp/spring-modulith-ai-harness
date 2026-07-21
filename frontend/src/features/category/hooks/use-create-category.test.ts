import { describe, expect, it, vi, beforeEach } from "vite-plus/test";
import { renderHook, act } from "@testing-library/react";
import { createElement } from "react";
import type { ReactNode } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useCreateCategory } from "@/features/category/hooks/use-create-category";

vi.mock("@tanstack/react-router", () => ({
  useNavigate: () => vi.fn(),
}));

vi.mock("@/api/category/category", () => ({
  useCreate2: vi.fn(),
  getList2QueryKey: vi.fn(() => ["/api/v1/categories"]),
}));

import { useCreate2 } from "@/api/category/category";

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });

  return function Wrapper({ children }: { readonly children: ReactNode }) {
    return createElement(QueryClientProvider, { client: queryClient }, children);
  };
}

describe("useCreateCategory", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it("createCategory が mutation を呼び出す", () => {
    const mutateFn = vi.fn();
    vi.mocked(useCreate2).mockReturnValue({
      mutate: mutateFn,
      isPending: false,
      error: null,
    } as unknown as ReturnType<typeof useCreate2>);

    const { result } = renderHook(() => useCreateCategory(), {
      wrapper: createWrapper(),
    });

    act(() => {
      result.current.createCategory({ name: "新カテゴリ", sortOrder: 1 });
    });

    expect(mutateFn).toHaveBeenCalledWith({
      data: { name: "新カテゴリ", sortOrder: 1 },
    });
  });

  it("送信中は isCreating が true", () => {
    vi.mocked(useCreate2).mockReturnValue({
      mutate: vi.fn(),
      isPending: true,
      error: null,
    } as unknown as ReturnType<typeof useCreate2>);

    const { result } = renderHook(() => useCreateCategory(), {
      wrapper: createWrapper(),
    });

    expect(result.current.isCreating).toBe(true);
  });
});
