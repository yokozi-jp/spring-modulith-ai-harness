import { useState } from "react";
import { Link, useNavigate } from "@tanstack/react-router";
import { Button } from "@/components/ui/button";
import { ErrorMessage } from "@/components/error-message";
import { useProduct } from "@/features/product/hooks/use-product";
import { useDeleteProduct } from "@/features/product/hooks/use-delete-product";
import {
  useArchiveProduct,
  usePublishProduct,
  useUnpublishProduct,
} from "@/features/product/hooks/use-change-product-status";

const STATUS_LABELS: Record<string, string> = {
  DRAFT: "下書き",
  PUBLISHED: "公開中",
  ARCHIVED: "アーカイブ",
};

interface ProductDetailProps {
  readonly id: string;
}

export function ProductDetail({ id }: ProductDetailProps) {
  const { product, isLoading, error, refetch } = useProduct(id);
  const { deleteProduct, isDeleting } = useDeleteProduct();
  const { publish, isPending: isPublishing } = usePublishProduct();
  const { unpublish, isPending: isUnpublishing } = useUnpublishProduct();
  const { archive, isPending: isArchiving } = useArchiveProduct();
  const navigate = useNavigate();
  const [actionError, setActionError] = useState<string | null>(null);

  if (isLoading) {
    return <div className="h-32 animate-pulse rounded-md bg-muted" />;
  }

  if (error) {
    return <ErrorMessage error={error} onRetry={refetch} />;
  }

  if (product === null) {
    return <ErrorMessage error={new Error("商品が見つかりません")} />;
  }

  function handleDelete() {
    if (product === null) {
      return;
    }
    setActionError(null);
    deleteProduct(
      { id: product.id, version: product.version },
      {
        onSuccess: () => {
          void navigate({ to: "/products" });
        },
        onError: (err) => {
          setActionError(err.message);
        },
      },
    );
  }

  return (
    <div className="space-y-6">
      <dl className="space-y-3">
        <div>
          <dt className="text-sm text-muted-foreground">商品名</dt>
          <dd className="text-lg font-medium">{product.name}</dd>
        </div>
        <div>
          <dt className="text-sm text-muted-foreground">商品説明</dt>
          <dd className="whitespace-pre-wrap">{product.description}</dd>
        </div>
        <div>
          <dt className="text-sm text-muted-foreground">カテゴリID</dt>
          <dd>{product.categoryId}</dd>
        </div>
        <div>
          <dt className="text-sm text-muted-foreground">SKU</dt>
          <dd>{product.sku}</dd>
        </div>
        <div>
          <dt className="text-sm text-muted-foreground">ステータス</dt>
          <dd>{STATUS_LABELS[product.status] ?? product.status}</dd>
        </div>
        <div>
          <dt className="text-sm text-muted-foreground">バージョン</dt>
          <dd>{product.version}</dd>
        </div>
      </dl>

      {actionError !== null && (
        <div className="rounded-md border border-destructive/50 p-3">
          <p className="text-sm text-destructive">{actionError}</p>
        </div>
      )}

      <div className="flex flex-wrap gap-2">
        {product.status === "DRAFT" && (
          <Button
            onClick={() => {
              publish(
                { id: product.id, version: product.version },
                {
                  onError: (err) => {
                    setActionError(err.message);
                  },
                },
              );
            }}
            disabled={isPublishing}
          >
            公開する
          </Button>
        )}
        {product.status === "PUBLISHED" && (
          <Button
            variant="outline"
            onClick={() => {
              unpublish(
                { id: product.id, version: product.version },
                {
                  onError: (err) => {
                    setActionError(err.message);
                  },
                },
              );
            }}
            disabled={isUnpublishing}
          >
            非公開にする
          </Button>
        )}
        {product.status !== "ARCHIVED" && (
          <Button
            variant="secondary"
            onClick={() => {
              archive(
                { id: product.id, version: product.version },
                {
                  onError: (err) => {
                    setActionError(err.message);
                  },
                },
              );
            }}
            disabled={isArchiving}
          >
            アーカイブ
          </Button>
        )}
        <Button asChild>
          <Link to="/products/$id/edit" params={{ id: product.id }}>
            編集
          </Link>
        </Button>
        <Button variant="destructive" onClick={handleDelete} disabled={isDeleting}>
          {isDeleting ? "削除中..." : "削除"}
        </Button>
      </div>
    </div>
  );
}
