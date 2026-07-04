import { useQueryClient } from "@tanstack/react-query";
import { useNavigate } from "@tanstack/react-router";
import { useUpdate2, getList2QueryKey, getFindById2QueryKey } from "@/api/category/category";

interface UpdateCategoryInput {
  readonly name: string;
  readonly sortOrder: number;
  readonly version: number;
}

export function useUpdateCategory(id: string) {
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  const mutation = useUpdate2({
    mutation: {
      onSuccess: () => {
        void queryClient.invalidateQueries({
          queryKey: getList2QueryKey(),
        });
        void queryClient.invalidateQueries({
          queryKey: getFindById2QueryKey(id),
        });
        void navigate({ to: "/categories/$id", params: { id } });
      },
    },
  });

  function updateCategory(input: UpdateCategoryInput) {
    mutation.mutate({
      id,
      data: {
        name: input.name,
        sortOrder: input.sortOrder,
        version: input.version,
      },
    });
  }

  return {
    updateCategory,
    isUpdating: mutation.isPending,
    error: mutation.error,
  };
}
