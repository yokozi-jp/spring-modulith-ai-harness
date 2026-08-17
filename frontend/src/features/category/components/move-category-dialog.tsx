import { useState } from "react";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { useCategoryOptions } from "@/features/category/hooks/use-category-options";

/** 移動先未選択（ルート直下へ移動）を表す Select 用の値。 */
const NO_PARENT_VALUE = "__root__";

interface MoveCategoryDialogProps {
  readonly isOpen: boolean;
  readonly onClose: () => void;
  readonly onMove: (newParentCategoryId?: string) => void;
  readonly categoryId: string;
  readonly isMoving: boolean;
}

/** カテゴリの移動先を選択するダイアログ。移動対象自身は選択肢から除外する。 */
export function MoveCategoryDialog({
  isOpen,
  onClose,
  onMove,
  categoryId,
  isMoving,
}: MoveCategoryDialogProps) {
  const [newParentCategoryId, setNewParentCategoryId] = useState(NO_PARENT_VALUE);
  const { options } = useCategoryOptions();
  const selectableOptions = options.filter((option) => option.id !== categoryId);

  function handleMove() {
    onMove(newParentCategoryId === NO_PARENT_VALUE ? undefined : newParentCategoryId);
  }

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>カテゴリを移動</DialogTitle>
        </DialogHeader>
        <Select value={newParentCategoryId} onValueChange={setNewParentCategoryId}>
          <SelectTrigger className="w-full">
            <SelectValue placeholder="移動先を選択" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value={NO_PARENT_VALUE}>なし（ルート直下）</SelectItem>
            {selectableOptions.map((option) => (
              <SelectItem key={option.id} value={option.id}>
                {option.name}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
        <DialogFooter>
          <Button type="button" variant="outline" onClick={onClose} disabled={isMoving}>
            キャンセル
          </Button>
          <Button type="button" onClick={handleMove} disabled={isMoving}>
            {isMoving ? "移動中..." : "移動する"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
