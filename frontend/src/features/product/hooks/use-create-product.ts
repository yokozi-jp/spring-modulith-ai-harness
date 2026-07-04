import { useQueryClient } from "@tanstack/react-query";
import { useNavigate } from "@tanstack/react-router";
import { useCreate, getListQueryKey } from "@/api/product/product";

interface CreateProductInput {
  readonly name: string;
  readonly description: string;
  readonly categoryId: string;
  readonly sku: string;
}

export function useCreateProduct() {
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  const mutation = useCreate({
    mutation: {
      onSuccess: () => {
        void queryClient.invalidateQueries({ queryKey: getListQueryKey() });
        void navigate({ to: "/products" });
      },
    },
  });

  function createProduct(input: CreateProductInput) {
    mutation.mutate({ data: input });
  }

  return {
    createProduct,
    isCreating: mutation.isPending,
    error: mutation.error,
  };
}
