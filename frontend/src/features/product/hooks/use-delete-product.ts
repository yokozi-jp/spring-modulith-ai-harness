import { useQueryClient } from "@tanstack/react-query";
import { useNavigate } from "@tanstack/react-router";
import { useDelete, getListQueryKey } from "@/api/product/product";

export function useDeleteProduct() {
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  const mutation = useDelete({
    mutation: {
      onSuccess: () => {
        void queryClient.invalidateQueries({ queryKey: getListQueryKey() });
        void navigate({ to: "/products" });
      },
    },
  });

  function deleteProduct(id: string, version: number) {
    mutation.mutate({ id, data: { version } });
  }

  return {
    deleteProduct,
    isDeleting: mutation.isPending,
    error: mutation.error,
  };
}
