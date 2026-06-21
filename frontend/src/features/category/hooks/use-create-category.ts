import { useMutation, useQueryClient } from "@tanstack/react-query";
import { createCategory } from "@/features/category/types/category-api";

export function useCreateCategory() {
  const queryClient = useQueryClient();

  const mutation = useMutation({
    mutationFn: createCategory,
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["categories"] });
    },
  });

  return {
    createCategory: mutation.mutate,
    isCreating: mutation.isPending,
    error: mutation.error,
  };
}
