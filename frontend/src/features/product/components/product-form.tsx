import { useState } from "react";
import { Button } from "@/components/ui/button";
import { useCategoryOptions } from "@/features/category/hooks/use-category-options";

interface ProductFormProps {
  readonly initialValues?: {
    readonly name: string;
    readonly description: string;
    readonly categoryId: string;
    readonly sku?: string;
  };
  readonly onSubmit: (data: {
    name: string;
    description: string;
    categoryId: string;
    sku: string;
  }) => void;
  readonly isSubmitting: boolean;
  readonly submitLabel: string;
  readonly showSku?: boolean;
}

export function ProductForm({
  initialValues,
  onSubmit,
  isSubmitting,
  submitLabel,
  showSku = true,
}: ProductFormProps) {
  const [name, setName] = useState(initialValues?.name ?? "");
  const [description, setDescription] = useState(initialValues?.description ?? "");
  const [categoryId, setCategoryId] = useState(initialValues?.categoryId ?? "");
  const [sku, setSku] = useState(initialValues?.sku ?? "");
  const { options, isLoading: isCategoriesLoading } = useCategoryOptions();

  function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    onSubmit({ name, description, categoryId, sku });
  }

  return (
    <form onSubmit={handleSubmit} className="max-w-md space-y-4">
      <div>
        <label htmlFor="name" className="text-sm font-medium">
          商品名
        </label>
        <input
          id="name"
          type="text"
          value={name}
          onChange={(e) => {
            setName(e.target.value);
          }}
          required
          maxLength={100}
          className="mt-1 w-full rounded-md border px-3 py-2"
        />
      </div>

      <div>
        <label htmlFor="description" className="text-sm font-medium">
          商品説明
        </label>
        <textarea
          id="description"
          value={description}
          onChange={(e) => {
            setDescription(e.target.value);
          }}
          required
          maxLength={1000}
          rows={4}
          className="mt-1 w-full rounded-md border px-3 py-2"
        />
      </div>

      <div>
        <label htmlFor="categoryId" className="text-sm font-medium">
          カテゴリ
        </label>
        <select
          id="categoryId"
          value={categoryId}
          onChange={(e) => {
            setCategoryId(e.target.value);
          }}
          required
          disabled={isCategoriesLoading}
          className="mt-1 w-full rounded-md border px-3 py-2"
        >
          <option value="">選択してください</option>
          {options.map((c) => (
            <option key={c.id} value={c.id}>
              {c.name}
            </option>
          ))}
        </select>
      </div>

      {showSku && (
        <div>
          <label htmlFor="sku" className="text-sm font-medium">
            SKU
          </label>
          <input
            id="sku"
            type="text"
            value={sku}
            onChange={(e) => {
              setSku(e.target.value);
            }}
            required
            className="mt-1 w-full rounded-md border px-3 py-2"
          />
        </div>
      )}

      <Button type="submit" disabled={isSubmitting}>
        {isSubmitting ? "送信中..." : submitLabel}
      </Button>
    </form>
  );
}
