import { usePricingList } from "@/features/pricing/hooks/use-pricing-list";

/** ダッシュボード用に最近の価格を5件取得する。 */
export function useRecentPricings() {
  const { pricings, isLoading, error } = usePricingList({}, 0, 5);

  return { pricings, isLoading, error };
}
