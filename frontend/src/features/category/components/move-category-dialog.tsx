import { useEffect, useState } from "react";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { ErrorMessage } from "@/components/error-message";
import { toError } from "@/lib/utils";
import { useCategoryOptions } from "@/features/category/hooks/use-category-options";
import { useMoveCategory } from "@/features/category/hooks/use-move-category";

interface MoveCategoryDialogProps {
  readonly isOpen: boolean;
  readonly onClose: () => void;
  readonly categoryId: string;
  readonly categoryName: string;
  readonly currentParentId: string | null;
  readonly version: number;
}

export function MoveCategoryDialog({
  isOpen,
  onClose,
  categoryId,
  categoryName,
  currentParentId,
  version,
}: MoveCategoryDialogProps) {
  const { options, isLoading: isLoadingOptions } = useCategoryOptions();
  const { moveCategory, isMoving, isSuccess, error, reset } = useMoveCategory(categoryId);
  const [selectedParentId, setSelectedParentId] = useState<string>(currentParentId ?? "");

  useEffect(() => {
    if (isOpen) {
      setSelectedParentId(currentParentId ?? "");
      reset();
    }
  }, [isOpen, currentParentId, reset]);

  useEffect(() => {
    if (isSuccess) {
      onClose();
    }
  }, [isSuccess, onClose]);

  // 自分自身は選択肢から除外する
  const filteredOptions = options.filter((opt) => opt.id !== categoryId);

  function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const newParentId = selectedParentId === "" ? null : selectedParentId;
    moveCategory(newParentId, version);
  }

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>親カテゴリを変更</DialogTitle>
          <DialogDescription>「{categoryName}」の親カテゴリを選択してください。</DialogDescription>
        </DialogHeader>

        {error !== null && error !== undefined && <ErrorMessage error={toError(error)} />}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <label htmlFor="parentCategory" className="text-sm font-medium">
              移動先の親カテゴリ
            </label>
            <select
              id="parentCategory"
              value={selectedParentId}
              onChange={(e) => {
                setSelectedParentId(e.target.value);
              }}
              disabled={isLoadingOptions}
              className="w-full rounded-md border px-3 py-2"
            >
              <option value="">ルート（親なし）</option>
              {filteredOptions.map((opt) => (
                <option key={opt.id} value={opt.id}>
                  {"　".repeat(opt.depth)}
                  {opt.name}
                </option>
              ))}
            </select>
          </div>

          <DialogFooter>
            <Button type="button" variant="outline" onClick={onClose} disabled={isMoving}>
              キャンセル
            </Button>
            <Button type="submit" disabled={isMoving || isLoadingOptions}>
              {isMoving ? "移動中..." : "移動する"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
