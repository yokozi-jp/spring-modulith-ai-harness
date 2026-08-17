import { useListCategory } from "@/api/category/category";

/** ルートカテゴリ一覧を取得する。 */
export function useCategoryList() {
  const query = useListCategory(
    {
      page: 0,
      size: 100,
      sort: ["sortOrder,asc"],
    },
    {
      query: { queryKey: ["categories", "root"] },
    },
  );

  return {
    categories: query.data?.data?.content ?? [],
    isLoading: query.isLoading,
    error: query.error,
    refetch: query.refetch,
  };
}
