import { useQuery } from "@tanstack/react-query";
import { apiClient } from "@/lib/api-client";
import type { CategorySummary, Page } from "@/features/category/types/category";

export function useCategoryList(page: number, size: number) {
  const query = useQuery({
    queryKey: ["categories", { page, size }],
    queryFn: () =>
      apiClient<Page<CategorySummary>>({
        url: "/categories",
        method: "GET",
        params: { page: String(page), size: String(size) },
      }),
  });

  return {
    categories: query.data ?? null,
    isLoading: query.isLoading,
    error: query.error,
  };
}
