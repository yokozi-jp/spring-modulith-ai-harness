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
import { useCategoryOptions } from "@/features/category/hooks/use-category-options";

/** 親カテゴリ未選択（ルート直下）を表す Select 用の値。Radix Select は空文字を許容しないため専用値を使う。 */
const NO_PARENT_VALUE = "__root__";

/** カテゴリフォームの送信値。 */
export interface CategoryFormValues {
  readonly name: string;
  readonly sortOrder: number;
  readonly parentCategoryId?: string;
}

interface CategoryFormProps {
  readonly initialValues?: CategoryFormValues;
  readonly showParentSelect: boolean;
  readonly onSubmit: (values: CategoryFormValues) => void;
  readonly isSubmitting: boolean;
  readonly submitLabel: string;
}

/** カテゴリ作成・編集共通フォーム。作成時のみ親カテゴリ選択を表示する。 */
export function CategoryForm({
  initialValues,
  showParentSelect,
  onSubmit,
  isSubmitting,
  submitLabel,
}: CategoryFormProps) {
  const [name, setName] = useState(initialValues?.name ?? "");
  const [sortOrder, setSortOrder] = useState(initialValues?.sortOrder ?? 0);
  const [parentCategoryId, setParentCategoryId] = useState(
    initialValues?.parentCategoryId ?? NO_PARENT_VALUE,
  );
  const { options, isLoading: isOptionsLoading } = useCategoryOptions();

  function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault();
    onSubmit({
      name,
      sortOrder,
      ...(parentCategoryId !== NO_PARENT_VALUE && { parentCategoryId }),
    });
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-4">
      <div className="flex flex-col gap-1">
        <Label htmlFor="category-name">カテゴリ名</Label>
        <Input
          id="category-name"
          type="text"
          value={name}
          onChange={(event) => {
            setName(event.target.value);
          }}
          maxLength={50}
          required
        />
      </div>
      <div className="flex flex-col gap-1">
        <Label htmlFor="category-sort-order">並び順</Label>
        <Input
          id="category-sort-order"
          type="number"
          value={sortOrder}
          onChange={(event) => {
            setSortOrder(Number(event.target.value));
          }}
          required
        />
      </div>
      {showParentSelect && (
        <div className="flex flex-col gap-1">
          <Label htmlFor="category-parent">親カテゴリ</Label>
          <Select
            value={parentCategoryId}
            onValueChange={setParentCategoryId}
            disabled={isOptionsLoading}
          >
            <SelectTrigger id="category-parent" className="w-full">
              <SelectValue placeholder="親カテゴリを選択" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value={NO_PARENT_VALUE}>なし（ルート直下）</SelectItem>
              {options.map((option) => (
                <SelectItem key={option.id} value={option.id}>
                  {option.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      )}
      <Button type="submit" disabled={isSubmitting}>
        {isSubmitting ? "送信中..." : submitLabel}
      </Button>
    </form>
  );
}
