import { useQueryClient } from "@tanstack/react-query";
import { useNavigate } from "@tanstack/react-router";
import { useCreateCategory as useCreateCategoryMutation } from "@/api/category/category";
import type { CreateCategoryRequest } from "@/api/openAPIDefinition.schemas";

/** カテゴリ作成に必要な入力値。 */
export interface CreateCategoryInput {
  readonly name: string;
  readonly sortOrder: number;
  readonly parentCategoryId?: string;
}

/** カテゴリを作成する。成功時は一覧を再取得し一覧ページへ遷移する。 */
export function useCreateCategory() {
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  const mutation = useCreateCategoryMutation({
    mutation: {
      onSuccess: () => {
        void queryClient.invalidateQueries({ queryKey: ["categories"] });
        void navigate({ to: "/categories" });
      },
    },
  });

  function createCategory(input: CreateCategoryInput) {
    const requestData: CreateCategoryRequest = {
      name: input.name,
      sortOrder: input.sortOrder,
      ...(input.parentCategoryId !== undefined && { parentCategoryId: input.parentCategoryId }),
    };
    mutation.mutate({ data: requestData });
  }

  return {
    createCategory,
    isCreating: mutation.isPending,
    error: mutation.error,
  };
}
