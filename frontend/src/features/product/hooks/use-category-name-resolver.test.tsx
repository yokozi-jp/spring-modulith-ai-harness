import type { ReactNode } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { renderHook, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vite-plus/test";
import { useCategoryNameResolver } from "@/features/product/hooks/use-category-name-resolver";
import { apiClient } from "@/lib/api-client";

vi.mock("@/lib/api-client");

function createWrapper() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return function Wrapper({ children }: { readonly children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  };
}

describe("useCategoryNameResolver", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it("categoryId からカテゴリ名を解決する", async () => {
    vi.mocked(apiClient).mockResolvedValue({
      data: { content: [{ id: "cat-1", name: "衣料品", sortOrder: 1 }] },
      status: 200,
      headers: new Headers(),
    });

    const { result } = renderHook(() => useCategoryNameResolver(), { wrapper: createWrapper() });

    await waitFor(() => {
      expect(result.current.resolveCategoryName("cat-1")).toBe("衣料品");
    });
  });

  it("見つからない categoryId はそのまま返す", async () => {
    vi.mocked(apiClient).mockResolvedValue({
      data: { content: [] },
      status: 200,
      headers: new Headers(),
    });

    const { result } = renderHook(() => useCategoryNameResolver(), { wrapper: createWrapper() });

    await waitFor(() => {
      expect(result.current.resolveCategoryName("unknown-id")).toBe("unknown-id");
    });
  });
});
