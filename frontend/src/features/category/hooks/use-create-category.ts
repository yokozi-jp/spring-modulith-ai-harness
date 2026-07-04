import { useQueryClient } from "@tanstack/react-query";
import { useNavigate } from "@tanstack/react-router";
import { useCreate2, getList2QueryKey } from "@/api/category/category";

interface CreateCategoryInput {
  readonly name: string;
  readonly sortOrder: number;
  readonly parentCategoryId?: string;
}

export function useCreateCategory() {
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  const mutation = useCreate2({
    mutation: {
      onSuccess: () => {
        void queryClient.invalidateQueries({
          queryKey: getList2QueryKey(),
        });
        void navigate({ to: "/categories" });
      },
    },
  });

  function createCategory(input: CreateCategoryInput) {
    mutation.mutate({
      data: {
        name: input.name,
        sortOrder: input.sortOrder,
        ...(input.parentCategoryId !== undefined && {
          parentCategoryId: input.parentCategoryId,
        }),
      },
    });
  }

  return {
    createCategory,
    isCreating: mutation.isPending,
    error: mutation.error,
  };
}
