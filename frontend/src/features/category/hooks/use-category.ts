import { useQuery } from "@tanstack/react-query";
import { getCategory } from "@/features/category/types/category-api";

export function useCategory(id: string) {
  const query = useQuery({
    queryKey: ["categories", id],
    queryFn: () => getCategory(id),
    enabled: id.length > 0,
  });

  return {
    category: query.data ?? null,
    isLoading: query.isLoading,
    error: query.error,
  };
}
