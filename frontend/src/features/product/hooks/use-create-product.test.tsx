import type { ReactNode } from "react";
import { act } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import * as tanstackRouter from "@tanstack/react-router";
import { renderHook, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vite-plus/test";
import { useCreateProduct } from "@/features/product/hooks/use-create-product";
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

describe("useCreateProduct", () => {
  const navigateMock = vi.fn();

  beforeEach(() => {
    vi.resetAllMocks();
    vi.mocked(tanstackRouter.useNavigate).mockReturnValue(navigateMock);
  });

  it("作成リクエストを正しい内容で送信する", async () => {
    vi.mocked(apiClient).mockResolvedValue({
      data: undefined,
      status: 201,
      headers: new Headers(),
    });

    const { result } = renderHook(() => useCreateProduct(), { wrapper: createWrapper() });

    act(() => {
      result.current.createProduct({
        name: "Tシャツ",
        description: "説明",
        categoryId: "cat-1",
        sku: "SKU-1",
      });
    });

    await waitFor(() => {
      expect(apiClient).toHaveBeenCalledWith(
        "/api/v1/products",
        expect.objectContaining({
          method: "POST",
          body: JSON.stringify({
            name: "Tシャツ",
            description: "説明",
            categoryId: "cat-1",
            sku: "SKU-1",
          }),
        }),
      );
    });
  });

  it("作成成功時に一覧ページへ遷移する", async () => {
    vi.mocked(apiClient).mockResolvedValue({
      data: undefined,
      status: 201,
      headers: new Headers(),
    });

    const { result } = renderHook(() => useCreateProduct(), { wrapper: createWrapper() });

    act(() => {
      result.current.createProduct({
        name: "Tシャツ",
        description: "説明",
        categoryId: "cat-1",
        sku: "SKU-1",
      });
    });

    await waitFor(() => {
      expect(navigateMock).toHaveBeenCalledWith({ to: "/products" });
    });
  });
});
