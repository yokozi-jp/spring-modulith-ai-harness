import { Link, useNavigate } from "@tanstack/react-router";
import { Button } from "@/components/ui/button";
import { useCategory } from "@/features/category/hooks/use-category";
import { useUpdateCategory } from "@/features/category/hooks/use-update-category";
import { CategoryForm } from "@/features/category/components/category-form";

interface CategoryEditPageProps {
  readonly id: string;
}

export function CategoryEditPage({ id }: CategoryEditPageProps) {
  const navigate = useNavigate();
  const { category, isLoading, error: fetchError } = useCategory(id);
  const { updateCategory, isUpdating, error: updateError } = useUpdateCategory(id);

  if (isLoading) {
    return (
      <div className="space-y-4">
        <div className="h-8 w-48 animate-pulse rounded-md bg-muted" />
        <div className="h-48 animate-pulse rounded-md bg-muted" />
      </div>
    );
  }

  if (fetchError) {
    return (
      <div role="alert" className="rounded-md border border-destructive/50 p-4">
        <p className="text-sm text-destructive">{fetchError.message}</p>
      </div>
    );
  }

  if (category === null) {
    return null;
  }

  function handleSubmit(data: { name: string; sortOrder: number }) {
    if (category === null) return;
    updateCategory(
      { name: data.name, sortOrder: data.sortOrder, version: category.version },
      {
        onSuccess: () => {
          void navigate({ to: "/categories/$id", params: { id } });
        },
      },
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">カテゴリ編集</h1>
        <Button variant="ghost" asChild>
          <Link to="/categories/$id" params={{ id }}>
            ← 詳細に戻る
          </Link>
        </Button>
      </div>

      <div className="max-w-md">
        <CategoryForm
          initialName={category.name}
          initialSortOrder={category.sortOrder}
          onSubmit={handleSubmit}
          isSubmitting={isUpdating}
          submitLabel="更新"
          error={updateError}
        />
      </div>
    </div>
  );
}
