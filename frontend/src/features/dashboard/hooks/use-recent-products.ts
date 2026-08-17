import { useProductList } from "@/features/product/hooks/use-product-list";

/** ダッシュボード用に最近の商品を5件取得する。 */
export function useRecentProducts() {
  const { products, isLoading, error } = useProductList({}, 0, 5);

  return { products, isLoading, error };
}
