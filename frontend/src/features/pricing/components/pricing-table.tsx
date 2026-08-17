import { Link } from "@tanstack/react-router";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { getPricingLevelLabel } from "@/features/pricing/types/pricing-level";
import type { PricingSummaryResponse } from "@/api/openAPIDefinition.schemas";

interface PricingTableProps {
  readonly pricings: readonly PricingSummaryResponse[];
}

/** 価格一覧テーブル。行クリックで詳細ページへ遷移する。 */
export function PricingTable({ pricings }: PricingTableProps) {
  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>商品ID</TableHead>
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
                {pricing.productId}
              </Link>
            </TableCell>
            <TableCell>{getPricingLevelLabel(pricing.level ?? "")}</TableCell>
            <TableCell>{pricing.areaCode}</TableCell>
            <TableCell className="text-right">{pricing.amount?.toLocaleString()}</TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  );
}
