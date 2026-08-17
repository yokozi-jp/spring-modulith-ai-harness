import { Link } from "@tanstack/react-router";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { usePricingList } from "@/features/pricing/hooks/use-pricing-list";
import { getPricingLevelLabel } from "@/features/pricing/types/pricing-level";

interface ProductPricingListProps {
  readonly productId: string;
}

/** 商品詳細ページ内の価格一覧。該当商品の価格をテーブル表示する。 */
export function ProductPricingList({ productId }: ProductPricingListProps) {
  const { pricings, isLoading } = usePricingList({ productId });

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-lg">価格一覧</CardTitle>
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
          <p className="text-sm text-muted-foreground">この商品に価格が登録されていません</p>
        )}
        {!isLoading && pricings.length > 0 && (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>レベル</TableHead>
                <TableHead>エリアコード</TableHead>
                <TableHead className="text-right">金額</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {pricings.map((pricing) => (
                <TableRow key={pricing.id ?? ""}>
                  <TableCell>
                    <Link
                      to="/pricings/$id"
                      params={{ id: pricing.id ?? "" }}
                      className="hover:underline"
                    >
                      {getPricingLevelLabel(pricing.level ?? "")}
                    </Link>
                  </TableCell>
                  <TableCell>{pricing.areaCode}</TableCell>
                  <TableCell className="text-right">¥{pricing.amount?.toLocaleString()}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </CardContent>
    </Card>
  );
}
