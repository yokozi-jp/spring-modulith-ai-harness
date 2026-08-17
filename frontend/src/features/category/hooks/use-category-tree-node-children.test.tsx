import type { ReactNode } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { renderHook, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vite-plus/test";
import { useCategoryTreeNodeChildren } from "@/features/category/hooks/use-category-tree-node-children";
import { apiClient } from "@/lib/api-client";

vi.mock("@/lib/api-client");

function createWrapper() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return function Wrapper({ children }: { readonly children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  };
}

describe("useCategoryTreeNodeChildren", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it("isExpanded が false の場合はリクエストしない", () => {
    vi.mocked(apiClient).mockResolvedValue({ data: [], status: 200, headers: new Headers() });

    const { result } = renderHook(() => useCategoryTreeNodeChildren("1", false), {
      wrapper: createWrapper(),
    });

    expect(result.current.isLoading).toBe(false);
    expect(result.current.childNodes).toEqual([]);
  });

  it("isExpanded が true の場合に childNodes を返す", async () => {
    vi.mocked(apiClient).mockResolvedValue({
      data: [{ id: "2", name: "トップス", sortOrder: 1 }],
      status: 200,
      headers: new Headers(),
    });

    const { result } = renderHook(() => useCategoryTreeNodeChildren("1", true), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.childNodes).toEqual([
      { id: "2", name: "トップス", sortOrder: 1, children: [], isExpanded: false, isLoaded: false },
    ]);
  });
});
