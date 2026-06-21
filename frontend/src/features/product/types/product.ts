export const PRODUCT_STATUS = {
  DRAFT: "DRAFT",
  PUBLISHED: "PUBLISHED",
  ARCHIVED: "ARCHIVED",
} as const;

export type ProductStatus = (typeof PRODUCT_STATUS)[keyof typeof PRODUCT_STATUS];

export interface ProductSummary {
  readonly id: string;
  readonly name: string;
  readonly status: ProductStatus;
  readonly categoryId: string;
}

export interface ProductDetail {
  readonly id: string;
  readonly name: string;
  readonly description: string;
  readonly categoryId: string;
  readonly sku: string;
  readonly status: ProductStatus;
  readonly version: number;
}

export interface CreateProductInput {
  readonly name: string;
  readonly description: string;
  readonly categoryId: string;
  readonly sku: string;
}

export interface UpdateProductInput {
  readonly name: string;
  readonly description: string;
  readonly categoryId: string;
  readonly version: number;
}

export interface ProductPage {
  readonly content: readonly ProductSummary[];
  readonly totalElements: number;
  readonly totalPages: number;
  readonly number: number;
  readonly size: number;
}
