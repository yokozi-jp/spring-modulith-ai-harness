import type { ReactNode } from "react";
import { act } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import * as tanstackRouter from "@tanstack/react-router";
import { renderHook, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vite-plus/test";
import { useDeleteProduct } from "@/features/product/hooks/use-delete-product";
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

describe("useDeleteProduct", () => {
  const navigateMock = vi.fn();

  beforeEach(() => {
    vi.resetAllMocks();
    vi.mocked(tanstackRouter.useNavigate).mockReturnValue(navigateMock);
  });

  it("削除リクエストを version 付きで送信する", async () => {
    vi.mocked(apiClient).mockResolvedValue({
      data: undefined,
      status: 204,
      headers: new Headers(),
    });

    const { result } = renderHook(() => useDeleteProduct(), { wrapper: createWrapper() });

    act(() => {
      result.current.deleteProduct("1", 0);
    });

    await waitFor(() => {
      expect(apiClient).toHaveBeenCalledWith(
        "/api/v1/products/1",
        expect.objectContaining({ method: "DELETE", body: JSON.stringify({ version: 0 }) }),
      );
    });
  });

  it("削除成功時に一覧ページへ遷移する", async () => {
    vi.mocked(apiClient).mockResolvedValue({
      data: undefined,
      status: 204,
      headers: new Headers(),
    });

    const { result } = renderHook(() => useDeleteProduct(), { wrapper: createWrapper() });

    act(() => {
      result.current.deleteProduct("1", 0);
    });

    await waitFor(() => {
      expect(navigateMock).toHaveBeenCalledWith({ to: "/products" });
    });
  });

  it("削除失敗時に error を返す", async () => {
    vi.mocked(apiClient).mockRejectedValue(new Error("Conflict"));

    const { result } = renderHook(() => useDeleteProduct(), { wrapper: createWrapper() });

    act(() => {
      result.current.deleteProduct("1", 0);
    });

    await waitFor(() => {
      expect(result.current.error).not.toBeNull();
    });
  });
});
