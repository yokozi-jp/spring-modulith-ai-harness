import { EmptyState } from "@/components/empty-state";
import { ErrorMessage } from "@/components/error-message";
import { PricingDetailCard } from "@/features/pricing/components/pricing-detail-card";
import { PricingDetailHeader } from "@/features/pricing/components/pricing-detail-header";
import { PricingDetailSkeleton } from "@/features/pricing/components/pricing-detail-skeleton";
import { usePricing } from "@/features/pricing/hooks/use-pricing";
import { toError } from "@/lib/utils";

interface PricingDetailPageContentProps {
  readonly pricingId: string;
}

/** 価格詳細ページの状態ハンドリング（Loading/Error/NotFound/Content）。 */
export function PricingDetailPageContent({ pricingId }: PricingDetailPageContentProps) {
  const { pricing, isLoading, error, refetch } = usePricing(pricingId);

  if (isLoading) {
    return <PricingDetailSkeleton />;
  }

  if (error !== null) {
    return (
      <ErrorMessage
        error={toError(error)}
        onRetry={() => {
          void refetch();
        }}
      />
    );
  }

  if (pricing === null) {
    return <EmptyState message="価格が見つかりません" />;
  }

  return (
    <div className="flex flex-col gap-6">
      <PricingDetailHeader pricingId={pricing.id ?? ""} version={pricing.version ?? 0} />
      <PricingDetailCard pricing={pricing} />
    </div>
  );
}
