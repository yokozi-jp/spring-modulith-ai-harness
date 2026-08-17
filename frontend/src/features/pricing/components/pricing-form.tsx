import type { SubmitEvent } from "react";
import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { useProductOptions } from "@/features/pricing/hooks/use-product-options";
import { PRICING_LEVEL, PRICING_LEVEL_LABELS } from "@/features/pricing/types/pricing-level";

/** 価格フォームの送信値。 */
export interface PricingFormValues {
  readonly productId: string;
  readonly level: string;
  readonly areaCode: string;
  readonly amount: number;
  readonly validFrom: string;
  readonly validTo?: string;
}

interface PricingFormInitialValues {
  readonly productId: string;
  readonly level: string;
  readonly areaCode: string;
  readonly amount: number;
  readonly validFrom: string;
  readonly validTo?: string;
}

interface PricingFormProps {
  readonly initialValues?: PricingFormInitialValues;
  readonly showIdentityFields: boolean;
  readonly onSubmit: (values: PricingFormValues) => void;
  readonly isSubmitting: boolean;
  readonly submitLabel: string;
}

/** 価格作成・編集共通フォーム。編集時は商品/レベル/エリアコードを変更不可にする。 */
export function PricingForm({
  initialValues,
  showIdentityFields,
  onSubmit,
  isSubmitting,
  submitLabel,
}: PricingFormProps) {
  const [productId, setProductId] = useState(initialValues?.productId ?? "");
  const [level, setLevel] = useState(initialValues?.level ?? "");
  const [areaCode, setAreaCode] = useState(initialValues?.areaCode ?? "");
  const [amount, setAmount] = useState(String(initialValues?.amount ?? ""));
  const [validFrom, setValidFrom] = useState(initialValues?.validFrom ?? "");
  const [validTo, setValidTo] = useState(initialValues?.validTo ?? "");
  const { options: productOptions, isLoading: isProductsLoading } = useProductOptions();

  function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault();
    onSubmit({
      productId,
      level,
      areaCode,
      amount: Number(amount),
      validFrom: `${validFrom}T00:00:00Z`,
      ...(validTo.length > 0 && { validTo: `${validTo}T00:00:00Z` }),
    });
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-4">
      {showIdentityFields && (
        <>
          <div className="flex flex-col gap-1">
            <Label htmlFor="pricing-product">商品</Label>
            <Select value={productId} onValueChange={setProductId} disabled={isProductsLoading}>
              <SelectTrigger id="pricing-product" className="w-full">
                <SelectValue placeholder="商品を選択" />
              </SelectTrigger>
              <SelectContent>
                {productOptions.map((option) => (
                  <SelectItem key={option.id} value={option.id}>
                    {option.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="flex flex-col gap-1">
            <Label htmlFor="pricing-level">レベル</Label>
            <Select value={level} onValueChange={setLevel}>
              <SelectTrigger id="pricing-level" className="w-full">
                <SelectValue placeholder="レベルを選択" />
              </SelectTrigger>
              <SelectContent>
                {Object.values(PRICING_LEVEL).map((lv) => (
                  <SelectItem key={lv} value={lv}>
                    {PRICING_LEVEL_LABELS[lv]}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="flex flex-col gap-1">
            <Label htmlFor="pricing-area-code">エリアコード</Label>
            <Input
              id="pricing-area-code"
              type="text"
              value={areaCode}
              onChange={(event) => {
                setAreaCode(event.target.value);
              }}
              required
            />
          </div>
        </>
      )}
      <div className="flex flex-col gap-1">
        <Label htmlFor="pricing-amount">金額</Label>
        <Input
          id="pricing-amount"
          type="number"
          value={amount}
          onChange={(event) => {
            setAmount(event.target.value);
          }}
          required
        />
      </div>
      <div className="flex flex-col gap-1">
        <Label htmlFor="pricing-valid-from">適用開始日</Label>
        <Input
          id="pricing-valid-from"
          type="date"
          value={validFrom}
          onChange={(event) => {
            setValidFrom(event.target.value);
          }}
          required
        />
      </div>
      <div className="flex flex-col gap-1">
        <Label htmlFor="pricing-valid-to">適用終了日（任意）</Label>
        <Input
          id="pricing-valid-to"
          type="date"
          value={validTo}
          onChange={(event) => {
            setValidTo(event.target.value);
          }}
        />
      </div>
      <Button type="submit" disabled={isSubmitting}>
        {isSubmitting ? "送信中..." : submitLabel}
      </Button>
    </form>
  );
}
