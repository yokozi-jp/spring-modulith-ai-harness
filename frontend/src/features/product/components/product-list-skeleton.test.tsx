import { describe, expect, it } from "vite-plus/test";
import { render, screen } from "@testing-library/react";
import { ProductListSkeleton } from "@/features/product/components/product-list-skeleton";

describe("ProductListSkeleton", () => {
  it("status ロールで表示する", () => {
    render(<ProductListSkeleton />);

    expect(screen.getByRole("status")).toBeTruthy();
  });
});
