import { useQueryClient } from "@tanstack/react-query";
import { useUnpublishProduct as useUnpublishProductMutation } from "@/api/product/product";

/** 商品を非公開にする（PUBLISHED→DRAFT）。成功時は詳細・一覧を再取得する。 */
export function useUnpublishProduct(id: string) {
  const queryClient = useQueryClient();

  const mutation = useUnpublishProductMutation({
    mutation: {
      onSuccess: () => {
        void queryClient.invalidateQueries({ queryKey: ["products"] });
      },
    },
  });

  function unpublishProduct(version: number) {
    mutation.mutate({ id, data: { version } });
  }

  return {
    unpublishProduct,
    isUnpublishing: mutation.isPending,
    error: mutation.error,
  };
}
