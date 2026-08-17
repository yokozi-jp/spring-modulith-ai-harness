import { useQueryClient } from "@tanstack/react-query";
import { useNavigate } from "@tanstack/react-router";
import { useDeletePricing as useDeletePricingMutation } from "@/api/pricing/pricing";

/** 価格を削除する。成功時は一覧を再取得し一覧ページへ遷移する。 */
export function useDeletePricing() {
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  const mutation = useDeletePricingMutation({
    mutation: {
      onSuccess: () => {
        void queryClient.invalidateQueries({ queryKey: ["pricings"] });
        void navigate({ to: "/pricings" });
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
