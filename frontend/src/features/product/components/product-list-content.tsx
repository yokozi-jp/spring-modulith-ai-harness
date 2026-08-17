import { ProductFilter } from "@/features/product/components/product-filter";
import { ProductListBody } from "@/features/product/components/product-list-body";
import { ProductListPagination } from "@/features/product/components/product-list-pagination";
import { useProductListPage } from "@/features/product/hooks/use-product-list-page";

/** 商品一覧ページのコンテンツ（フィルタ・テーブル・ページ送りをまとめる）。 */
export function ProductListContent() {
  const {
    filter,
    page,
    products,
    totalPages,
    isLoading,
    error,
    refetch,
    onFilterChange,
    onPageChange,
  } = useProductListPage();

  return (
    <div className="flex flex-col gap-4">
      <ProductFilter filter={filter} onChange={onFilterChange} />
      <ProductListBody
        products={products}
        isLoading={isLoading}
        error={error}
        onRetry={() => {
          void refetch();
        }}
      />
      <ProductListPagination page={page} totalPages={totalPages} onPageChange={onPageChange} />
    </div>
  );
}
