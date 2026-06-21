import { useQuery } from "@tanstack/react-query";
import { apiClient } from "@/lib/api-client";
import type { CategoryDetail } from "@/features/category/types/category";

export function useCategory(id: string) {
  const query = useQuery({
    queryKey: ["categories", id],
    queryFn: () =>
      apiClient<CategoryDetail>({
        url: `/categories/${id}`,
        method: "GET",
      }),
    enabled: id.length > 0,
  });

  return {
    category: query.data ?? null,
    isLoading: query.isLoading,
    error: query.error,
    refetch: query.refetch,
  };
}
