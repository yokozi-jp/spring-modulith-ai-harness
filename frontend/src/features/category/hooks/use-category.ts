import { useFindCategoryById } from "@/api/category/category";

/** カテゴリ詳細を取得する。id が空文字の場合は実行しない。 */
export function useCategory(id: string) {
  const query = useFindCategoryById(id, {
    query: {
      queryKey: ["categories", id],
      enabled: id.length > 0,
    },
  });

  return {
    category: query.data?.data ?? null,
    isLoading: query.isLoading,
    error: query.error,
    refetch: query.refetch,
  };
}
