import { EmptyState } from "@/components/empty-state";
import { ErrorMessage } from "@/components/error-message";
import { ProductDetailSkeleton } from "@/features/product/components/product-detail-skeleton";
import { ProductForm } from "@/features/product/components/product-form";
import { useProduct } from "@/features/product/hooks/use-product";
import { useUpdateProduct } from "@/features/product/hooks/use-update-product";
import { toError } from "@/lib/utils";
import type { ProductFormValues } from "@/features/product/components/product-form";

interface ProductEditPageContentProps {
  readonly productId: string;
}

/** 商品編集ページの状態ハンドリング（Loading/Error/NotFound/Content）。 */
export function ProductEditPageContent({ productId }: ProductEditPageContentProps) {
  const { product, isLoading, error, refetch } = useProduct(productId);
  const { updateProduct, isUpdating } = useUpdateProduct(productId);

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

  function handleSubmit(values: ProductFormValues) {
    if (product === null) {
      return;
    }
    updateProduct({
      name: values.name,
      description: values.description,
      categoryId: values.categoryId,
      version: product.version ?? 0,
    });
  }

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-2xl font-bold tracking-tight">商品を編集</h1>
      <ProductForm
        initialValues={{
          name: product.name ?? "",
          description: product.description ?? "",
          categoryId: product.categoryId ?? "",
        }}
        showSkuField={false}
        onSubmit={handleSubmit}
        isSubmitting={isUpdating}
        submitLabel="更新"
      />
    </div>
  );
}
