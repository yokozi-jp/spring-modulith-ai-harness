import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useCategoryNameResolver } from "@/features/product/hooks/use-category-name-resolver";
import type { ProductDetailResponse } from "@/api/openAPIDefinition.schemas";

interface ProductDetailCardProps {
  readonly product: ProductDetailResponse;
}

/** 商品詳細の表示本体。 */
export function ProductDetailCard({ product }: ProductDetailCardProps) {
  const { resolveCategoryName } = useCategoryNameResolver();

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-xl">{product.name}</CardTitle>
      </CardHeader>
      <CardContent className="flex flex-col gap-2 text-sm">
        <div className="flex justify-between">
          <span className="text-muted-foreground">カテゴリ</span>
          <span>{resolveCategoryName(product.categoryId ?? "")}</span>
        </div>
        <div className="flex justify-between">
          <span className="text-muted-foreground">SKU</span>
          <span>{product.sku}</span>
        </div>
        <div className="flex flex-col gap-1">
          <span className="text-muted-foreground">商品説明</span>
          <p className="whitespace-pre-wrap">{product.description}</p>
        </div>
      </CardContent>
    </Card>
  );
}
