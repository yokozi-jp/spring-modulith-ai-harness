import { Link } from "@tanstack/react-router";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { useRecentProducts } from "@/features/dashboard/hooks/use-recent-products";

/** ダッシュボードの最近の商品リスト。 */
export function RecentProductsList() {
  const { products, isLoading } = useRecentProducts();

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-lg">最近の商品</CardTitle>
      </CardHeader>
      <CardContent>
        {isLoading && (
          <div className="flex flex-col gap-2">
            <Skeleton className="h-5 w-full" />
            <Skeleton className="h-5 w-full" />
            <Skeleton className="h-5 w-full" />
          </div>
        )}
        {!isLoading && products.length === 0 && (
          <p className="text-sm text-muted-foreground">商品がまだありません</p>
        )}
        {!isLoading && products.length > 0 && (
          <ul className="flex flex-col gap-1">
            {products.map((product) => (
              <li key={product.id ?? ""}>
                <Link
                  to="/products/$id"
                  params={{ id: product.id ?? "" }}
                  className="text-sm hover:underline"
                >
                  {product.name}
                </Link>
              </li>
            ))}
          </ul>
        )}
      </CardContent>
    </Card>
  );
}
