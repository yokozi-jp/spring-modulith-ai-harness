import type { ReactNode } from "react";
import { act } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import * as tanstackRouter from "@tanstack/react-router";
import { renderHook, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vite-plus/test";
import { useUpdateCategory } from "@/features/category/hooks/use-update-category";
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

describe("useUpdateCategory", () => {
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

    const { result } = renderHook(() => useUpdateCategory("1"), { wrapper: createWrapper() });

    act(() => {
      result.current.updateCategory({ name: "衣料品(改)", sortOrder: 2, version: 0 });
    });

    await waitFor(() => {
      expect(apiClient).toHaveBeenCalledWith(
        "/api/v1/categories/1",
        expect.objectContaining({
          method: "PUT",
          body: JSON.stringify({ name: "衣料品(改)", sortOrder: 2, version: 0 }),
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

    const { result } = renderHook(() => useUpdateCategory("1"), { wrapper: createWrapper() });

    act(() => {
      result.current.updateCategory({ name: "衣料品", sortOrder: 1, version: 0 });
    });

    await waitFor(() => {
      expect(navigateMock).toHaveBeenCalledWith({ to: "/categories/$id", params: { id: "1" } });
    });
  });
});
