import { describe, expect, it } from "vite-plus/test";
import {
  getProductStatusColor,
  getProductStatusLabel,
  isProductStatus,
} from "@/features/product/types/product-status";

describe("isProductStatus", () => {
  it("有効なステータス文字列に対して true を返す", () => {
    expect(isProductStatus("DRAFT")).toBe(true);
  });

  it("不明な文字列に対して false を返す", () => {
    expect(isProductStatus("UNKNOWN")).toBe(false);
  });
});

describe("getProductStatusLabel", () => {
  it("DRAFT に対して「下書き」を返す", () => {
    expect(getProductStatusLabel("DRAFT")).toBe("下書き");
  });

  it("PUBLISHED に対して「公開中」を返す", () => {
    expect(getProductStatusLabel("PUBLISHED")).toBe("公開中");
  });

  it("不明な値はそのまま返す", () => {
    expect(getProductStatusLabel("UNKNOWN")).toBe("UNKNOWN");
  });
});

describe("getProductStatusColor", () => {
  it("DRAFT に対してカラークラスを返す", () => {
    expect(getProductStatusColor("DRAFT")).toBe("bg-muted text-muted-foreground");
  });

  it("不明な値に対して既定クラスを返す", () => {
    expect(getProductStatusColor("UNKNOWN")).toBe("bg-muted text-muted-foreground");
  });
});
