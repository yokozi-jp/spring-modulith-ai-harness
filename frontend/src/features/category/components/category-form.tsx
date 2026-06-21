import { useState } from "react";
import { Button } from "@/components/ui/button";
import { useCategoryOptions } from "@/features/category/hooks/use-category-options";

interface CategoryFormProps {
  readonly initialValues?: {
    readonly name: string;
    readonly sortOrder: number;
    readonly parentCategoryId: string | null;
  };
  readonly excludeId?: string;
  readonly onSubmit: (data: {
    name: string;
    sortOrder: number;
    parentCategoryId: string | null;
  }) => void;
  readonly isSubmitting: boolean;
  readonly submitLabel: string;
}

export function CategoryForm({
  initialValues,
  excludeId,
  onSubmit,
  isSubmitting,
  submitLabel,
}: CategoryFormProps) {
  const [name, setName] = useState(initialValues?.name ?? "");
  const [sortOrder, setSortOrder] = useState(String(initialValues?.sortOrder ?? 0));
  const [parentCategoryId, setParentCategoryId] = useState(initialValues?.parentCategoryId ?? "");
  const { options, isLoading: isCategoriesLoading } = useCategoryOptions();

  function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    onSubmit({
      name,
      sortOrder: Number(sortOrder),
      parentCategoryId: parentCategoryId.length > 0 ? parentCategoryId : null,
    });
  }

  return (
    <form onSubmit={handleSubmit} className="max-w-md space-y-4">
      <div>
        <label htmlFor="name" className="text-sm font-medium">
          カテゴリ名
        </label>
        <input
          id="name"
          type="text"
          value={name}
          onChange={(e) => {
            setName(e.target.value);
          }}
          required
          maxLength={50}
          className="mt-1 w-full rounded-md border px-3 py-2"
        />
      </div>

      <div>
        <label htmlFor="sortOrder" className="text-sm font-medium">
          表示順
        </label>
        <input
          id="sortOrder"
          type="number"
          value={sortOrder}
          onChange={(e) => {
            setSortOrder(e.target.value);
          }}
          required
          className="mt-1 w-full rounded-md border px-3 py-2"
        />
      </div>

      <div>
        <label htmlFor="parentCategoryId" className="text-sm font-medium">
          親カテゴリ（任意）
        </label>
        <select
          id="parentCategoryId"
          value={parentCategoryId}
          onChange={(e) => {
            setParentCategoryId(e.target.value);
          }}
          disabled={isCategoriesLoading}
          className="mt-1 w-full rounded-md border px-3 py-2"
        >
          <option value="">なし（ルートカテゴリ）</option>
          {options
            .filter((c) => c.id !== excludeId)
            .map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
        </select>
      </div>

      <Button type="submit" disabled={isSubmitting}>
        {isSubmitting ? "送信中..." : submitLabel}
      </Button>
    </form>
  );
}
