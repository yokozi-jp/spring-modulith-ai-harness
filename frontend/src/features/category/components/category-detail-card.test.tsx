import type { ComponentProps } from "react";
import { describe, expect, it, vi } from "vite-plus/test";
import { render, screen } from "@testing-library/react";
import { CategoryDetailCard } from "@/features/category/components/category-detail-card";

vi.mock("@tanstack/react-router", async () => {
  const actual = await vi.importActual("@tanstack/react-router");
  return {
    ...actual,
    Link: ({ children, to }: ComponentProps<"a"> & { readonly to?: string }) => (
      <a href={to}>{children}</a>
    ),
  };
});

describe("CategoryDetailCard", () => {
  it("カテゴリ名と並び順を表示する", () => {
    render(
      <CategoryDetailCard
        category={{ id: "1", name: "衣料品", sortOrder: 1, version: 0, ancestors: [] }}
      />,
    );

    expect(screen.getByText("衣料品")).toBeTruthy();
    expect(screen.getByText("1")).toBeTruthy();
  });

  it("祖先カテゴリがある場合は breadcrumb を表示する", () => {
    render(
      <CategoryDetailCard
        category={{
          id: "2",
          name: "トップス",
          sortOrder: 1,
          version: 0,
          ancestors: [{ id: "1", name: "衣料品" }],
        }}
      />,
    );

    expect(screen.getByText("衣料品")).toBeTruthy();
  });

  it("祖先カテゴリがない場合は breadcrumb を表示しない", () => {
    render(
      <CategoryDetailCard
        category={{ id: "1", name: "衣料品", sortOrder: 1, version: 0, ancestors: [] }}
      />,
    );

    expect(screen.queryByLabelText("カテゴリの階層パス")).toBeNull();
  });
});
