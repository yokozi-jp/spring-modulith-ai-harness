import { Link } from "@tanstack/react-router";
import { Badge } from "@/components/ui/badge";
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
import { useProductList } from "@/features/product/hooks/use-product-list";
import {
  getProductStatusColor,
  getProductStatusLabel,
} from "@/features/product/types/product-status";

interface CategoryProductListProps {
  readonly categoryId: string;
}

/** カテゴリ詳細ページ内の商品一覧。該当カテゴリの商品をテーブル表示する。 */
export function CategoryProductList({ categoryId }: CategoryProductListProps) {
  const { products, isLoading } = useProductList({ categoryId });

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-lg">商品一覧</CardTitle>
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
          <p className="text-sm text-muted-foreground">このカテゴリに商品がありません</p>
        )}
        {!isLoading && products.length > 0 && (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>商品名</TableHead>
                <TableHead>ステータス</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {products.map((product) => (
                <TableRow key={product.id ?? ""}>
                  <TableCell>
                    <Link
                      to="/products/$id"
                      params={{ id: product.id ?? "" }}
                      className="hover:underline"
                    >
                      {product.name}
                    </Link>
                  </TableCell>
                  <TableCell>
                    <Badge className={getProductStatusColor(product.status ?? "")}>
                      {getProductStatusLabel(product.status ?? "")}
                    </Badge>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </CardContent>
    </Card>
  );
}
