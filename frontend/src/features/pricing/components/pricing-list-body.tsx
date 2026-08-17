import { EmptyState } from "@/components/empty-state";
import { ErrorMessage } from "@/components/error-message";
import { PricingListSkeleton } from "@/features/pricing/components/pricing-list-skeleton";
import { PricingTable } from "@/features/pricing/components/pricing-table";
import { toError } from "@/lib/utils";
import type { PricingSummaryResponse } from "@/api/openAPIDefinition.schemas";

interface PricingListBodyProps {
  readonly pricings: readonly PricingSummaryResponse[];
  readonly isLoading: boolean;
  readonly error: unknown;
  readonly onRetry: () => void;
}

/** 価格一覧本体の状態ハンドリング（Loading/Error/Empty/Content）。 */
export function PricingListBody({ pricings, isLoading, error, onRetry }: PricingListBodyProps) {
  if (isLoading) {
    return <PricingListSkeleton />;
  }

  if (error !== null && error !== undefined) {
    return (
      <ErrorMessage
        error={toError(error)}
        onRetry={() => {
          onRetry();
        }}
      />
    );
  }

  if (pricings.length === 0) {
    return <EmptyState message="価格がありません" />;
  }

  return <PricingTable pricings={pricings} />;
}
