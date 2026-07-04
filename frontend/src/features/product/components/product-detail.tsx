import { useState } from "react";
import { Link } from "@tanstack/react-router";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { ConfirmDialog } from "@/components/confirm-dialog";
import { ErrorMessage } from "@/components/error-message";
import { toError } from "@/lib/utils";
import { useProduct } from "@/features/product/hooks/use-product";
import { useDeleteProduct } from "@/features/product/hooks/use-delete-product";
import { useProductStatus } from "@/features/product/hooks/use-product-status";
import { StatusBadge } from "@/features/product/components/status-badge";
import { PRODUCT_STATUS } from "@/features/product/types/product-status";
import { ProductPricingList } from "@/features/product/components/product-pricing-list";

interface ProductDetailProps {
  readonly id: string;
}

export function ProductDetail({ id }: ProductDetailProps) {
  const { product, isLoading, error, refetch } = useProduct(id);
  const { deleteProduct, isDeleting, error: deleteError } = useDeleteProduct();
  const {
    publishProduct,
    unpublishProduct,
    archiveProduct,
    isPending: isStatusPending,
    error: statusError,
  } = useProductStatus(id);
  const [isDialogOpen, setIsDialogOpen] = useState(false);

  if (isLoading) {
    return (
      <div className="space-y-4">
        <div className="h-8 w-48 animate-pulse rounded bg-muted" />
        <div className="h-32 animate-pulse rounded-xl bg-muted" />
      </div>
    );
  }

  if (error) {
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
    return <ErrorMessage error={new Error("商品が見つかりません")} />;
  }

  function handleConfirmDelete() {
    if (product === null) {
      return;
    }
    deleteProduct(product.id, product.version);
    setIsDialogOpen(false);
  }

  return (
    <div className="space-y-6">
      {deleteError !== null && deleteError !== undefined && (
        <ErrorMessage error={toError(deleteError)} />
      )}
      {statusError !== null && statusError !== undefined && (
        <ErrorMessage error={toError(statusError)} />
      )}

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div className="space-y-1">
              <CardTitle className="text-xl">{product.name}</CardTitle>
              <CardDescription className="font-mono text-xs">SKU: {product.sku}</CardDescription>
            </div>
            <StatusBadge status={product.status} />
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          <p className="text-sm text-muted-foreground">{product.description}</p>

          <Separator />

          <div className="grid grid-cols-2 gap-4 text-sm">
            <div>
              <p className="text-xs font-medium uppercase text-muted-foreground">カテゴリ ID</p>
              <p className="mt-0.5 font-mono text-xs">{product.categoryId}</p>
            </div>
            <div>
              <p className="text-xs font-medium uppercase text-muted-foreground">バージョン</p>
              <p className="mt-0.5">
                <Badge variant="outline">v{product.version}</Badge>
              </p>
            </div>
          </div>

          <Separator />

          <div className="flex flex-wrap gap-2">
            {product.status !== PRODUCT_STATUS.ARCHIVED && (
              <Button asChild size="sm">
                <Link to="/products/$id/edit" params={{ id }}>
                  編集
                </Link>
              </Button>
            )}

            {product.status === PRODUCT_STATUS.DRAFT && (
              <Button
                variant="outline"
                size="sm"
                onClick={() => {
                  publishProduct(product.version);
                }}
                disabled={isStatusPending}
              >
                公開する
              </Button>
            )}
            {product.status === PRODUCT_STATUS.PUBLISHED && (
              <Button
                variant="outline"
                size="sm"
                onClick={() => {
                  unpublishProduct(product.version);
                }}
                disabled={isStatusPending}
              >
                非公開にする
              </Button>
            )}
            {product.status !== PRODUCT_STATUS.ARCHIVED && (
              <Button
                variant="outline"
                size="sm"
                onClick={() => {
                  archiveProduct(product.version);
                }}
                disabled={isStatusPending}
              >
                アーカイブ
              </Button>
            )}

            <Button
              variant="destructive"
              size="sm"
              onClick={() => {
                setIsDialogOpen(true);
              }}
            >
              削除
            </Button>
          </div>
        </CardContent>
      </Card>

      <ProductPricingList productId={product.id} />

      <ConfirmDialog
        isOpen={isDialogOpen}
        onClose={() => {
          setIsDialogOpen(false);
        }}
        onConfirm={handleConfirmDelete}
        title="商品を削除"
        message={`「${product.name}」を削除しますか？この操作は取り消せません。`}
        confirmLabel="削除する"
        isLoading={isDeleting}
      />
    </div>
  );
}
