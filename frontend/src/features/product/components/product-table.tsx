import { Link } from "@tanstack/react-router";
import { Badge } from "@/components/ui/badge";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { useCategoryNameResolver } from "@/features/product/hooks/use-category-name-resolver";
import {
  getProductStatusColor,
  getProductStatusLabel,
} from "@/features/product/types/product-status";
import type { ProductSummaryResponse } from "@/api/openAPIDefinition.schemas";

interface ProductTableProps {
  readonly products: readonly ProductSummaryResponse[];
}

/** 商品一覧テーブル。行クリックで詳細ページへ遷移する。 */
export function ProductTable({ products }: ProductTableProps) {
  const { resolveCategoryName } = useCategoryNameResolver();

  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>商品名</TableHead>
          <TableHead>カテゴリ</TableHead>
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
            <TableCell>{resolveCategoryName(product.categoryId ?? "")}</TableCell>
            <TableCell>
              <Badge className={getProductStatusColor(product.status ?? "")}>
                {getProductStatusLabel(product.status ?? "")}
              </Badge>
            </TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  );
}
