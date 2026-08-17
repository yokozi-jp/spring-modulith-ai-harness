import { useQueryClient } from "@tanstack/react-query";
import { useNavigate } from "@tanstack/react-router";
import { useDeleteProduct as useDeleteProductMutation } from "@/api/product/product";

/** 商品を削除する。成功時は一覧を再取得し一覧ページへ遷移する。 */
export function useDeleteProduct() {
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  const mutation = useDeleteProductMutation({
    mutation: {
      onSuccess: () => {
        void queryClient.invalidateQueries({ queryKey: ["products"] });
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
