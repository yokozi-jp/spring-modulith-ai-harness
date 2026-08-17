/** 商品ステータス。backend の ProductStatus enum に対応する。 */
export const PRODUCT_STATUS = {
  DRAFT: "DRAFT",
  PUBLISHED: "PUBLISHED",
  ARCHIVED: "ARCHIVED",
} as const;

export type ProductStatus = (typeof PRODUCT_STATUS)[keyof typeof PRODUCT_STATUS];

/** ステータスの日本語表示ラベル。 */
export const PRODUCT_STATUS_LABELS: Record<ProductStatus, string> = {
  DRAFT: "下書き",
  PUBLISHED: "公開中",
  ARCHIVED: "アーカイブ済み",
};

/** ステータスに応じたバッジ表示用クラス。 */
export const PRODUCT_STATUS_COLORS: Record<ProductStatus, string> = {
  DRAFT: "bg-muted text-muted-foreground",
  PUBLISHED: "bg-emerald-100 text-emerald-700",
  ARCHIVED: "bg-zinc-100 text-zinc-500",
};

/** 文字列を ProductStatus として扱えるか判定する。 */
export function isProductStatus(value: string): value is ProductStatus {
  return value in PRODUCT_STATUS_LABELS;
}

/** ステータスラベルを取得する。不明な値の場合はそのまま返す。 */
export function getProductStatusLabel(status: string): string {
  return isProductStatus(status) ? PRODUCT_STATUS_LABELS[status] : status;
}

/** ステータスカラークラスを取得する。不明な値の場合は既定クラスを返す。 */
export function getProductStatusColor(status: string): string {
  return isProductStatus(status) ? PRODUCT_STATUS_COLORS[status] : "bg-muted text-muted-foreground";
}
