import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { ErrorMessage } from "@/components/error-message";
import { EmptyState } from "@/components/empty-state";
import { toError } from "@/lib/utils";
import { useCategoryList } from "@/features/category/hooks/use-category-list";
import { CategoryListSkeleton } from "@/features/category/components/category-list-skeleton";
import { CategoryTreeNode } from "@/features/category/components/category-row";

export function CategoryList() {
  const { categories, isLoading, error } = useCategoryList();

  if (isLoading) {
    return <CategoryListSkeleton />;
  }

  if (error) {
    return <ErrorMessage error={toError(error)} />;
  }

  if (categories.length === 0) {
    return <EmptyState message="カテゴリがありません" />;
  }

  return (
    <Card>
      <CardHeader className="pb-3">
        <CardTitle className="text-base">カテゴリツリー</CardTitle>
      </CardHeader>
      <CardContent className="pb-4">
        <ul className="space-y-0.5">
          {categories.map((category) => (
            <CategoryTreeNode
              key={category.id}
              id={category.id ?? ""}
              name={category.name ?? ""}
              sortOrder={category.sortOrder ?? 0}
              depth={0}
            />
          ))}
        </ul>
      </CardContent>
    </Card>
  );
}
