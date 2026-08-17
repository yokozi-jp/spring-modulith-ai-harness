import { Link } from "@tanstack/react-router";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { useRecentPricings } from "@/features/dashboard/hooks/use-recent-pricings";
import { getPricingLevelLabel } from "@/features/pricing/types/pricing-level";

/** ダッシュボードの最近の価格リスト。 */
export function RecentPricingsList() {
  const { pricings, isLoading } = useRecentPricings();

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-lg">最近の価格</CardTitle>
      </CardHeader>
      <CardContent>
        {isLoading && (
          <div className="flex flex-col gap-2">
            <Skeleton className="h-5 w-full" />
            <Skeleton className="h-5 w-full" />
            <Skeleton className="h-5 w-full" />
          </div>
        )}
        {!isLoading && pricings.length === 0 && (
          <p className="text-sm text-muted-foreground">価格がまだありません</p>
        )}
        {!isLoading && pricings.length > 0 && (
          <ul className="flex flex-col gap-1">
            {pricings.map((pricing) => (
              <li key={pricing.id ?? ""} className="flex items-center justify-between text-sm">
                <Link
                  to="/pricings/$id"
                  params={{ id: pricing.id ?? "" }}
                  className="hover:underline"
                >
                  {getPricingLevelLabel(pricing.level ?? "")} / {pricing.areaCode}
                </Link>
                <span className="text-muted-foreground">¥{pricing.amount?.toLocaleString()}</span>
              </li>
            ))}
          </ul>
        )}
      </CardContent>
    </Card>
  );
}
