import { useQueryClient } from "@tanstack/react-query";
import { useUpdate1, getList1QueryKey, getFindById1QueryKey } from "@/api/pricing/pricing";

interface UpdatePricingInput {
  readonly amount: number;
  readonly validFrom: string;
  readonly validTo?: string;
  readonly version: number;
}

export function useUpdatePricing(id: string) {
  const queryClient = useQueryClient();

  const mutation = useUpdate1({
    mutation: {
      onSuccess: () => {
        void queryClient.invalidateQueries({ queryKey: getList1QueryKey() });
        void queryClient.invalidateQueries({
          queryKey: getFindById1QueryKey(id),
        });
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
    isSuccess: mutation.isSuccess,
    error: mutation.error,
    reset: mutation.reset,
  };
}
