import { useState } from "react";
import { Link } from "@tanstack/react-router";
import { Button } from "@/components/ui/button";
import { ErrorMessage } from "@/components/error-message";
import { toError } from "@/lib/utils";
import { useCategory } from "@/features/category/hooks/use-category";
import { useUpdateCategory } from "@/features/category/hooks/use-update-category";

interface CategoryEditFormProps {
  readonly id: string;
}

export function CategoryEditForm({ id }: CategoryEditFormProps) {
  const { category, isLoading, error: fetchError } = useCategory(id);
  const { updateCategory, isUpdating, error: updateError } = useUpdateCategory(id);
  const [name, setName] = useState<string | null>(null);
  const [sortOrder, setSortOrder] = useState<number | null>(null);

  if (isLoading) {
    return (
      <div className="max-w-md space-y-4">
        <div className="h-10 animate-pulse rounded bg-muted" />
        <div className="h-10 animate-pulse rounded bg-muted" />
      </div>
    );
  }

  if (fetchError) {
    return <ErrorMessage error={toError(fetchError)} />;
  }

  if (category === null) {
    return <ErrorMessage error={new Error("カテゴリが見つかりません")} />;
  }

  const currentName = name ?? category.name;
  const currentSortOrder = sortOrder ?? category.sortOrder;

  function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (category === null) {
      return;
    }
    updateCategory({
      name: currentName,
      sortOrder: currentSortOrder,
      version: category.version,
    });
  }

  return (
    <form onSubmit={handleSubmit} className="max-w-md space-y-4">
      {updateError !== null && updateError !== undefined && (
        <ErrorMessage error={toError(updateError)} />
      )}

      <div className="space-y-2">
        <label htmlFor="name" className="text-sm font-medium">
          カテゴリ名
        </label>
        <input
          id="name"
          type="text"
          value={currentName}
          onChange={(e) => {
            setName(e.target.value);
          }}
          required
          maxLength={50}
          className="w-full rounded-md border px-3 py-2"
        />
      </div>

      <div className="space-y-2">
        <label htmlFor="sortOrder" className="text-sm font-medium">
          並び順
        </label>
        <input
          id="sortOrder"
          type="number"
          value={currentSortOrder}
          onChange={(e) => {
            setSortOrder(Number(e.target.value));
          }}
          required
          min={0}
          className="w-full rounded-md border px-3 py-2"
        />
      </div>

      <div className="flex gap-2">
        <Button type="submit" disabled={isUpdating}>
          {isUpdating ? "更新中..." : "更新"}
        </Button>
        <Button asChild variant="outline">
          <Link to="/categories/$id" params={{ id }}>
            キャンセル
          </Link>
        </Button>
      </div>
    </form>
  );
}
