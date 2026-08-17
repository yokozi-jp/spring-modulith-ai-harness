import { describe, expect, it } from "vite-plus/test";
import { getIndentClass } from "@/features/category/components/tree-indent";

describe("getIndentClass", () => {
  it("depth 0 に対応するクラスを返す", () => {
    expect(getIndentClass(0)).toBe("pl-2");
  });

  it("depth 2 に対応するクラスを返す", () => {
    expect(getIndentClass(2)).toBe("pl-12");
  });

  it("配列の最大深度を超える場合は最後のクラスに頭打ちにする", () => {
    expect(getIndentClass(100)).toBe("pl-37");
  });
});
