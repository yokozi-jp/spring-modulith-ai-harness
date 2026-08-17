import type { ReactNode } from "react";
import { act } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import * as tanstackRouter from "@tanstack/react-router";
import { renderHook, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vite-plus/test";
import { useCreateCategory } from "@/features/category/hooks/use-create-category";
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

describe("useCreateCategory", () => {
  const navigateMock = vi.fn();

  beforeEach(() => {
    vi.resetAllMocks();
    vi.mocked(tanstackRouter.useNavigate).mockReturnValue(navigateMock);
  });

  it("parentCategoryId ありで作成リクエストを送信する", async () => {
    vi.mocked(apiClient).mockResolvedValue({
      data: undefined,
      status: 201,
      headers: new Headers(),
    });

    const { result } = renderHook(() => useCreateCategory(), { wrapper: createWrapper() });

    act(() => {
      result.current.createCategory({ name: "トップス", sortOrder: 1, parentCategoryId: "1" });
    });

    await waitFor(() => {
      expect(apiClient).toHaveBeenCalledWith(
        "/api/v1/categories",
        expect.objectContaining({
          method: "POST",
          body: JSON.stringify({ name: "トップス", sortOrder: 1, parentCategoryId: "1" }),
        }),
      );
    });
  });

  it("parentCategoryId なしで作成リクエストを送信する（プロパティ省略）", async () => {
    vi.mocked(apiClient).mockResolvedValue({
      data: undefined,
      status: 201,
      headers: new Headers(),
    });

    const { result } = renderHook(() => useCreateCategory(), { wrapper: createWrapper() });

    act(() => {
      result.current.createCategory({ name: "衣料品", sortOrder: 1 });
    });

    await waitFor(() => {
      expect(apiClient).toHaveBeenCalledWith(
        "/api/v1/categories",
        expect.objectContaining({
          method: "POST",
          body: JSON.stringify({ name: "衣料品", sortOrder: 1 }),
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

    const { result } = renderHook(() => useCreateCategory(), { wrapper: createWrapper() });

    act(() => {
      result.current.createCategory({ name: "衣料品", sortOrder: 1 });
    });

    await waitFor(() => {
      expect(navigateMock).toHaveBeenCalledWith({ to: "/categories" });
    });
  });
});
