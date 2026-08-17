import { useFindPricingById } from "@/api/pricing/pricing";

/** 価格詳細を取得する。id が空文字の場合は実行しない。 */
export function usePricing(id: string) {
  const query = useFindPricingById(id, {
    query: {
      queryKey: ["pricings", id],
      enabled: id.length > 0,
    },
  });

  return {
    pricing: query.data?.data ?? null,
    isLoading: query.isLoading,
    error: query.error,
    refetch: query.refetch,
  };
}
