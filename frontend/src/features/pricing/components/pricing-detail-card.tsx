import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { getPricingLevelLabel } from "@/features/pricing/types/pricing-level";
import type { PricingDetailResponse } from "@/api/openAPIDefinition.schemas";

interface PricingDetailCardProps {
  readonly pricing: PricingDetailResponse;
}

/** 価格詳細の表示本体。 */
export function PricingDetailCard({ pricing }: PricingDetailCardProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-xl">価格情報</CardTitle>
      </CardHeader>
      <CardContent className="flex flex-col gap-2 text-sm">
        <div className="flex justify-between">
          <span className="text-muted-foreground">商品ID</span>
          <span>{pricing.productId}</span>
        </div>
        <div className="flex justify-between">
          <span className="text-muted-foreground">レベル</span>
          <span>{getPricingLevelLabel(pricing.level ?? "")}</span>
        </div>
        <div className="flex justify-between">
          <span className="text-muted-foreground">エリアコード</span>
          <span>{pricing.areaCode}</span>
        </div>
        <div className="flex justify-between">
          <span className="text-muted-foreground">金額</span>
          <span>{pricing.amount?.toLocaleString()}</span>
        </div>
        <div className="flex justify-between">
          <span className="text-muted-foreground">適用開始日</span>
          <span>{(pricing.validFrom ?? "").slice(0, 10)}</span>
        </div>
        <div className="flex justify-between">
          <span className="text-muted-foreground">適用終了日</span>
          <span>
            {pricing.validTo !== undefined && pricing.validTo !== null
              ? pricing.validTo.slice(0, 10)
              : "—"}
          </span>
        </div>
      </CardContent>
    </Card>
  );
}
