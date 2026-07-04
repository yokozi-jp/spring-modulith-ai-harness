import { useQueryClient } from "@tanstack/react-query";
import { useNavigate } from "@tanstack/react-router";
import { useDelete2, getList2QueryKey } from "@/api/category/category";

export function useDeleteCategory() {
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  const mutation = useDelete2({
    mutation: {
      onSuccess: () => {
        void queryClient.invalidateQueries({
          queryKey: getList2QueryKey(),
        });
        void navigate({ to: "/categories" });
      },
    },
  });

  function deleteCategory(id: string, version: number) {
    mutation.mutate({ id, data: { version } });
  }

  return {
    deleteCategory,
    isDeleting: mutation.isPending,
    error: mutation.error,
  };
}
