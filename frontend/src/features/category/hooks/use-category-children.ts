import { useFindCategoryChildren } from "@/api/category/category";

/** 指定カテゴリの直接の子カテゴリ一覧を取得する。id が空文字の場合は実行しない。 */
export function useCategoryChildren(id: string) {
  const query = useFindCategoryChildren(id, {
    query: {
      queryKey: ["categories", id, "children"],
      enabled: id.length > 0,
    },
  });

  return {
    children: query.data?.data ?? [],
    isLoading: query.isLoading,
    error: query.error,
    refetch: query.refetch,
  };
}
