export const PRICING_LEVEL = {
  REGION: "REGION",
  PREFECTURE: "PREFECTURE",
} as const;

export type PricingLevel = (typeof PRICING_LEVEL)[keyof typeof PRICING_LEVEL];

export const LEVEL_LABELS: Record<PricingLevel, string> = {
  REGION: "地方単位",
  PREFECTURE: "都道府県単位",
};
