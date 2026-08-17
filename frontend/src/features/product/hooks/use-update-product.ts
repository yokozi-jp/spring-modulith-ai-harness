import { useQueryClient } from "@tanstack/react-query";
import { useNavigate } from "@tanstack/react-router";
import { useUpdateProduct as useUpdateProductMutation } from "@/api/product/product";

/** 商品更新に必要な入力値。 */
export interface UpdateProductInput {
  readonly name: string;
  readonly description: string;
  readonly categoryId: string;
  readonly version: number;
}

/** 商品を更新する。成功時は詳細・一覧を再取得し詳細ページへ遷移する。 */
export function useUpdateProduct(id: string) {
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  const mutation = useUpdateProductMutation({
    mutation: {
      onSuccess: () => {
        void queryClient.invalidateQueries({ queryKey: ["products"] });
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
