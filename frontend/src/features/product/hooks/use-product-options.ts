import { useList } from "@/api/product/product";
import type { ProductSummaryResponse } from "@/api/openAPIDefinition.schemas";

interface ProductOption {
  readonly id: string;
  readonly name: string;
}

export function useProductOptions() {
  const query = useList({
    param: {},
    pageable: { page: 0, size: 1000, sort: ["name,asc"] },
  });

  const products = (query.data?.data?.content ?? []) as ProductSummaryResponse[];
  const options: ProductOption[] = products
    .filter(
      (p): p is ProductSummaryResponse & { id: string; name: string } =>
        p.id !== undefined && p.name !== undefined,
    )
    .map((p) => ({ id: p.id, name: p.name }));

  return {
    options,
    isLoading: query.isLoading,
    error: query.error,
  };
}
