import { useState } from "react";
import { usePricingList } from "@/features/pricing/hooks/use-pricing-list";
import type { PricingListFilterValues } from "@/features/pricing/components/pricing-filter";

/** 価格一覧ページの状態（フィルタ・ページ）とデータ取得をまとめる。 */
export function usePricingListPage() {
  const [filter, setFilter] = useState<PricingListFilterValues>({
    productId: undefined,
    level: undefined,
  });
  const [page, setPage] = useState(0);

  const { pricings, totalPages, isLoading, error, refetch } = usePricingList(
    {
      ...(filter.productId !== undefined && { productId: filter.productId }),
      ...(filter.level !== undefined && { level: filter.level }),
    },
    page,
  );

  function handleFilterChange(newFilter: PricingListFilterValues) {
    setFilter(newFilter);
    setPage(0);
  }

  return {
    filter,
    page,
    pricings,
    totalPages,
    isLoading,
    error,
    refetch,
    onFilterChange: handleFilterChange,
    onPageChange: setPage,
  };
}
