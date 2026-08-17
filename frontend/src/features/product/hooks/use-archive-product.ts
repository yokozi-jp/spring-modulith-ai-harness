import { useQueryClient } from "@tanstack/react-query";
import { useArchiveProduct as useArchiveProductMutation } from "@/api/product/product";

/** 商品をアーカイブする（→ARCHIVED）。成功時は詳細・一覧を再取得する。 */
export function useArchiveProduct(id: string) {
  const queryClient = useQueryClient();

  const mutation = useArchiveProductMutation({
    mutation: {
      onSuccess: () => {
        void queryClient.invalidateQueries({ queryKey: ["products"] });
      },
    },
  });

  function archiveProduct(version: number) {
    mutation.mutate({ id, data: { version } });
  }

  return {
    archiveProduct,
    isArchiving: mutation.isPending,
    error: mutation.error,
  };
}
