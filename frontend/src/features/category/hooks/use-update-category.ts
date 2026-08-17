import { useQueryClient } from "@tanstack/react-query";
import { useNavigate } from "@tanstack/react-router";
import { useUpdateCategory as useUpdateCategoryMutation } from "@/api/category/category";

/** カテゴリ更新に必要な入力値。 */
export interface UpdateCategoryInput {
  readonly name: string;
  readonly sortOrder: number;
  readonly version: number;
}

/** カテゴリを更新する。成功時は詳細・一覧を再取得し詳細ページへ遷移する。 */
export function useUpdateCategory(id: string) {
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  const mutation = useUpdateCategoryMutation({
    mutation: {
      onSuccess: () => {
        void queryClient.invalidateQueries({ queryKey: ["categories"] });
        void navigate({ to: "/categories/$id", params: { id } });
      },
    },
  });

  function updateCategory(input: UpdateCategoryInput) {
    mutation.mutate({
      id,
      data: { name: input.name, sortOrder: input.sortOrder, version: input.version },
    });
  }

  return {
    updateCategory,
    isUpdating: mutation.isPending,
    error: mutation.error,
  };
}
