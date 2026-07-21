import { useState } from "react";
import { Button } from "@/components/ui/button";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { usePricingList } from "@/features/pricing/hooks/use-pricing-list";
import { LEVEL_LABELS } from "@/features/pricing/types/pricing-level";
import type { PricingLevel } from "@/features/pricing/types/pricing-level";
import { PricingFormDialog } from "@/features/product/components/pricing-form-dialog";
import { PricingEditDialog } from "@/features/product/components/pricing-edit-dialog";

interface ProductPricingListProps {
  readonly productId: string;
}

export function ProductPricingList({ productId }: ProductPricingListProps) {
  const { pricings, isLoading } = usePricingList(0, 100, productId);
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [editPricingId, setEditPricingId] = useState<string | null>(null);

  return (
    <div className="space-y-3">
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-semibold">価格一覧</h2>
        <Button
          size="sm"
          onClick={() => {
            setIsCreateOpen(true);
          }}
        >
          価格を追加
        </Button>
      </div>

      {isLoading && (
        <div className="space-y-2">
          <div className="h-8 w-full animate-pulse rounded bg-muted" />
          <div className="h-8 w-full animate-pulse rounded bg-muted" />
        </div>
      )}

      {!isLoading && pricings.length === 0 && (
        <p className="text-sm text-muted-foreground">この商品の価格は登録されていません</p>
      )}

      {!isLoading && pricings.length > 0 && (
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>レベル</TableHead>
              <TableHead>エリア</TableHead>
              <TableHead className="text-right">金額</TableHead>
              <TableHead className="w-24" />
            </TableRow>
          </TableHeader>
          <TableBody>
            {pricings.map((pricing) => (
              <TableRow key={pricing.id}>
                <TableCell>
                  {LEVEL_LABELS[pricing.level as PricingLevel] ?? pricing.level}
                </TableCell>
                <TableCell>{pricing.areaCode}</TableCell>
                <TableCell className="text-right">¥{pricing.amount?.toLocaleString()}</TableCell>
                <TableCell>
                  <Button
                    variant="ghost"
                    size="xs"
                    onClick={() => {
                      setEditPricingId(pricing.id ?? "");
                    }}
                  >
                    詳細
                  </Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}

      <PricingFormDialog
        isOpen={isCreateOpen}
        onClose={() => {
          setIsCreateOpen(false);
        }}
        productId={productId}
      />

      <PricingEditDialog
        pricingId={editPricingId}
        onClose={() => {
          setEditPricingId(null);
        }}
      />
    </div>
  );
}
