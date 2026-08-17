import type { ReactNode } from "react";
import { act } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import * as tanstackRouter from "@tanstack/react-router";
import { renderHook, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vite-plus/test";
import { useUpdateProduct } from "@/features/product/hooks/use-update-product";
import { apiClient } from "@/lib/api-client";

vi.mock("@/lib/api-client");
vi.mock("@tanstack/react-router", async () => {
  const actual = await vi.importActual("@tanstack/react-router");
  return { ...actual, useNavigate: vi.fn() };
});

function createWrapper() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return function Wrapper({ children }: { readonly children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  };
}

describe("useUpdateProduct", () => {
  const navigateMock = vi.fn();

  beforeEach(() => {
    vi.resetAllMocks();
    vi.mocked(tanstackRouter.useNavigate).mockReturnValue(navigateMock);
  });

  it("更新リクエストを正しい内容で送信する", async () => {
    vi.mocked(apiClient).mockResolvedValue({
      data: undefined,
      status: 200,
      headers: new Headers(),
    });

    const { result } = renderHook(() => useUpdateProduct("1"), { wrapper: createWrapper() });

    act(() => {
      result.current.updateProduct({
        name: "Tシャツ(改)",
        description: "説明(改)",
        categoryId: "cat-2",
        version: 0,
      });
    });

    await waitFor(() => {
      expect(apiClient).toHaveBeenCalledWith(
        "/api/v1/products/1",
        expect.objectContaining({
          method: "PUT",
          body: JSON.stringify({
            name: "Tシャツ(改)",
            description: "説明(改)",
            categoryId: "cat-2",
            version: 0,
          }),
        }),
      );
    });
  });

  it("更新成功時に詳細ページへ遷移する", async () => {
    vi.mocked(apiClient).mockResolvedValue({
      data: undefined,
      status: 200,
      headers: new Headers(),
    });

    const { result } = renderHook(() => useUpdateProduct("1"), { wrapper: createWrapper() });

    act(() => {
      result.current.updateProduct({
        name: "Tシャツ",
        description: "説明",
        categoryId: "cat-1",
        version: 0,
      });
    });

    await waitFor(() => {
      expect(navigateMock).toHaveBeenCalledWith({ to: "/products/$id", params: { id: "1" } });
    });
  });
});
