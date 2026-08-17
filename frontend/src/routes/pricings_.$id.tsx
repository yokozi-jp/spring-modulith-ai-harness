import { createFileRoute } from "@tanstack/react-router";
import { PricingDetailPageContent } from "@/features/pricing/components/pricing-detail-page-content";

export const Route = createFileRoute("/pricings_/$id")({
  component: PricingDetailPage,
});

function PricingDetailPage() {
  const { id } = Route.useParams();

  return <PricingDetailPageContent pricingId={id} />;
}
