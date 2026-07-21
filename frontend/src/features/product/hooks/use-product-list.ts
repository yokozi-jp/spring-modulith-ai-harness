import { useList } from "@/api/product/product";
import type { ProductSummaryResponse } from "@/api/openAPIDefinition.schemas";

export function useProductList(
  page: number = 0,
  size: number = 20,
  categoryId?: string,
  status?: string,
) {
  const query = useList({
    param: {
      ...(categoryId !== undefined && { categoryId }),
      ...(status !== undefined && { status }),
    },
    pageable: { page, size, sort: ["name,asc"] },
  });

  return {
    products: (query.data?.data?.content ?? []) as ProductSummaryResponse[],
    totalPages: query.data?.data?.totalPages ?? 0,
    totalElements: query.data?.data?.totalElements ?? 0,
    isLoading: query.isLoading,
    error: query.error,
  };
}
