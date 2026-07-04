import { useList2 } from "@/api/category/category";
import type { CategorySummaryResponse } from "@/api/openAPIDefinition.schemas";

export function useCategoryList() {
  const query = useList2({
    param: {},
    pageable: { page: 0, size: 100, sort: ["sortOrder,asc"] },
  });

  return {
    categories: (query.data?.data?.content ?? []) as CategorySummaryResponse[],
    isLoading: query.isLoading,
    error: query.error,
  };
}
