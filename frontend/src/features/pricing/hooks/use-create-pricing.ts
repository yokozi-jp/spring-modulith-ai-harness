import { useQueryClient } from "@tanstack/react-query";
import { useNavigate } from "@tanstack/react-router";
import { useCreatePricing as useCreatePricingMutation } from "@/api/pricing/pricing";

/** 価格作成に必要な入力値。 */
export interface CreatePricingInput {
  readonly productId: string;
  readonly level: string;
  readonly areaCode: string;
  readonly amount: number;
  readonly validFrom: string;
  readonly validTo?: string;
}

/** 価格を作成する。成功時は一覧を再取得し一覧ページへ遷移する。 */
export function useCreatePricing() {
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  const mutation = useCreatePricingMutation({
    mutation: {
      onSuccess: () => {
        void queryClient.invalidateQueries({ queryKey: ["pricings"] });
        void navigate({ to: "/pricings" });
      },
    },
  });

  function createPricing(input: CreatePricingInput) {
    mutation.mutate({
      data: {
        productId: input.productId,
        level: input.level,
        areaCode: input.areaCode,
        amount: input.amount,
        validFrom: input.validFrom,
        ...(input.validTo !== undefined && { validTo: input.validTo }),
      },
    });
  }

  return {
    createPricing,
    isCreating: mutation.isPending,
    error: mutation.error,
  };
}
