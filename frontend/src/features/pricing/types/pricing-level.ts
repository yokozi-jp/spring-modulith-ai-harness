/** 価格レベル定数。バックエンドの PricingLevel enum に対応する。 */
export const PRICING_LEVEL = {
  REGION: "REGION",
  PREFECTURE: "PREFECTURE",
} as const;

export type PricingLevel = (typeof PRICING_LEVEL)[keyof typeof PRICING_LEVEL];

/** 価格レベルの表示ラベル。 */
export const PRICING_LEVEL_LABELS: Record<PricingLevel, string> = {
  REGION: "地方単位",
  PREFECTURE: "都道府県単位",
};

/** level 値を表示ラベルに変換する。未知の値はそのまま返す。 */
export function getPricingLevelLabel(level: string): string {
  if (level in PRICING_LEVEL_LABELS) {
    return PRICING_LEVEL_LABELS[level as PricingLevel];
  }
  return level;
}
