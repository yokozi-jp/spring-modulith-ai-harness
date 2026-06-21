import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { CategoryForm } from "@/features/category/components/category-form";
import { useCreateCategory } from "@/features/category/hooks/use-create-category";
import { ErrorMessage } from "@/components/error-message";

export const Route = createFileRoute("/categories_/new")({
  component: CategoryNewPage,
});

function CategoryNewPage() {
  const { createCategory, isCreating, error } = useCreateCategory();
  const navigate = useNavigate();

  function handleSubmit(data: {
    name: string;
    sortOrder: number;
    parentCategoryId: string | null;
  }) {
    createCategory(data, {
      onSuccess: () => {
        void navigate({ to: "/categories" });
      },
    });
  }

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold">カテゴリ作成</h1>
      <ErrorMessage error={error} />
      <CategoryForm onSubmit={handleSubmit} isSubmitting={isCreating} submitLabel="作成" />
    </div>
  );
}
