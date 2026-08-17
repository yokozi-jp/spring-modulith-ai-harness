import { createFileRoute } from "@tanstack/react-router";
import { DashboardQuickLinks } from "@/features/dashboard/components/dashboard-quick-links";
import { RecentProductsList } from "@/features/dashboard/components/recent-products-list";
import { RecentPricingsList } from "@/features/dashboard/components/recent-pricings-list";

export const Route = createFileRoute("/")({
  component: DashboardPage,
});

function DashboardPage() {
  return (
    <div className="flex flex-col gap-8">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">ダッシュボード</h1>
        <p className="text-muted-foreground">Spring Modulith AI Harness の管理画面です。</p>
      </div>
      <DashboardQuickLinks />
      <div className="grid gap-6 md:grid-cols-2">
        <RecentProductsList />
        <RecentPricingsList />
      </div>
    </div>
  );
}
