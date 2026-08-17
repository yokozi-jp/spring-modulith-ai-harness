import { EmptyState } from "@/components/empty-state";
import { ErrorMessage } from "@/components/error-message";
import { ProductDetailCard } from "@/features/product/components/product-detail-card";
import { ProductDetailHeader } from "@/features/product/components/product-detail-header";
import { ProductDetailSkeleton } from "@/features/product/components/product-detail-skeleton";
import { ProductPricingList } from "@/features/product/components/product-pricing-list";
import { useProduct } from "@/features/product/hooks/use-product";
import { toError } from "@/lib/utils";

interface ProductDetailPageContentProps {
  readonly productId: string;
}

/** 商品詳細ページの状態ハンドリング（Loading/Error/NotFound/Content）。 */
export function ProductDetailPageContent({ productId }: ProductDetailPageContentProps) {
  const { product, isLoading, error, refetch } = useProduct(productId);

  if (isLoading) {
    return <ProductDetailSkeleton />;
  }

  if (error !== null) {
    return (
      <ErrorMessage
        error={toError(error)}
        onRetry={() => {
          void refetch();
        }}
      />
    );
  }

  if (product === null) {
    return <EmptyState message="商品が見つかりません" />;
  }

  return (
    <div className="flex flex-col gap-6">
      <ProductDetailHeader
        productId={product.id ?? ""}
        productName={product.name ?? ""}
        status={product.status ?? ""}
        version={product.version ?? 0}
      />
      <ProductDetailCard product={product} />
      <ProductPricingList productId={product.id ?? ""} />
    </div>
  );
}
