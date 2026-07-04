import { useQueryClient } from "@tanstack/react-query";
import { useCreate1, getList1QueryKey } from "@/api/pricing/pricing";

interface CreatePricingInput {
  readonly productId: string;
  readonly level: string;
  readonly areaCode: string;
  readonly amount: number;
  readonly validFrom: string;
  readonly validTo?: string;
}

export function useCreatePricing() {
  const queryClient = useQueryClient();

  const mutation = useCreate1({
    mutation: {
      onSuccess: () => {
        void queryClient.invalidateQueries({ queryKey: getList1QueryKey() });
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
    isSuccess: mutation.isSuccess,
    error: mutation.error,
    reset: mutation.reset,
  };
}
