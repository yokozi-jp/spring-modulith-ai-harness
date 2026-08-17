import { EmptyState } from "@/components/empty-state";
import { ErrorMessage } from "@/components/error-message";
import { PricingDetailSkeleton } from "@/features/pricing/components/pricing-detail-skeleton";
import { PricingForm } from "@/features/pricing/components/pricing-form";
import { usePricing } from "@/features/pricing/hooks/use-pricing";
import { useUpdatePricing } from "@/features/pricing/hooks/use-update-pricing";
import { toError } from "@/lib/utils";
import type { PricingFormValues } from "@/features/pricing/components/pricing-form";

interface PricingEditPageContentProps {
  readonly pricingId: string;
}

/** 価格編集ページの状態ハンドリング（Loading/Error/NotFound/Content）。 */
export function PricingEditPageContent({ pricingId }: PricingEditPageContentProps) {
  const { pricing, isLoading, error, refetch } = usePricing(pricingId);
  const { updatePricing, isUpdating } = useUpdatePricing(pricingId);

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

  function handleSubmit(values: PricingFormValues) {
    if (pricing === null) {
      return;
    }
    updatePricing({
      amount: values.amount,
      validFrom: values.validFrom,
      version: pricing.version ?? 0,
      ...(values.validTo !== undefined && { validTo: values.validTo }),
    });
  }

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-2xl font-bold tracking-tight">価格を編集</h1>
      <PricingForm
        initialValues={{
          productId: pricing.productId ?? "",
          level: pricing.level ?? "",
          areaCode: pricing.areaCode ?? "",
          amount: pricing.amount ?? 0,
          validFrom: (pricing.validFrom ?? "").slice(0, 10),
          ...(pricing.validTo !== undefined &&
            pricing.validTo !== null && { validTo: pricing.validTo.slice(0, 10) }),
        }}
        showIdentityFields={false}
        onSubmit={handleSubmit}
        isSubmitting={isUpdating}
        submitLabel="更新"
      />
    </div>
  );
}
