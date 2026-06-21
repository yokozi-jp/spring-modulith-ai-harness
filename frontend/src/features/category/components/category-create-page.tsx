import { Link, useNavigate } from "@tanstack/react-router";
import { Button } from "@/components/ui/button";
import { useCreateCategory } from "@/features/category/hooks/use-create-category";
import { CategoryForm } from "@/features/category/components/category-form";

export function CategoryCreatePage() {
  const navigate = useNavigate();
  const { createCategory, isCreating, error } = useCreateCategory();

  function handleSubmit(data: { name: string; sortOrder: number }) {
    createCategory(
      { name: data.name, sortOrder: data.sortOrder },
      {
        onSuccess: () => {
          void navigate({ to: "/categories" });
        },
      },
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">カテゴリ作成</h1>
        <Button variant="ghost" asChild>
          <Link to="/categories">← 一覧に戻る</Link>
        </Button>
      </div>

      <div className="max-w-md">
        <CategoryForm
          onSubmit={handleSubmit}
          isSubmitting={isCreating}
          submitLabel="作成"
          error={error}
        />
      </div>
    </div>
  );
}
