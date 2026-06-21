import { useMutation, useQueryClient } from "@tanstack/react-query";
import { apiClient } from "@/lib/api-client";

interface ChangeStatusParams {
  readonly id: string;
  readonly version: number;
}

function useMutationFor(action: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, version }: ChangeStatusParams) =>
      apiClient<undefined>({
        url: `/products/${id}/${action}`,
        method: "PATCH",
        data: { version },
      }),
    onSuccess: (_, variables) => {
      void queryClient.invalidateQueries({ queryKey: ["products", variables.id] });
      void queryClient.invalidateQueries({ queryKey: ["products"] });
    },
  });
}

export function usePublishProduct() {
  const mutation = useMutationFor("publish");
  return { publish: mutation.mutate, isPending: mutation.isPending, error: mutation.error };
}

export function useUnpublishProduct() {
  const mutation = useMutationFor("unpublish");
  return { unpublish: mutation.mutate, isPending: mutation.isPending, error: mutation.error };
}

export function useArchiveProduct() {
  const mutation = useMutationFor("archive");
  return { archive: mutation.mutate, isPending: mutation.isPending, error: mutation.error };
}
