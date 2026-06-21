import { useState } from "react";
import { Link, useNavigate } from "@tanstack/react-router";
import { Button } from "@/components/ui/button";
import { ErrorMessage } from "@/components/error-message";
import { useCategory } from "@/features/category/hooks/use-category";
import { useDeleteCategory } from "@/features/category/hooks/use-delete-category";

interface CategoryDetailProps {
  readonly id: string;
}

export function CategoryDetail({ id }: CategoryDetailProps) {
  const { category, isLoading, error, refetch } = useCategory(id);
  const { deleteCategory, isDeleting } = useDeleteCategory();
  const navigate = useNavigate();
  const [deleteError, setDeleteError] = useState<string | null>(null);

  if (isLoading) {
    return <div className="h-32 animate-pulse rounded-md bg-muted" />;
  }

  if (error) {
    return <ErrorMessage error={error} onRetry={refetch} />;
  }

  if (category === null) {
    return <ErrorMessage error={new Error("カテゴリが見つかりません")} />;
  }

  function handleDelete() {
    if (category === null) {
      return;
    }
    setDeleteError(null);
    deleteCategory(
      { id: category.id, version: category.version },
      {
        onSuccess: () => {
          void navigate({ to: "/categories" });
        },
        onError: (err) => {
          setDeleteError(err.message);
        },
      },
    );
  }

  return (
    <div className="space-y-6">
      <dl className="space-y-3">
        <div>
          <dt className="text-sm text-muted-foreground">カテゴリ名</dt>
          <dd className="text-lg font-medium">{category.name}</dd>
        </div>
        <div>
          <dt className="text-sm text-muted-foreground">表示順</dt>
          <dd>{category.sortOrder}</dd>
        </div>
        <div>
          <dt className="text-sm text-muted-foreground">親カテゴリ</dt>
          <dd>{category.parentCategoryId ?? "なし（ルート）"}</dd>
        </div>
        {category.ancestors.length > 0 && (
          <div>
            <dt className="text-sm text-muted-foreground">パンくず</dt>
            <dd>{category.ancestors.map((a) => a.name).join(" > ")}</dd>
          </div>
        )}
        <div>
          <dt className="text-sm text-muted-foreground">バージョン</dt>
          <dd>{category.version}</dd>
        </div>
      </dl>

      {deleteError !== null && (
        <div className="rounded-md border border-destructive/50 p-3">
          <p className="text-sm text-destructive">{deleteError}</p>
        </div>
      )}

      <div className="flex gap-2">
        <Button asChild>
          <Link to="/categories/$id/edit" params={{ id: category.id }}>
            編集
          </Link>
        </Button>
        <Button variant="destructive" onClick={handleDelete} disabled={isDeleting}>
          {isDeleting ? "削除中..." : "削除"}
        </Button>
      </div>
    </div>
  );
}
