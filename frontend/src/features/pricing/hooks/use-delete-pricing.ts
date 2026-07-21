import { useQueryClient } from "@tanstack/react-query";
import { useDelete1, getList1QueryKey } from "@/api/pricing/pricing";

export function useDeletePricing() {
  const queryClient = useQueryClient();

  const mutation = useDelete1({
    mutation: {
      onSuccess: () => {
        void queryClient.invalidateQueries({ queryKey: getList1QueryKey() });
      },
    },
  });

  function deletePricing(id: string, version: number) {
    mutation.mutate({ id, data: { version } });
  }

  return {
    deletePricing,
    isDeleting: mutation.isPending,
    error: mutation.error,
  };
}
