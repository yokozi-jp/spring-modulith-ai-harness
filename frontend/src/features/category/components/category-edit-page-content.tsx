import { EmptyState } from "@/components/empty-state";
import { ErrorMessage } from "@/components/error-message";
import { CategoryDetailSkeleton } from "@/features/category/components/category-detail-skeleton";
import { CategoryForm } from "@/features/category/components/category-form";
import { useCategory } from "@/features/category/hooks/use-category";
import { useUpdateCategory } from "@/features/category/hooks/use-update-category";
import { toError } from "@/lib/utils";
import type { CategoryFormValues } from "@/features/category/components/category-form";

interface CategoryEditPageContentProps {
  readonly categoryId: string;
}

/** カテゴリ編集ページの状態ハンドリング（Loading/Error/NotFound/Content）。 */
export function CategoryEditPageContent({ categoryId }: CategoryEditPageContentProps) {
  const { category, isLoading, error, refetch } = useCategory(categoryId);
  const { updateCategory, isUpdating } = useUpdateCategory(categoryId);

  if (isLoading) {
    return <CategoryDetailSkeleton />;
  }

  if (error !== null) {
    return (
      <ErrorMessage
        error={toError(error)}
        onRetry={() => {
          void refetch();
        }}
      />
    );
  }

  if (category === null) {
    return <EmptyState message="カテゴリが見つかりません" />;
  }

  function handleSubmit(values: CategoryFormValues) {
    if (category === null) {
      return;
    }
    updateCategory({
      name: values.name,
      sortOrder: values.sortOrder,
      version: category.version ?? 0,
    });
  }

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-2xl font-bold tracking-tight">カテゴリを編集</h1>
      <CategoryForm
        initialValues={{ name: category.name ?? "", sortOrder: category.sortOrder ?? 0 }}
        showParentSelect={false}
        onSubmit={handleSubmit}
        isSubmitting={isUpdating}
        submitLabel="更新"
      />
    </div>
  );
}
