import { useQueryClient } from "@tanstack/react-query";
import { useMoveCategory as useMoveCategoryMutation } from "@/api/category/category";
import type { MoveCategoryRequest } from "@/api/openAPIDefinition.schemas";

/** カテゴリ移動に必要な入力値。 */
export interface MoveCategoryInput {
  readonly newParentCategoryId?: string;
  readonly version: number;
}

/** カテゴリを移動する。成功時は一覧・詳細を再取得する。 */
export function useMoveCategory(id: string) {
  const queryClient = useQueryClient();

  const mutation = useMoveCategoryMutation({
    mutation: {
      onSuccess: () => {
        void queryClient.invalidateQueries({ queryKey: ["categories"] });
      },
    },
  });

  function moveCategory(input: MoveCategoryInput) {
    const requestData: MoveCategoryRequest = {
      version: input.version,
      ...(input.newParentCategoryId !== undefined && {
        newParentCategoryId: input.newParentCategoryId,
      }),
    };
    mutation.mutate({ id, data: requestData });
  }

  return {
    moveCategory,
    isMoving: mutation.isPending,
    error: mutation.error,
  };
}
