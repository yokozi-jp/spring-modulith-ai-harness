import { useList2 } from "@/api/category/category";
import type { CategorySummaryResponse } from "@/api/openAPIDefinition.schemas";

export function useCategoryList(page: number = 0, size: number = 20) {
  const query = useList2({
    param: {},
    pageable: { page, size, sort: ["sortOrder,asc"] },
  });

  return {
    categories: (query.data?.data?.content ?? []) as CategorySummaryResponse[],
    totalPages: query.data?.data?.totalPages ?? 0,
    totalElements: query.data?.data?.totalElements ?? 0,
    isLoading: query.isLoading,
    error: query.error,
  };
}
