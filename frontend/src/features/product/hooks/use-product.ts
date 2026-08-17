import { useFindProductById } from "@/api/product/product";

/** 商品詳細を取得する。id が空文字の場合は実行しない。 */
export function useProduct(id: string) {
  const query = useFindProductById(id, {
    query: {
      queryKey: ["products", id],
      enabled: id.length > 0,
    },
  });

  return {
    product: query.data?.data ?? null,
    isLoading: query.isLoading,
    error: query.error,
    refetch: query.refetch,
  };
}
