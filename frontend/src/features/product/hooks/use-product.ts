import { useFindById } from "@/api/product/product";
import type { ProductDetailResponse } from "@/api/openAPIDefinition.schemas";

interface ProductDetail {
  readonly id: string;
  readonly name: string;
  readonly description: string;
  readonly categoryId: string;
  readonly sku: string;
  readonly status: string;
  readonly version: number;
}

function toDetail(data: ProductDetailResponse): ProductDetail {
  return {
    id: data.id ?? "",
    name: data.name ?? "",
    description: data.description ?? "",
    categoryId: data.categoryId ?? "",
    sku: data.sku ?? "",
    status: data.status ?? "DRAFT",
    version: data.version ?? 0,
  };
}

export function useProduct(id: string) {
  const query = useFindById(id);

  return {
    product: query.data?.data !== undefined ? toDetail(query.data.data) : null,
    isLoading: query.isLoading,
    error: query.error,
    refetch: query.refetch,
  };
}
