import { describe, expect, it } from "vite-plus/test";
import { render, screen } from "@testing-library/react";
import { CategoryDetailSkeleton } from "@/features/category/components/category-detail-skeleton";

describe("CategoryDetailSkeleton", () => {
  it("status ロールで表示する", () => {
    render(<CategoryDetailSkeleton />);

    expect(screen.getByRole("status")).toBeTruthy();
  });
});
