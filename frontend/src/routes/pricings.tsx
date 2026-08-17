import { Link, createFileRoute } from "@tanstack/react-router";
import { Button } from "@/components/ui/button";
import { PricingListContent } from "@/features/pricing/components/pricing-list-content";

export const Route = createFileRoute("/pricings")({
  component: PricingsPage,
});

function PricingsPage() {
  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold tracking-tight">価格管理</h1>
        <Button asChild>
          <Link to="/pricings/new">新規作成</Link>
        </Button>
      </div>
      <PricingListContent />
    </div>
  );
}
