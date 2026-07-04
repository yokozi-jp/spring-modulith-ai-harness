import { useEffect, useState } from "react";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { ErrorMessage } from "@/components/error-message";
import { toError } from "@/lib/utils";
import { usePricing } from "@/features/pricing/hooks/use-pricing";
import { useUpdatePricing } from "@/features/pricing/hooks/use-update-pricing";
import { useDeletePricing } from "@/features/pricing/hooks/use-delete-pricing";
import { LEVEL_LABELS } from "@/features/pricing/types/pricing-level";
import type { PricingLevel } from "@/features/pricing/types/pricing-level";

interface PricingEditDialogProps {
  readonly pricingId: string | null;
  readonly onClose: () => void;
}

function toDatetimeLocal(isoString: string): string {
  if (isoString === "") {
    return "";
  }
  const date = new Date(isoString);
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${String(date.getFullYear())}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

function formatDateTime(isoString: string): string {
  if (isoString === "") {
    return "—";
  }
  return new Date(isoString).toLocaleString("ja-JP");
}

export function PricingEditDialog({ pricingId, onClose }: PricingEditDialogProps) {
  const { pricing, isLoading } = usePricing(pricingId ?? "");
  const {
    updatePricing,
    isUpdating,
    isSuccess: isUpdateSuccess,
    error: updateError,
    reset: resetUpdate,
  } = useUpdatePricing(pricingId ?? "");
  const { deletePricing, isDeleting, error: deleteError } = useDeletePricing();
  const [amount, setAmount] = useState("");
  const [validFrom, setValidFrom] = useState("");
  const [validTo, setValidTo] = useState("");
  const [isEditing, setIsEditing] = useState(false);
  const [isConfirmingDelete, setIsConfirmingDelete] = useState(false);

  useEffect(() => {
    if (pricing !== null) {
      setAmount(String(pricing.amount));
      setValidFrom(toDatetimeLocal(pricing.validFrom));
      setValidTo(toDatetimeLocal(pricing.validTo ?? ""));
      setIsEditing(false);
      setIsConfirmingDelete(false);
      resetUpdate();
    }
  }, [pricing, resetUpdate]);

  useEffect(() => {
    if (isUpdateSuccess) {
      setIsEditing(false);
    }
  }, [isUpdateSuccess]);

  function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (pricing === null) {
      return;
    }
    updatePricing({
      amount: Number(amount),
      validFrom: new Date(validFrom).toISOString(),
      ...(validTo !== "" && { validTo: new Date(validTo).toISOString() }),
      version: pricing.version,
    });
  }

  function handleDelete() {
    if (pricing === null) {
      return;
    }
    deletePricing(pricing.id, pricing.version);
    onClose();
  }

  const combinedError = updateError ?? deleteError;

  return (
    <Dialog open={pricingId !== null} onOpenChange={onClose}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>価格の詳細</DialogTitle>
        </DialogHeader>

        {isLoading && (
          <div className="space-y-3">
            <div className="h-6 w-32 animate-pulse rounded bg-muted" />
            <div className="h-6 w-48 animate-pulse rounded bg-muted" />
          </div>
        )}

        {combinedError !== null && combinedError !== undefined && (
          <ErrorMessage error={toError(combinedError)} />
        )}

        {!isLoading && pricing !== null && !isEditing && !isConfirmingDelete && (
          <div className="space-y-4">
            <dl className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-2 text-sm">
              <dt className="font-medium text-muted-foreground">レベル</dt>
              <dd>{LEVEL_LABELS[pricing.level as PricingLevel] ?? pricing.level}</dd>

              <dt className="font-medium text-muted-foreground">エリア</dt>
              <dd>{pricing.areaCode}</dd>

              <dt className="font-medium text-muted-foreground">金額</dt>
              <dd className="font-semibold">¥{pricing.amount.toLocaleString()}</dd>

              <dt className="font-medium text-muted-foreground">有効開始</dt>
              <dd>{formatDateTime(pricing.validFrom)}</dd>

              <dt className="font-medium text-muted-foreground">有効終了</dt>
              <dd>{pricing.validTo !== null ? formatDateTime(pricing.validTo) : "無期限"}</dd>
            </dl>

            <DialogFooter className="flex justify-between">
              <Button
                variant="destructive"
                size="sm"
                onClick={() => {
                  setIsConfirmingDelete(true);
                }}
              >
                削除
              </Button>
              <Button
                onClick={() => {
                  setIsEditing(true);
                }}
              >
                編集する
              </Button>
            </DialogFooter>
          </div>
        )}

        {!isLoading && pricing !== null && isConfirmingDelete && (
          <div className="space-y-4">
            <p className="text-sm">この価格を削除しますか？この操作は取り消せません。</p>
            <DialogFooter>
              <Button
                variant="outline"
                onClick={() => {
                  setIsConfirmingDelete(false);
                }}
                disabled={isDeleting}
              >
                キャンセル
              </Button>
              <Button variant="destructive" onClick={handleDelete} disabled={isDeleting}>
                {isDeleting ? "削除中..." : "削除する"}
              </Button>
            </DialogFooter>
          </div>
        )}

        {!isLoading && pricing !== null && isEditing && (
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <label htmlFor="edit-amount" className="text-sm font-medium">
                金額
              </label>
              <input
                id="edit-amount"
                type="number"
                value={amount}
                onChange={(e) => {
                  setAmount(e.target.value);
                }}
                required
                min={1}
                step="0.01"
                className="w-full rounded-md border px-3 py-2"
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <label htmlFor="edit-from" className="text-sm font-medium">
                  有効開始
                </label>
                <input
                  id="edit-from"
                  type="datetime-local"
                  value={validFrom}
                  onChange={(e) => {
                    setValidFrom(e.target.value);
                  }}
                  required
                  className="w-full rounded-md border px-3 py-2"
                />
              </div>

              <div className="space-y-2">
                <label htmlFor="edit-to" className="text-sm font-medium">
                  有効終了（任意）
                </label>
                <input
                  id="edit-to"
                  type="datetime-local"
                  value={validTo}
                  onChange={(e) => {
                    setValidTo(e.target.value);
                  }}
                  className="w-full rounded-md border px-3 py-2"
                />
              </div>
            </div>

            <DialogFooter>
              <Button
                type="button"
                variant="outline"
                onClick={() => {
                  setIsEditing(false);
                }}
                disabled={isUpdating}
              >
                キャンセル
              </Button>
              <Button type="submit" disabled={isUpdating}>
                {isUpdating ? "更新中..." : "更新する"}
              </Button>
            </DialogFooter>
          </form>
        )}
      </DialogContent>
    </Dialog>
  );
}
