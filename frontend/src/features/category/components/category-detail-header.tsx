import { useState } from "react";
import { Link } from "@tanstack/react-router";
import { Button } from "@/components/ui/button";
import { ConfirmDialog } from "@/components/confirm-dialog";
import { MoveCategoryDialog } from "@/features/category/components/move-category-dialog";
import { useDeleteCategory } from "@/features/category/hooks/use-delete-category";
import { useMoveCategory } from "@/features/category/hooks/use-move-category";

interface CategoryDetailHeaderProps {
  readonly categoryId: string;
  readonly categoryName: string;
  readonly version: number;
}

/** カテゴリ詳細ページの見出し・編集/削除/移動ボタン・確認ダイアログ群。 */
export function CategoryDetailHeader({
  categoryId,
  categoryName,
  version,
}: CategoryDetailHeaderProps) {
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [isMoveDialogOpen, setIsMoveDialogOpen] = useState(false);
  const { deleteCategory, isDeleting } = useDeleteCategory();
  const { moveCategory, isMoving } = useMoveCategory(categoryId);

  function handleDelete() {
    deleteCategory(categoryId, version);
  }

  function handleMove(newParentCategoryId?: string) {
    moveCategory({ version, ...(newParentCategoryId !== undefined && { newParentCategoryId }) });
    setIsMoveDialogOpen(false);
  }

  return (
    <div className="flex items-center justify-between">
      <h1 className="text-2xl font-bold tracking-tight">{categoryName}</h1>
      <div className="flex gap-2">
        <Button
          type="button"
          variant="outline"
          onClick={() => {
            setIsMoveDialogOpen(true);
          }}
        >
          移動
        </Button>
        <Button asChild variant="outline">
          <Link to="/categories/$id/edit" params={{ id: categoryId }}>
            編集
          </Link>
        </Button>
        <Button
          type="button"
          variant="destructive"
          onClick={() => {
            setIsDeleteDialogOpen(true);
          }}
        >
          削除
        </Button>
      </div>
      <ConfirmDialog
        isOpen={isDeleteDialogOpen}
        onClose={() => {
          setIsDeleteDialogOpen(false);
        }}
        onConfirm={handleDelete}
        title="カテゴリを削除"
        message={`「${categoryName}」を削除します。この操作は取り消せません。`}
        confirmLabel="削除する"
        isLoading={isDeleting}
      />
      <MoveCategoryDialog
        isOpen={isMoveDialogOpen}
        onClose={() => {
          setIsMoveDialogOpen(false);
        }}
        onMove={handleMove}
        categoryId={categoryId}
        isMoving={isMoving}
      />
    </div>
  );
}
