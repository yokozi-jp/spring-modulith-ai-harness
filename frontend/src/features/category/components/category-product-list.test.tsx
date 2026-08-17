import type { ReactNode } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render, screen } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vite-plus/test";
import { CategoryProductList } from "@/features/category/components/category-product-list";
import { apiClient } from "@/lib/api-client";

vi.mock("@/lib/api-client");
vi.mock("@tanstack/react-router", async () => {
  const actual = await vi.importActual("@tanstack/react-router");
  return {
    ...actual,
    Link: ({ children }: { readonly children: ReactNode }) => <span>{children}</span>,
  };
});

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });

  return function Wrapper({ children }: { readonly children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  };
}

describe("CategoryProductList", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it("商品がない場合はメッセージを表示する", async () => {
    vi.mocked(apiClient).mockResolvedValue({
      data: { content: [] },
      status: 200,
      headers: new Headers(),
    });

    const Wrapper = createWrapper();
    render(
      <Wrapper>
        <CategoryProductList categoryId="c1" />
      </Wrapper>,
    );

    const message = await screen.findByText("このカテゴリに商品がありません");
    expect(message).toBeTruthy();
  });

  it("商品がある場合はテーブルを表示する", async () => {
    vi.mocked(apiClient).mockResolvedValue({
      data: {
        content: [{ id: "p1", name: "商品A", status: "PUBLISHED", categoryId: "c1" }],
      },
      status: 200,
      headers: new Headers(),
    });

    const Wrapper = createWrapper();
    render(
      <Wrapper>
        <CategoryProductList categoryId="c1" />
      </Wrapper>,
    );

    const productName = await screen.findByText("商品A");
    expect(productName).toBeTruthy();
  });
});
