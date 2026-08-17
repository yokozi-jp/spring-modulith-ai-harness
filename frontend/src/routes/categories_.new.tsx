import { createFileRoute } from "@tanstack/react-router";
import { CategoryForm } from "@/features/category/components/category-form";
import { useCreateCategory } from "@/features/category/hooks/use-create-category";
import type { CategoryFormValues } from "@/features/category/components/category-form";

export const Route = createFileRoute("/categories_/new")({
  component: NewCategoryPage,
});

function NewCategoryPage() {
  const { createCategory, isCreating } = useCreateCategory();

  function handleSubmit(values: CategoryFormValues) {
    createCategory(values);
  }

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-2xl font-bold tracking-tight">カテゴリを作成</h1>
      <CategoryForm
        showParentSelect
        onSubmit={handleSubmit}
        isSubmitting={isCreating}
        submitLabel="作成"
      />
    </div>
  );
}
