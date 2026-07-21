import { useFindChildren } from "@/api/category/category";
import type { CategorySummaryResponse } from "@/api/openAPIDefinition.schemas";

export function useCategoryChildren(id: string) {
  const query = useFindChildren(id);

  return {
    children: (query.data?.data ?? []) as CategorySummaryResponse[],
    isLoading: query.isLoading,
    error: query.error,
  };
}
