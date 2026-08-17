import { useState } from "react";
import { Link } from "@tanstack/react-router";
import { Button } from "@/components/ui/button";
import { ConfirmDialog } from "@/components/confirm-dialog";
import { useDeletePricing } from "@/features/pricing/hooks/use-delete-pricing";

interface PricingDetailHeaderProps {
  readonly pricingId: string;
  readonly version: number;
}

/** 価格詳細ページの見出し・編集/削除ボタン・削除確認ダイアログ。 */
export function PricingDetailHeader({ pricingId, version }: PricingDetailHeaderProps) {
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const { deletePricing, isDeleting } = useDeletePricing();

  function handleDelete() {
    deletePricing(pricingId, version);
  }

  return (
    <div className="flex items-center justify-between">
      <h1 className="text-2xl font-bold tracking-tight">価格詳細</h1>
      <div className="flex gap-2">
        <Button asChild variant="outline">
          <Link to="/pricings/$id/edit" params={{ id: pricingId }}>
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
        title="価格を削除"
        message="この価格を削除します。この操作は取り消せません。"
        confirmLabel="削除する"
        isLoading={isDeleting}
      />
    </div>
  );
}
