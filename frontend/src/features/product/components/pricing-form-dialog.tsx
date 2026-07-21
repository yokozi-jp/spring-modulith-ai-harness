import { useEffect, useState } from "react";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { ErrorMessage } from "@/components/error-message";
import { toError } from "@/lib/utils";
import { useCreatePricing } from "@/features/pricing/hooks/use-create-pricing";
import { PRICING_LEVEL, LEVEL_LABELS } from "@/features/pricing/types/pricing-level";
import type { PricingLevel } from "@/features/pricing/types/pricing-level";

interface PricingFormDialogProps {
  readonly isOpen: boolean;
  readonly onClose: () => void;
  readonly productId: string;
}

export function PricingFormDialog({ isOpen, onClose, productId }: PricingFormDialogProps) {
  const { createPricing, isCreating, isSuccess, error, reset } = useCreatePricing();
  const [level, setLevel] = useState("");
  const [areaCode, setAreaCode] = useState("");
  const [amount, setAmount] = useState("");
  const [validFrom, setValidFrom] = useState("");
  const [validTo, setValidTo] = useState("");

  useEffect(() => {
    if (isOpen) {
      setLevel("");
      setAreaCode("");
      setAmount("");
      setValidFrom("");
      setValidTo("");
      reset();
    }
  }, [isOpen, reset]);

  useEffect(() => {
    if (isSuccess) {
      onClose();
    }
  }, [isSuccess, onClose]);

  function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    createPricing({
      productId,
      level,
      areaCode,
      amount: Number(amount),
      validFrom: new Date(validFrom).toISOString(),
      ...(validTo !== "" && { validTo: new Date(validTo).toISOString() }),
    });
  }

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>価格を追加</DialogTitle>
          <DialogDescription>この商品の新しい価格を登録します。</DialogDescription>
        </DialogHeader>

        {error !== null && error !== undefined && <ErrorMessage error={toError(error)} />}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <label htmlFor="pricing-level" className="text-sm font-medium">
                レベル
              </label>
              <select
                id="pricing-level"
                value={level}
                onChange={(e) => {
                  setLevel(e.target.value);
                }}
                required
                className="w-full rounded-md border px-3 py-2"
              >
                <option value="">選択</option>
                {Object.entries(PRICING_LEVEL).map(([, value]) => (
                  <option key={value} value={value}>
                    {LEVEL_LABELS[value as PricingLevel]}
                  </option>
                ))}
              </select>
            </div>

            <div className="space-y-2">
              <label htmlFor="pricing-area" className="text-sm font-medium">
                エリアコード
              </label>
              <input
                id="pricing-area"
                type="text"
                value={areaCode}
                onChange={(e) => {
                  setAreaCode(e.target.value);
                }}
                required
                className="w-full rounded-md border px-3 py-2"
                placeholder="JP-13"
              />
            </div>
          </div>

          <div className="space-y-2">
            <label htmlFor="pricing-amount" className="text-sm font-medium">
              金額
            </label>
            <input
              id="pricing-amount"
              type="number"
              value={amount}
              onChange={(e) => {
                setAmount(e.target.value);
              }}
              required
              min={1}
              step="0.01"
              className="w-full rounded-md border px-3 py-2"
              placeholder="1000"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <label htmlFor="pricing-from" className="text-sm font-medium">
                有効開始
              </label>
              <input
                id="pricing-from"
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
              <label htmlFor="pricing-to" className="text-sm font-medium">
                有効終了（任意）
              </label>
              <input
                id="pricing-to"
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
            <Button type="button" variant="outline" onClick={onClose} disabled={isCreating}>
              キャンセル
            </Button>
            <Button type="submit" disabled={isCreating}>
              {isCreating ? "追加中..." : "追加する"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
