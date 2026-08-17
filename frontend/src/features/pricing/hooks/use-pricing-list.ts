import { useListPricing } from "@/api/pricing/pricing";

/** 価格一覧のフィルタ条件。 */
export interface PricingListFilter {
  readonly productId?: string;
  readonly level?: string;
  readonly areaCode?: string;
}

/** 価格一覧を取得する。productId/level/areaCode でフィルタ可能。 */
export function usePricingList(filter: PricingListFilter, page = 0, size = 20) {
  const query = useListPricing(
    {
      ...(filter.productId !== undefined && { productId: filter.productId }),
      ...(filter.level !== undefined && { level: filter.level }),
      ...(filter.areaCode !== undefined && { areaCode: filter.areaCode }),
      page,
      size,
      sort: ["createdAt,desc"],
    },
    {
      query: { queryKey: ["pricings", { filter, page, size }] },
    },
  );

  return {
    pricings: query.data?.data?.content ?? [],
    totalPages: query.data?.data?.totalPages ?? 0,
    totalElements: query.data?.data?.totalElements ?? 0,
    isLoading: query.isLoading,
    error: query.error,
    refetch: query.refetch,
  };
}
