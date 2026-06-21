import { useMutation, useQueryClient } from "@tanstack/react-query";
import { apiClient } from "@/lib/api-client";
import type { CreateProductInput } from "@/features/product/types/product";

export function useCreateProduct() {
  const queryClient = useQueryClient();

  const mutation = useMutation({
    mutationFn: (input: CreateProductInput) =>
      apiClient<undefined>({
        url: "/products",
        method: "POST",
        data: input,
      }),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["products"] });
    },
  });

  return {
    createProduct: mutation.mutate,
    isCreating: mutation.isPending,
    error: mutation.error,
  };
}
