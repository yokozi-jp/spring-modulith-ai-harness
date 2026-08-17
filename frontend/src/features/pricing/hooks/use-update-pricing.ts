import { useQueryClient } from "@tanstack/react-query";
import { useNavigate } from "@tanstack/react-router";
import { useUpdatePricing as useUpdatePricingMutation } from "@/api/pricing/pricing";

/** 価格更新に必要な入力値。 */
export interface UpdatePricingInput {
  readonly amount: number;
  readonly validFrom: string;
  readonly validTo?: string;
  readonly version: number;
}

/** 価格を更新する。成功時は詳細・一覧を再取得し詳細ページへ遷移する。 */
export function useUpdatePricing(id: string) {
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  const mutation = useUpdatePricingMutation({
    mutation: {
      onSuccess: () => {
        void queryClient.invalidateQueries({ queryKey: ["pricings"] });
        void navigate({ to: "/pricings/$id", params: { id } });
      },
    },
  });

  function updatePricing(input: UpdatePricingInput) {
    mutation.mutate({
      id,
      data: {
        amount: input.amount,
        validFrom: input.validFrom,
        version: input.version,
        ...(input.validTo !== undefined && { validTo: input.validTo }),
      },
    });
  }

  return {
    updatePricing,
    isUpdating: mutation.isPending,
    error: mutation.error,
  };
}
