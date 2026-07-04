export const PRODUCT_STATUS = {
  DRAFT: "DRAFT",
  PUBLISHED: "PUBLISHED",
  ARCHIVED: "ARCHIVED",
} as const;

export type ProductStatus = (typeof PRODUCT_STATUS)[keyof typeof PRODUCT_STATUS];

export const STATUS_LABELS: Record<ProductStatus, string> = {
  DRAFT: "下書き",
  PUBLISHED: "公開",
  ARCHIVED: "アーカイブ",
};

export const STATUS_COLORS: Record<ProductStatus, string> = {
  DRAFT: "bg-muted text-muted-foreground",
  PUBLISHED: "bg-emerald-100 text-emerald-700",
  ARCHIVED: "bg-zinc-100 text-zinc-500",
};
