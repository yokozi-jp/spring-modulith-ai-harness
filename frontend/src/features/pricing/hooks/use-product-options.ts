import { useProductList } from "@/features/product/hooks/use-product-list";

/** 商品選択用のオプション。 */
export interface ProductOption {
  readonly id: string;
  readonly name: string;
}

/** 商品選択のセレクトボックス用にオプション一覧を返す。 */
export function useProductOptions() {
  const { products, isLoading, error } = useProductList({}, 0, 100);

  const options: ProductOption[] = products.map((product) => ({
    id: product.id ?? "",
    name: product.name ?? "",
  }));

  return { options, isLoading, error };
}
