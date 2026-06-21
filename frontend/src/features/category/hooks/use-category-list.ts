import { useQuery } from "@tanstack/react-query";
import { getCategories } from "@/features/category/types/category-api";

export function useCategoryList(page = 0, size = 20) {
  const query = useQuery({
    queryKey: ["categories", { page, size }],
    queryFn: () => getCategories({ page, size }),
  });

  return {
    categories: query.data?.content ?? [],
    totalPages: query.data?.totalPages ?? 0,
    currentPage: query.data?.number ?? 0,
    isLoading: query.isLoading,
    error: query.error,
  };
}
