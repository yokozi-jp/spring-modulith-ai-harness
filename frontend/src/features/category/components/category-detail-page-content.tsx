import { EmptyState } from "@/components/empty-state";
import { ErrorMessage } from "@/components/error-message";
import { CategoryDetailCard } from "@/features/category/components/category-detail-card";
import { CategoryDetailHeader } from "@/features/category/components/category-detail-header";
import { CategoryDetailSkeleton } from "@/features/category/components/category-detail-skeleton";
import { CategoryProductList } from "@/features/category/components/category-product-list";
import { useCategory } from "@/features/category/hooks/use-category";
import { toError } from "@/lib/utils";

interface CategoryDetailPageContentProps {
  readonly categoryId: string;
}

/** カテゴリ詳細ページの状態ハンドリング（Loading/Error/NotFound/Content）。 */
export function CategoryDetailPageContent({ categoryId }: CategoryDetailPageContentProps) {
  const { category, isLoading, error, refetch } = useCategory(categoryId);

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

  return (
    <div className="flex flex-col gap-6">
      <CategoryDetailHeader
        categoryId={category.id ?? ""}
        categoryName={category.name ?? ""}
        version={category.version ?? 0}
      />
      <CategoryDetailCard category={category} />
      <CategoryProductList categoryId={category.id ?? ""} />
    </div>
  );
}
