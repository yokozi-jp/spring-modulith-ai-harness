import { useMutation, useQueryClient } from "@tanstack/react-query";
import { updateCategory } from "@/features/category/types/category-api";
import type { UpdateCategoryInput } from "@/features/category/types/category";

export function useUpdateCategory(id: string) {
  const queryClient = useQueryClient();

  const mutation = useMutation({
    mutationFn: (data: UpdateCategoryInput) => updateCategory(id, data),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["categories"] });
      void queryClient.invalidateQueries({ queryKey: ["categories", id] });
    },
  });

  return {
    updateCategory: mutation.mutate,
    isUpdating: mutation.isPending,
    error: mutation.error,
  };
}
