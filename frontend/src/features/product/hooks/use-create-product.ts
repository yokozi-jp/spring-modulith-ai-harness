import { useQueryClient } from "@tanstack/react-query";
import { useNavigate } from "@tanstack/react-router";
import { useCreateProduct as useCreateProductMutation } from "@/api/product/product";

/** 商品作成に必要な入力値。 */
export interface CreateProductInput {
  readonly name: string;
  readonly description: string;
  readonly categoryId: string;
  readonly sku: string;
}

/** 商品を作成する。成功時は一覧を再取得し一覧ページへ遷移する。 */
export function useCreateProduct() {
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  const mutation = useCreateProductMutation({
    mutation: {
      onSuccess: () => {
        void queryClient.invalidateQueries({ queryKey: ["products"] });
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
