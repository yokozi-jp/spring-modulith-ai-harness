import { describe, expect, it } from "vite-plus/test";
import { render, screen } from "@testing-library/react";
import { ProductDetailSkeleton } from "@/features/product/components/product-detail-skeleton";

describe("ProductDetailSkeleton", () => {
  it("status ロールで表示する", () => {
    render(<ProductDetailSkeleton />);

    expect(screen.getByRole("status")).toBeTruthy();
  });
});
