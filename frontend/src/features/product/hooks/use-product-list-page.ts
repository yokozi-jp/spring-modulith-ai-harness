import { useState } from "react";
import { useProductList } from "@/features/product/hooks/use-product-list";
import type { ProductListFilterValues } from "@/features/product/components/product-filter";

/** 商品一覧ページの状態（フィルタ・ページ）とデータ取得をまとめる。 */
export function useProductListPage() {
  const [filter, setFilter] = useState<ProductListFilterValues>({
    categoryId: undefined,
    status: undefined,
  });
  const [page, setPage] = useState(0);

  const { products, totalPages, isLoading, error, refetch } = useProductList(
    {
      ...(filter.categoryId !== undefined && { categoryId: filter.categoryId }),
      ...(filter.status !== undefined && { status: filter.status }),
    },
    page,
  );

  function handleFilterChange(newFilter: ProductListFilterValues) {
    setFilter(newFilter);
    setPage(0);
  }

  return {
    filter,
    page,
    products,
    totalPages,
    isLoading,
    error,
    refetch,
    onFilterChange: handleFilterChange,
    onPageChange: setPage,
  };
}
