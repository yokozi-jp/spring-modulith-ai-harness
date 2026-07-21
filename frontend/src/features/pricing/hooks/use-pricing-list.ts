import { useList1 } from "@/api/pricing/pricing";
import type { PricingSummaryResponse } from "@/api/openAPIDefinition.schemas";

export function usePricingList(page: number = 0, size: number = 20, productId?: string) {
  const query = useList1({
    param: {
      ...(productId !== undefined && { productId }),
    },
    pageable: { page, size },
  });

  return {
    pricings: (query.data?.data?.content ?? []) as PricingSummaryResponse[],
    totalPages: query.data?.data?.totalPages ?? 0,
    totalElements: query.data?.data?.totalElements ?? 0,
    isLoading: query.isLoading,
    error: query.error,
  };
}
