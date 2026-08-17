import { describe, expect, it } from "vite-plus/test";
import { toCategoryTreeNode } from "@/features/category/types/category";

describe("toCategoryTreeNode", () => {
  it("CategorySummaryResponse からツリーノードを生成する", () => {
    const result = toCategoryTreeNode({ id: "1", name: "衣料品", sortOrder: 1 });

    expect(result).toEqual({
      id: "1",
      name: "衣料品",
      sortOrder: 1,
      children: [],
      isExpanded: false,
      isLoaded: false,
    });
  });

  it("フィールドが undefined の場合はデフォルト値を使う", () => {
    const result = toCategoryTreeNode({});

    expect(result).toEqual({
      id: "",
      name: "",
      sortOrder: 0,
      children: [],
      isExpanded: false,
      isLoaded: false,
    });
  });
});
