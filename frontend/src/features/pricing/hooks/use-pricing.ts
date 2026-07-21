import { useFindById1 } from "@/api/pricing/pricing";
import type { PricingDetailResponse } from "@/api/openAPIDefinition.schemas";

interface PricingDetail {
  readonly id: string;
  readonly productId: string;
  readonly level: string;
  readonly areaCode: string;
  readonly amount: number;
  readonly validFrom: string;
  readonly validTo: string | null;
  readonly version: number;
}

function toDetail(data: PricingDetailResponse): PricingDetail {
  return {
    id: data.id ?? "",
    productId: data.productId ?? "",
    level: data.level ?? "",
    areaCode: data.areaCode ?? "",
    amount: data.amount ?? 0,
    validFrom: data.validFrom ?? "",
    validTo: data.validTo ?? null,
    version: data.version ?? 0,
  };
}

export function usePricing(id: string) {
  const query = useFindById1(id, {
    query: { enabled: id !== "" },
  });

  return {
    pricing: query.data?.data !== undefined ? toDetail(query.data.data) : null,
    isLoading: query.isLoading,
    error: query.error,
  };
}
