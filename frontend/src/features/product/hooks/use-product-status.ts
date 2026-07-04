import { useQueryClient } from "@tanstack/react-query";
import {
  usePublish,
  useUnpublish,
  useArchive,
  getListQueryKey,
  getFindByIdQueryKey,
} from "@/api/product/product";

export function useProductStatus(id: string) {
  const queryClient = useQueryClient();

  const invalidate = () => {
    void queryClient.invalidateQueries({ queryKey: getListQueryKey() });
    void queryClient.invalidateQueries({ queryKey: getFindByIdQueryKey(id) });
  };

  const publishMutation = usePublish({
    mutation: { onSuccess: invalidate },
  });
  const unpublishMutation = useUnpublish({
    mutation: { onSuccess: invalidate },
  });
  const archiveMutation = useArchive({
    mutation: { onSuccess: invalidate },
  });

  function publishProduct(version: number) {
    publishMutation.mutate({ id, data: { version } });
  }

  function unpublishProduct(version: number) {
    unpublishMutation.mutate({ id, data: { version } });
  }

  function archiveProduct(version: number) {
    archiveMutation.mutate({ id, data: { version } });
  }

  return {
    publishProduct,
    unpublishProduct,
    archiveProduct,
    isPending:
      publishMutation.isPending || unpublishMutation.isPending || archiveMutation.isPending,
    error: publishMutation.error ?? unpublishMutation.error ?? archiveMutation.error,
  };
}
