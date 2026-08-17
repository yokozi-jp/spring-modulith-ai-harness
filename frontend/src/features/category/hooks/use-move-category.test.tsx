import type { ReactNode } from "react";
import { act } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { renderHook, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vite-plus/test";
import { useMoveCategory } from "@/features/category/hooks/use-move-category";
import { apiClient } from "@/lib/api-client";

vi.mock("@/lib/api-client");

function createWrapper() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return function Wrapper({ children }: { readonly children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  };
}

describe("useMoveCategory", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it("newParentCategoryId ありで移動リクエストを送信する", async () => {
    vi.mocked(apiClient).mockResolvedValue({
      data: undefined,
      status: 200,
      headers: new Headers(),
    });

    const { result } = renderHook(() => useMoveCategory("1"), { wrapper: createWrapper() });

    act(() => {
      result.current.moveCategory({ newParentCategoryId: "2", version: 0 });
    });

    await waitFor(() => {
      expect(apiClient).toHaveBeenCalledWith(
        "/api/v1/categories/1/move",
        expect.objectContaining({
          method: "PATCH",
          body: JSON.stringify({ version: 0, newParentCategoryId: "2" }),
        }),
      );
    });
  });

  it("newParentCategoryId なし（ルートへ移動）でリクエストを送信する", async () => {
    vi.mocked(apiClient).mockResolvedValue({
      data: undefined,
      status: 200,
      headers: new Headers(),
    });

    const { result } = renderHook(() => useMoveCategory("1"), { wrapper: createWrapper() });

    act(() => {
      result.current.moveCategory({ version: 0 });
    });

    await waitFor(() => {
      expect(apiClient).toHaveBeenCalledWith(
        "/api/v1/categories/1/move",
        expect.objectContaining({ method: "PATCH", body: JSON.stringify({ version: 0 }) }),
      );
    });
  });

  it("移動失敗時に error を返す", async () => {
    vi.mocked(apiClient).mockRejectedValue(new Error("Conflict"));

    const { result } = renderHook(() => useMoveCategory("1"), { wrapper: createWrapper() });

    act(() => {
      result.current.moveCategory({ version: 0 });
    });

    await waitFor(() => {
      expect(result.current.error).not.toBeNull();
    });
  });
});
