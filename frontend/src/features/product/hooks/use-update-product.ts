import { useMutation, useQueryClient } from "@tanstack/react-query";
import { apiClient } from "@/lib/api-client";
import type { UpdateProductInput } from "@/features/product/types/product";

interface UpdateProductParams {
  readonly id: string;
  readonly input: UpdateProductInput;
}

export function useUpdateProduct() {
  const queryClient = useQueryClient();

  const mutation = useMutation({
    mutationFn: ({ id, input }: UpdateProductParams) =>
      apiClient<undefined>({
        url: `/products/${id}`,
        method: "PUT",
        data: input,
      }),
    onSuccess: (_, variables) => {
      void queryClient.invalidateQueries({ queryKey: ["products", variables.id] });
      void queryClient.invalidateQueries({ queryKey: ["products"] });
    },
  });

  return {
    updateProduct: mutation.mutate,
    isUpdating: mutation.isPending,
    error: mutation.error,
  };
}
