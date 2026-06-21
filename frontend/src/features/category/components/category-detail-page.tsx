import { useState } from "react";
import { Link, useNavigate } from "@tanstack/react-router";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Separator } from "@/components/ui/separator";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { useCategory } from "@/features/category/hooks/use-category";
import { useDeleteCategory } from "@/features/category/hooks/use-delete-category";

interface CategoryDetailPageProps {
  readonly id: string;
}

export function CategoryDetailPage({ id }: CategoryDetailPageProps) {
  const navigate = useNavigate();
  const { category, isLoading, error } = useCategory(id);
  const { deleteCategory, isDeleting } = useDeleteCategory();
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [deleteError, setDeleteError] = useState<string | null>(null);

  if (isLoading) {
    return (
      <div className="space-y-4">
        <div className="h-8 w-48 animate-pulse rounded-md bg-muted" />
        <div className="h-32 animate-pulse rounded-md bg-muted" />
      </div>
    );
  }

  if (error) {
    return (
      <div role="alert" className="rounded-md border border-destructive/50 p-4">
        <p className="text-sm text-destructive">{error.message}</p>
      </div>
    );
  }

  if (category === null) {
    return null;
  }

  function handleDelete() {
    if (category === null) return;
    setDeleteError(null);
    deleteCategory(
      { id: category.id, version: category.version },
      {
        onSuccess: () => {
          setIsDeleteDialogOpen(false);
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
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">{category.name}</h1>
        <div className="flex gap-2">
          <Button variant="outline" asChild>
            <Link to="/categories/$id/edit" params={{ id: category.id }}>
              編集
            </Link>
          </Button>
          <Button
            variant="destructive"
            onClick={() => {
              setIsDeleteDialogOpen(true);
            }}
          >
            削除
          </Button>
        </div>
      </div>

      <Separator />

      <dl className="grid grid-cols-[auto_1fr] gap-x-6 gap-y-3 text-sm">
        <dt className="font-medium text-muted-foreground">ID</dt>
        <dd>{category.id}</dd>

        <dt className="font-medium text-muted-foreground">表示順</dt>
        <dd>{category.sortOrder}</dd>

        <dt className="font-medium text-muted-foreground">バージョン</dt>
        <dd>
          <Badge variant="secondary">v{category.version}</Badge>
        </dd>

        {category.ancestors.length > 0 && (
          <>
            <dt className="font-medium text-muted-foreground">パンくず</dt>
            <dd className="flex items-center gap-1">
              {category.ancestors.map((ancestor, i) => (
                <span key={ancestor.id}>
                  <Link
                    to="/categories/$id"
                    params={{ id: ancestor.id }}
                    className="text-primary underline-offset-4 hover:underline"
                  >
                    {ancestor.name}
                  </Link>
                  {i < category.ancestors.length - 1 && (
                    <span className="mx-1 text-muted-foreground">/</span>
                  )}
                </span>
              ))}
            </dd>
          </>
        )}
      </dl>

      <div className="pt-4">
        <Button variant="ghost" asChild>
          <Link to="/categories">← 一覧に戻る</Link>
        </Button>
      </div>

      <Dialog open={isDeleteDialogOpen} onOpenChange={setIsDeleteDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>カテゴリを削除</DialogTitle>
          </DialogHeader>
          <p className="text-sm text-muted-foreground">
            「{category.name}」を削除してよろしいですか？この操作は取り消せません。
          </p>
          {deleteError !== null && <p className="text-sm text-destructive">{deleteError}</p>}
          <div className="flex justify-end gap-2">
            <Button
              variant="outline"
              onClick={() => {
                setIsDeleteDialogOpen(false);
              }}
            >
              キャンセル
            </Button>
            <Button variant="destructive" disabled={isDeleting} onClick={handleDelete}>
              {isDeleting ? "削除中..." : "削除する"}
            </Button>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}
