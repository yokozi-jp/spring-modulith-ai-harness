import { useState } from "react";
import { Link } from "@tanstack/react-router";
import { Button } from "@/components/ui/button";
import { ErrorMessage } from "@/components/error-message";
import { EmptyState } from "@/components/empty-state";
import { useCategoryList } from "@/features/category/hooks/use-category-list";

export function CategoryList() {
  const [page, setPage] = useState(0);
  const { categories, isLoading, error } = useCategoryList(page, 20);

  if (isLoading) {
    return <CategoryListSkeleton />;
  }

  if (error) {
    return <ErrorMessage error={error} />;
  }

  if (categories === null || categories.content.length === 0) {
    return <EmptyState message="カテゴリがありません" />;
  }

  return (
    <div className="space-y-4">
      <table className="w-full border-collapse">
        <thead>
          <tr className="border-b text-left text-sm text-muted-foreground">
            <th className="px-4 py-2">名前</th>
            <th className="px-4 py-2">表示順</th>
            <th className="px-4 py-2">操作</th>
          </tr>
        </thead>
        <tbody>
          {categories.content.map((category) => (
            <tr key={category.id} className="border-b hover:bg-muted/50">
              <td className="px-4 py-2">{category.name}</td>
              <td className="px-4 py-2">{category.sortOrder}</td>
              <td className="px-4 py-2">
                <Button variant="ghost" size="sm" asChild>
                  <Link to="/categories/$id" params={{ id: category.id }}>
                    詳細
                  </Link>
                </Button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {categories.totalPages > 1 && (
        <div className="flex items-center justify-center gap-2">
          <Button
            variant="outline"
            size="sm"
            disabled={page === 0}
            onClick={() => {
              setPage((p) => p - 1);
            }}
          >
            前へ
          </Button>
          <span className="text-sm text-muted-foreground">
            {page + 1} / {categories.totalPages}
          </span>
          <Button
            variant="outline"
            size="sm"
            disabled={page >= categories.totalPages - 1}
            onClick={() => {
              setPage((p) => p + 1);
            }}
          >
            次へ
          </Button>
        </div>
      )}
    </div>
  );
}

function CategoryListSkeleton() {
  return (
    <div className="space-y-4">
      {Array.from({ length: 5 }).map((_, i) => (
        <div key={`skeleton-${String(i)}`} className="h-12 animate-pulse rounded-md bg-muted" />
      ))}
    </div>
  );
}
