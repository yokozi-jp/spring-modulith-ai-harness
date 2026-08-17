import type { ReactNode } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { renderHook, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vite-plus/test";
import { useCategoryOptions } from "@/features/category/hooks/use-category-options";
import { apiClient } from "@/lib/api-client";

vi.mock("@/lib/api-client");

function createWrapper() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return function Wrapper({ children }: { readonly children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  };
}

describe("useCategoryOptions", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it("ルートカテゴリを選択肢として返す", async () => {
    vi.mocked(apiClient).mockResolvedValue({
      data: { content: [{ id: "1", name: "衣料品", sortOrder: 1 }] },
      status: 200,
      headers: new Headers(),
    });

    const { result } = renderHook(() => useCategoryOptions(), { wrapper: createWrapper() });

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.options).toEqual([{ id: "1", name: "衣料品" }]);
  });
});
