import { useQuery } from "@tanstack/react-query";
import { apiClient } from "@/lib/api-client";
import type { ProductDetail } from "@/features/product/types/product";

export function useProduct(id: string) {
  const query = useQuery({
    queryKey: ["products", id],
    queryFn: () =>
      apiClient<ProductDetail>({
        url: `/products/${id}`,
        method: "GET",
      }),
    enabled: id.length > 0,
  });

  return {
    product: query.data ?? null,
    isLoading: query.isLoading,
    error: query.error,
    refetch: query.refetch,
  };
}
