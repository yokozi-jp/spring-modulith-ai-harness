import { useQuery } from "@tanstack/react-query";
import { apiClient } from "@/lib/api-client";
import type { CategorySummary, Page } from "@/features/category/types/category";

export function useCategoryOptions() {
  const query = useQuery({
    queryKey: ["categories", "options"],
    queryFn: () =>
      apiClient<Page<CategorySummary>>({
        url: "/categories",
        method: "GET",
        params: { page: "0", size: "1000" },
      }),
  });

  return {
    options: query.data?.content ?? [],
    isLoading: query.isLoading,
  };
}
