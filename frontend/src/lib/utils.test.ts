import { describe, expect, it } from "vite-plus/test";
import { cn } from "@/lib/utils";

describe("cn utility", () => {
  it("merges class names", () => {
    expect(cn("px-2", "py-1")).toBe("px-2 py-1");
  });

  it("resolves conflicts with last class winning", () => {
    expect(cn("px-2", "px-4")).toBe("px-4");
  });

  it("handles undefined and empty values", () => {
    expect(cn("px-2", undefined, "", "py-1")).toBe("px-2 py-1");
  });
});
