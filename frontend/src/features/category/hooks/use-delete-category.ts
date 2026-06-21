import { useMutation, useQueryClient } from "@tanstack/react-query";
import { apiClient } from "@/lib/api-client";

interface DeleteCategoryParams {
  readonly id: string;
  readonly version: number;
}

export function useDeleteCategory() {
  const queryClient = useQueryClient();

  const mutation = useMutation({
    mutationFn: ({ id, version }: DeleteCategoryParams) =>
      apiClient<undefined>({
        url: `/categories/${id}`,
        method: "DELETE",
        data: { version },
      }),
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
