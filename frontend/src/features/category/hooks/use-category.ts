import { useFindById2 } from "@/api/category/category";
import type { AncestorResponse, CategoryDetailResponse } from "@/api/openAPIDefinition.schemas";

interface CategoryDetail {
  readonly id: string;
  readonly name: string;
  readonly sortOrder: number;
  readonly parentCategoryId: string | null;
  readonly version: number;
  readonly ancestors: AncestorResponse[];
}

function toDetail(data: CategoryDetailResponse): CategoryDetail {
  return {
    id: data.id ?? "",
    name: data.name ?? "",
    sortOrder: data.sortOrder ?? 0,
    parentCategoryId: data.parentCategoryId ?? null,
    version: data.version ?? 0,
    ancestors: data.ancestors ?? [],
  };
}

export function useCategory(id: string) {
  const query = useFindById2(id);

  return {
    category: query.data?.data !== undefined ? toDetail(query.data.data) : null,
    isLoading: query.isLoading,
    error: query.error,
    refetch: query.refetch,
  };
}
