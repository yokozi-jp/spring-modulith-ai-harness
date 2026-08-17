import { EmptyState } from "@/components/empty-state";
import { ErrorMessage } from "@/components/error-message";
import { CategoryTreeNodeItem } from "@/features/category/components/category-tree-node";
import { CategoryTreeSkeleton } from "@/features/category/components/category-tree-skeleton";
import { useCategoryList } from "@/features/category/hooks/use-category-list";
import { toError } from "@/lib/utils";

/** ルートカテゴリから展開可能なカテゴリツリーを表示する。 */
export function CategoryTree() {
  const { categories, isLoading, error, refetch } = useCategoryList();

  if (isLoading) {
    return <CategoryTreeSkeleton />;
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

  if (categories.length === 0) {
    return <EmptyState message="カテゴリがありません" />;
  }

  return (
    <div className="flex flex-col gap-1">
      {categories.map((category) => (
        <CategoryTreeNodeItem
          key={category.id ?? ""}
          id={category.id ?? ""}
          name={category.name ?? ""}
          depth={0}
        />
      ))}
    </div>
  );
}
