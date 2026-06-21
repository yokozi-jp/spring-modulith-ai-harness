import { useMutation, useQueryClient } from "@tanstack/react-query";
import { apiClient } from "@/lib/api-client";
import type { CreateCategoryInput } from "@/features/category/types/category";

export function useCreateCategory() {
  const queryClient = useQueryClient();

  const mutation = useMutation({
    mutationFn: (input: CreateCategoryInput) =>
      apiClient<undefined>({
        url: "/categories",
        method: "POST",
        data: input,
      }),
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
