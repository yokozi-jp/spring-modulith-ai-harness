import type { ReactNode } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render, screen } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vite-plus/test";
import { ProductPricingList } from "@/features/product/components/product-pricing-list";
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

describe("ProductPricingList", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it("価格がない場合はメッセージを表示する", async () => {
    vi.mocked(apiClient).mockResolvedValue({
      data: { content: [] },
      status: 200,
      headers: new Headers(),
    });

    const Wrapper = createWrapper();
    render(
      <Wrapper>
        <ProductPricingList productId="p1" />
      </Wrapper>,
    );

    const message = await screen.findByText("この商品に価格が登録されていません");
    expect(message).toBeTruthy();
  });

  it("価格がある場合はテーブルを表示する", async () => {
    vi.mocked(apiClient).mockResolvedValue({
      data: {
        content: [{ id: "pr1", productId: "p1", level: "REGION", areaCode: "関東", amount: 1500 }],
      },
      status: 200,
      headers: new Headers(),
    });

    const Wrapper = createWrapper();
    render(
      <Wrapper>
        <ProductPricingList productId="p1" />
      </Wrapper>,
    );

    const levelCell = await screen.findByText("地方単位");
    expect(levelCell).toBeTruthy();
    expect(screen.getByText("関東")).toBeTruthy();
  });
});
