import { useState } from "react";
import { Link } from "@tanstack/react-router";
import { Button } from "@/components/ui/button";
import { ConfirmDialog } from "@/components/confirm-dialog";
import { ErrorMessage } from "@/components/error-message";
import { toError } from "@/lib/utils";
import { useCategory } from "@/features/category/hooks/use-category";
import { useCategoryChildren } from "@/features/category/hooks/use-category-children";
import { useDeleteCategory } from "@/features/category/hooks/use-delete-category";
import { MoveCategoryDialog } from "@/features/category/components/move-category-dialog";
import { CategoryProductList } from "@/features/category/components/category-product-list";

interface CategoryDetailProps {
  readonly id: string;
}

export function CategoryDetail({ id }: CategoryDetailProps) {
  const { category, isLoading, error, refetch } = useCategory(id);
  const { deleteCategory, isDeleting, error: deleteError } = useDeleteCategory();
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [isMoveDialogOpen, setIsMoveDialogOpen] = useState(false);

  if (isLoading) {
    return (
      <div className="space-y-4">
        <div className="h-8 w-48 animate-pulse rounded bg-muted" />
        <div className="h-6 w-32 animate-pulse rounded bg-muted" />
      </div>
    );
  }

  if (error) {
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
    return <ErrorMessage error={new Error("カテゴリが見つかりません")} />;
  }

  function handleDeleteClick() {
    setIsDialogOpen(true);
  }

  function handleConfirmDelete() {
    if (category === null) {
      return;
    }
    deleteCategory(category.id, category.version);
    setIsDialogOpen(false);
  }

  return (
    <div className="space-y-6">
      {deleteError !== null && deleteError !== undefined && (
        <ErrorMessage error={toError(deleteError)} />
      )}

      <div className="space-y-4">
        {category.ancestors.length > 0 && (
          <nav className="flex items-center gap-1 text-sm text-muted-foreground">
            {category.ancestors.map((ancestor, index) => (
              <span key={ancestor.id} className="flex items-center gap-1">
                {index > 0 && <span>/</span>}
                <Link
                  to="/categories/$id"
                  params={{ id: ancestor.id ?? "" }}
                  className="hover:underline"
                >
                  {ancestor.name}
                </Link>
              </span>
            ))}
            <span>/</span>
            <span className="font-medium text-foreground">{category.name}</span>
          </nav>
        )}

        <dl className="grid grid-cols-[auto_1fr] gap-x-6 gap-y-3">
          <dt className="text-sm font-medium text-muted-foreground">カテゴリ名</dt>
          <dd>{category.name}</dd>

          <dt className="text-sm font-medium text-muted-foreground">並び順</dt>
          <dd>{category.sortOrder}</dd>

          <dt className="text-sm font-medium text-muted-foreground">バージョン</dt>
          <dd>{category.version}</dd>
        </dl>
      </div>

      <div className="flex gap-2">
        <Button asChild>
          <Link to="/categories/$id/edit" params={{ id }}>
            編集
          </Link>
        </Button>
        <Button
          variant="outline"
          onClick={() => {
            setIsMoveDialogOpen(true);
          }}
        >
          移動
        </Button>
        <Button variant="destructive" onClick={handleDeleteClick}>
          削除
        </Button>
      </div>

      <ChildrenSection id={id} />

      <CategoryProductList categoryId={id} />

      <MoveCategoryDialog
        isOpen={isMoveDialogOpen}
        onClose={() => {
          setIsMoveDialogOpen(false);
        }}
        categoryId={category.id}
        categoryName={category.name}
        currentParentId={category.parentCategoryId}
        version={category.version}
      />

      <ConfirmDialog
        isOpen={isDialogOpen}
        onClose={() => {
          setIsDialogOpen(false);
        }}
        onConfirm={handleConfirmDelete}
        title="カテゴリを削除"
        message={`「${category.name}」を削除しますか？この操作は取り消せません。`}
        confirmLabel="削除する"
        isLoading={isDeleting}
      />
    </div>
  );
}

interface ChildrenSectionProps {
  readonly id: string;
}

function ChildrenSection({ id }: ChildrenSectionProps) {
  const { children, isLoading } = useCategoryChildren(id);

  return (
    <div className="space-y-3">
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-semibold">子カテゴリ</h2>
        <Button asChild size="sm">
          <Link to="/categories/new" search={{ parentCategoryId: id }}>
            子カテゴリを作成
          </Link>
        </Button>
      </div>
      {isLoading && (
        <div className="space-y-2">
          <div className="h-8 w-48 animate-pulse rounded bg-muted" />
          <div className="h-8 w-36 animate-pulse rounded bg-muted" />
        </div>
      )}
      {!isLoading && children.length === 0 && (
        <p className="text-sm text-muted-foreground">子カテゴリはありません</p>
      )}
      {!isLoading && children.length > 0 && (
        <ul className="space-y-1">
          {children.map((child) => (
            <li key={child.id}>
              <Link
                to="/categories/$id"
                params={{ id: child.id ?? "" }}
                className="inline-flex items-center gap-2 rounded-md px-3 py-1.5 text-sm hover:bg-muted"
              >
                <span>{child.name}</span>
                <span className="text-xs text-muted-foreground">(並び順: {child.sortOrder})</span>
              </Link>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
