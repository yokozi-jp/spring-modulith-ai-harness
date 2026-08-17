import { PricingFilter } from "@/features/pricing/components/pricing-filter";
import { PricingListBody } from "@/features/pricing/components/pricing-list-body";
import { PricingListPagination } from "@/features/pricing/components/pricing-list-pagination";
import { usePricingListPage } from "@/features/pricing/hooks/use-pricing-list-page";

/** 価格一覧ページのコンテンツ（フィルタ・テーブル・ページ送りをまとめる）。 */
export function PricingListContent() {
  const {
    filter,
    page,
    pricings,
    totalPages,
    isLoading,
    error,
    refetch,
    onFilterChange,
    onPageChange,
  } = usePricingListPage();

  return (
    <div className="flex flex-col gap-4">
      <PricingFilter filter={filter} onChange={onFilterChange} />
      <PricingListBody
        pricings={pricings}
        isLoading={isLoading}
        error={error}
        onRetry={() => {
          void refetch();
        }}
      />
      <PricingListPagination page={page} totalPages={totalPages} onPageChange={onPageChange} />
    </div>
  );
}
