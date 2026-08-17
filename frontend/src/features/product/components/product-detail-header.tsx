import { useState } from "react";
import { Link } from "@tanstack/react-router";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { ConfirmDialog } from "@/components/confirm-dialog";
import { useArchiveProduct } from "@/features/product/hooks/use-archive-product";
import { useDeleteProduct } from "@/features/product/hooks/use-delete-product";
import { usePublishProduct } from "@/features/product/hooks/use-publish-product";
import { useUnpublishProduct } from "@/features/product/hooks/use-unpublish-product";
import {
  PRODUCT_STATUS,
  getProductStatusColor,
  getProductStatusLabel,
} from "@/features/product/types/product-status";

interface ProductDetailHeaderProps {
  readonly productId: string;
  readonly productName: string;
  readonly status: string;
  readonly version: number;
}

/** 商品詳細ページの見出し・ステータスバッジ・各操作ボタン・削除確認ダイアログ。 */
export function ProductDetailHeader({
  productId,
  productName,
  status,
  version,
}: ProductDetailHeaderProps) {
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const { deleteProduct, isDeleting } = useDeleteProduct();
  const { publishProduct, isPublishing } = usePublishProduct(productId);
  const { unpublishProduct, isUnpublishing } = useUnpublishProduct(productId);
  const { archiveProduct, isArchiving } = useArchiveProduct(productId);

  function handleDelete() {
    deleteProduct(productId, version);
  }

  function handlePublish() {
    publishProduct(version);
  }

  function handleUnpublish() {
    unpublishProduct(version);
  }

  function handleArchive() {
    archiveProduct(version);
  }

  return (
    <div className="flex items-center justify-between">
      <div className="flex items-center gap-3">
        <h1 className="text-2xl font-bold tracking-tight">{productName}</h1>
        <Badge className={getProductStatusColor(status)}>{getProductStatusLabel(status)}</Badge>
      </div>
      <div className="flex gap-2">
        {status === PRODUCT_STATUS.DRAFT && (
          <Button type="button" variant="outline" onClick={handlePublish} disabled={isPublishing}>
            公開
          </Button>
        )}
        {status === PRODUCT_STATUS.PUBLISHED && (
          <Button
            type="button"
            variant="outline"
            onClick={handleUnpublish}
            disabled={isUnpublishing}
          >
            非公開にする
          </Button>
        )}
        {status !== PRODUCT_STATUS.ARCHIVED && (
          <Button type="button" variant="outline" onClick={handleArchive} disabled={isArchiving}>
            アーカイブ
          </Button>
        )}
        <Button asChild variant="outline">
          <Link to="/products/$id/edit" params={{ id: productId }}>
            編集
          </Link>
        </Button>
        <Button
          type="button"
          variant="destructive"
          onClick={() => {
            setIsDeleteDialogOpen(true);
          }}
        >
          削除
        </Button>
      </div>
      <ConfirmDialog
        isOpen={isDeleteDialogOpen}
        onClose={() => {
          setIsDeleteDialogOpen(false);
        }}
        onConfirm={handleDelete}
        title="商品を削除"
        message={`「${productName}」を削除します。この操作は取り消せません。`}
        confirmLabel="削除する"
        isLoading={isDeleting}
      />
    </div>
  );
}
