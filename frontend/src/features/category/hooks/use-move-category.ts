import { useQueryClient } from "@tanstack/react-query";
import { useMove, getList2QueryKey, getFindById2QueryKey } from "@/api/category/category";

export function useMoveCategory(id: string) {
  const queryClient = useQueryClient();

  const mutation = useMove({
    mutation: {
      onSuccess: () => {
        void queryClient.invalidateQueries({
          queryKey: getList2QueryKey(),
        });
        void queryClient.invalidateQueries({
          queryKey: getFindById2QueryKey(id),
        });
      },
    },
  });

  function moveCategory(newParentCategoryId: string | null, version: number) {
    mutation.mutate({
      id,
      data: {
        version,
        ...(newParentCategoryId !== null && { newParentCategoryId }),
      },
    });
  }

  return {
    moveCategory,
    isMoving: mutation.isPending,
    isSuccess: mutation.isSuccess,
    error: mutation.error,
    reset: mutation.reset,
  };
}
