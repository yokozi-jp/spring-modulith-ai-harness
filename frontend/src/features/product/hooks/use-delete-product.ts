import { useMutation, useQueryClient } from "@tanstack/react-query";
import { apiClient } from "@/lib/api-client";

interface DeleteProductParams {
  readonly id: string;
  readonly version: number;
}

export function useDeleteProduct() {
  const queryClient = useQueryClient();

  const mutation = useMutation({
    mutationFn: ({ id, version }: DeleteProductParams) =>
      apiClient<undefined>({
        url: `/products/${id}`,
        method: "DELETE",
        data: { version },
      }),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["products"] });
    },
  });

  return {
    deleteProduct: mutation.mutate,
    isDeleting: mutation.isPending,
    error: mutation.error,
  };
}
