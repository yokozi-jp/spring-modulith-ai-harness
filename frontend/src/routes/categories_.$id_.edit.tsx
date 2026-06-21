import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { CategoryForm } from "@/features/category/components/category-form";
import { useCategory } from "@/features/category/hooks/use-category";
import { useUpdateCategory } from "@/features/category/hooks/use-update-category";
import { ErrorMessage } from "@/components/error-message";

export const Route = createFileRoute("/categories_/$id_/edit")({
  component: CategoryEditPage,
});

function CategoryEditPage() {
  const { id } = Route.useParams();
  const { category, isLoading, error: fetchError } = useCategory(id);
  const { updateCategory, isUpdating, error: updateError } = useUpdateCategory();
  const navigate = useNavigate();

  if (isLoading) {
    return <div className="h-32 animate-pulse rounded-md bg-muted" />;
  }

  if (fetchError) {
    return <ErrorMessage error={fetchError} />;
  }

  if (category === null) {
    return <ErrorMessage error={new Error("カテゴリが見つかりません")} />;
  }

  function handleSubmit(data: {
    name: string;
    sortOrder: number;
    parentCategoryId: string | null;
  }) {
    if (category === null) {
      return;
    }
    updateCategory(
      { id, input: { name: data.name, sortOrder: data.sortOrder, version: category.version } },
      {
        onSuccess: () => {
          void navigate({ to: "/categories/$id", params: { id } });
        },
      },
    );
  }

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold">カテゴリ編集</h1>
      <ErrorMessage error={updateError} />
      <CategoryForm
        initialValues={{
          name: category.name,
          sortOrder: category.sortOrder,
          parentCategoryId: category.parentCategoryId,
        }}
        excludeId={id}
        onSubmit={handleSubmit}
        isSubmitting={isUpdating}
        submitLabel="更新"
      />
    </div>
  );
}
