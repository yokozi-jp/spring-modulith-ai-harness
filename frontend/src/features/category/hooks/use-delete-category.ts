import { useMutation, useQueryClient } from "@tanstack/react-query";
import { deleteCategory } from "@/features/category/types/category-api";

export function useDeleteCategory() {
  const queryClient = useQueryClient();

  const mutation = useMutation({
    mutationFn: ({ id, version }: { id: string; version: number }) => deleteCategory(id, version),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["categories"] });
    },
  });

  return {
    deleteCategory: mutation.mutate,
    isDeleting: mutation.isPending,
    error: mutation.error,
  };
}
