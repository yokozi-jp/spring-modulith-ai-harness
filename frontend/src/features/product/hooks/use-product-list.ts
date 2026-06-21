import { useQuery } from "@tanstack/react-query";
import { apiClient } from "@/lib/api-client";
import type { ProductPage } from "@/features/product/types/product";

export function useProductList(page: number, size: number) {
  const query = useQuery({
    queryKey: ["products", { page, size }],
    queryFn: () =>
      apiClient<ProductPage>({
        url: "/products",
        method: "GET",
        params: { page: String(page), size: String(size) },
      }),
  });

  return {
    products: query.data ?? null,
    isLoading: query.isLoading,
    error: query.error,
  };
}
