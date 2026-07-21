import { useQueryClient } from "@tanstack/react-query";
import { useNavigate } from "@tanstack/react-router";
import { useUpdate, getListQueryKey, getFindByIdQueryKey } from "@/api/product/product";

interface UpdateProductInput {
  readonly name: string;
  readonly description: string;
  readonly categoryId: string;
  readonly version: number;
}

export function useUpdateProduct(id: string) {
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  const mutation = useUpdate({
    mutation: {
      onSuccess: () => {
        void queryClient.invalidateQueries({ queryKey: getListQueryKey() });
        void queryClient.invalidateQueries({
          queryKey: getFindByIdQueryKey(id),
        });
        void navigate({ to: "/products/$id", params: { id } });
      },
    },
  });

  function updateProduct(input: UpdateProductInput) {
    mutation.mutate({ id, data: input });
  }

  return {
    updateProduct,
    isUpdating: mutation.isPending,
    error: mutation.error,
  };
}
