import type { SubmitEvent } from "react";
import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { useCategoryOptions } from "@/features/category/hooks/use-category-options";

/** 商品フォームの送信値。 */
export interface ProductFormValues {
  readonly name: string;
  readonly description: string;
  readonly categoryId: string;
  readonly sku?: string;
}

interface ProductFormInitialValues {
  readonly name: string;
  readonly description: string;
  readonly categoryId: string;
}

interface ProductFormProps {
  readonly initialValues?: ProductFormInitialValues;
  readonly showSkuField: boolean;
  readonly onSubmit: (values: ProductFormValues) => void;
  readonly isSubmitting: boolean;
  readonly submitLabel: string;
}

/** 商品作成・編集共通フォーム。作成時のみ SKU 入力を表示する。 */
export function ProductForm({
  initialValues,
  showSkuField,
  onSubmit,
  isSubmitting,
  submitLabel,
}: ProductFormProps) {
  const [name, setName] = useState(initialValues?.name ?? "");
  const [description, setDescription] = useState(initialValues?.description ?? "");
  const [categoryId, setCategoryId] = useState(initialValues?.categoryId ?? "");
  const [sku, setSku] = useState("");
  const { options, isLoading: isOptionsLoading } = useCategoryOptions();

  function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault();
    onSubmit({
      name,
      description,
      categoryId,
      ...(showSkuField && { sku }),
    });
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-4">
      <div className="flex flex-col gap-1">
        <Label htmlFor="product-name">商品名</Label>
        <Input
          id="product-name"
          type="text"
          value={name}
          onChange={(event) => {
            setName(event.target.value);
          }}
          required
        />
      </div>
      <div className="flex flex-col gap-1">
        <Label htmlFor="product-description">商品説明</Label>
        <Textarea
          id="product-description"
          value={description}
          onChange={(event) => {
            setDescription(event.target.value);
          }}
          required
        />
      </div>
      <div className="flex flex-col gap-1">
        <Label htmlFor="product-category">カテゴリ</Label>
        <Select value={categoryId} onValueChange={setCategoryId} disabled={isOptionsLoading}>
          <SelectTrigger id="product-category" className="w-full">
            <SelectValue placeholder="カテゴリを選択" />
          </SelectTrigger>
          <SelectContent>
            {options.map((option) => (
              <SelectItem key={option.id} value={option.id}>
                {option.name}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>
      {showSkuField && (
        <div className="flex flex-col gap-1">
          <Label htmlFor="product-sku">SKU</Label>
          <Input
            id="product-sku"
            type="text"
            value={sku}
            onChange={(event) => {
              setSku(event.target.value);
            }}
            required
          />
        </div>
      )}
      <Button type="submit" disabled={isSubmitting}>
        {isSubmitting ? "送信中..." : submitLabel}
      </Button>
    </form>
  );
}
