import { createFileRoute } from "@tanstack/react-router";
import { PricingForm } from "@/features/pricing/components/pricing-form";
import { useCreatePricing } from "@/features/pricing/hooks/use-create-pricing";
import type { PricingFormValues } from "@/features/pricing/components/pricing-form";

export const Route = createFileRoute("/pricings_/new")({
  component: NewPricingPage,
});

function NewPricingPage() {
  const { createPricing, isCreating } = useCreatePricing();

  function handleSubmit(values: PricingFormValues) {
    createPricing({
      productId: values.productId,
      level: values.level,
      areaCode: values.areaCode,
      amount: values.amount,
      validFrom: values.validFrom,
      ...(values.validTo !== undefined && { validTo: values.validTo }),
    });
  }

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-2xl font-bold tracking-tight">価格を作成</h1>
      <PricingForm
        showIdentityFields
        onSubmit={handleSubmit}
        isSubmitting={isCreating}
        submitLabel="作成"
      />
    </div>
  );
}
