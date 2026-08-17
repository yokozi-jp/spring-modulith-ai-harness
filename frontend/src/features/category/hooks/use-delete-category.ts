import { useQueryClient } from "@tanstack/react-query";
import { useNavigate } from "@tanstack/react-router";
import { useDeleteCategory as useDeleteCategoryMutation } from "@/api/category/category";

/** カテゴリを削除する。成功時は一覧を再取得し一覧ページへ遷移する。 */
export function useDeleteCategory() {
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  const mutation = useDeleteCategoryMutation({
    mutation: {
      onSuccess: () => {
        void queryClient.invalidateQueries({ queryKey: ["categories"] });
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
