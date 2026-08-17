import { createFileRoute } from "@tanstack/react-router";
import { PricingEditPageContent } from "@/features/pricing/components/pricing-edit-page-content";

export const Route = createFileRoute("/pricings_/$id_/edit")({
  component: PricingEditPage,
});

function PricingEditPage() {
  const { id } = Route.useParams();

  return <PricingEditPageContent pricingId={id} />;
}
