import { describe, expect, it } from "vite-plus/test";
import { getPricingLevelLabel, PRICING_LEVEL } from "@/features/pricing/types/pricing-level";

describe("getPricingLevelLabel", () => {
  it("REGION を '地方単位' に変換する", () => {
    expect(getPricingLevelLabel(PRICING_LEVEL.REGION)).toBe("地方単位");
  });

  it("PREFECTURE を '都道府県単位' に変換する", () => {
    expect(getPricingLevelLabel(PRICING_LEVEL.PREFECTURE)).toBe("都道府県単位");
  });

  it("未知の値はそのまま返す", () => {
    expect(getPricingLevelLabel("UNKNOWN")).toBe("UNKNOWN");
  });
});
