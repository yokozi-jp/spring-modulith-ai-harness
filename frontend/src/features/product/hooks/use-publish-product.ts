import { useQueryClient } from "@tanstack/react-query";
import { usePublishProduct as usePublishProductMutation } from "@/api/product/product";

/** 商品を公開する（DRAFT→PUBLISHED）。成功時は詳細・一覧を再取得する。 */
export function usePublishProduct(id: string) {
  const queryClient = useQueryClient();

  const mutation = usePublishProductMutation({
    mutation: {
      onSuccess: () => {
        void queryClient.invalidateQueries({ queryKey: ["products"] });
      },
    },
  });

  function publishProduct(version: number) {
    mutation.mutate({ id, data: { version } });
  }

  return {
    publishProduct,
    isPublishing: mutation.isPending,
    error: mutation.error,
  };
}
