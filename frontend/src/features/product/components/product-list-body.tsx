import { EmptyState } from "@/components/empty-state";
import { ErrorMessage } from "@/components/error-message";
import { ProductListSkeleton } from "@/features/product/components/product-list-skeleton";
import { ProductTable } from "@/features/product/components/product-table";
import { toError } from "@/lib/utils";
import type { ProductSummaryResponse } from "@/api/openAPIDefinition.schemas";

interface ProductListBodyProps {
  readonly products: readonly ProductSummaryResponse[];
  readonly isLoading: boolean;
  readonly error: unknown;
  readonly onRetry: () => void;
}

/** 商品一覧本体の状態ハンドリング（Loading/Error/Empty/Content）。 */
export function ProductListBody({ products, isLoading, error, onRetry }: ProductListBodyProps) {
  if (isLoading) {
    return <ProductListSkeleton />;
  }

  if (error !== null && error !== undefined) {
    return (
      <ErrorMessage
        error={toError(error)}
        onRetry={() => {
          onRetry();
        }}
      />
    );
  }

  if (products.length === 0) {
    return <EmptyState message="商品がありません" />;
  }

  return <ProductTable products={products} />;
}
