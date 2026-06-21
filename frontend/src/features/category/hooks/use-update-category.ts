import { useMutation, useQueryClient } from "@tanstack/react-query";
import { apiClient } from "@/lib/api-client";
import type { UpdateCategoryInput } from "@/features/category/types/category";

interface UpdateCategoryParams {
  readonly id: string;
  readonly input: UpdateCategoryInput;
}

export function useUpdateCategory() {
  const queryClient = useQueryClient();

  const mutation = useMutation({
    mutationFn: ({ id, input }: UpdateCategoryParams) =>
      apiClient<undefined>({
        url: `/categories/${id}`,
        method: "PUT",
        data: input,
      }),
    onSuccess: (_, variables) => {
      void queryClient.invalidateQueries({ queryKey: ["categories", variables.id] });
      void queryClient.invalidateQueries({ queryKey: ["categories"] });
    },
  });

  return {
    updateCategory: mutation.mutate,
    isUpdating: mutation.isPending,
    error: mutation.error,
  };
}
