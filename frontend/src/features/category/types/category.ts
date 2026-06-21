export interface CategorySummary {
  readonly id: string;
  readonly name: string;
  readonly sortOrder: number;
}

export interface CategoryAncestor {
  readonly id: string;
  readonly name: string;
}

export interface CategoryDetail {
  readonly id: string;
  readonly name: string;
  readonly sortOrder: number;
  readonly parentCategoryId: string | null;
  readonly version: number;
  readonly ancestors: readonly CategoryAncestor[];
}

export interface CreateCategoryInput {
  readonly name: string;
  readonly sortOrder: number;
  readonly parentCategoryId: string | null;
}

export interface UpdateCategoryInput {
  readonly name: string;
  readonly sortOrder: number;
  readonly version: number;
}

export interface Page<T> {
  readonly content: readonly T[];
  readonly totalElements: number;
  readonly totalPages: number;
  readonly number: number;
  readonly size: number;
}
