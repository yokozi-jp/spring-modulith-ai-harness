import { useListProduct } from "@/api/product/product";

/** 商品一覧のフィルタ条件。 */
export interface ProductListFilter {
  readonly categoryId?: string;
  readonly status?: string;
}

/** 商品一覧を取得する。categoryId/status でフィルタ可能。 */
export function useProductList(filter: ProductListFilter, page = 0, size = 20) {
  const query = useListProduct(
    {
      ...(filter.categoryId !== undefined && { categoryId: filter.categoryId }),
      ...(filter.status !== undefined && { status: filter.status }),
      page,
      size,
      sort: ["createdAt,desc"],
    },
    {
      query: { queryKey: ["products", { filter, page, size }] },
    },
  );

  return {
    products: query.data?.data?.content ?? [],
    totalPages: query.data?.data?.totalPages ?? 0,
    totalElements: query.data?.data?.totalElements ?? 0,
    isLoading: query.isLoading,
    error: query.error,
    refetch: query.refetch,
  };
}
