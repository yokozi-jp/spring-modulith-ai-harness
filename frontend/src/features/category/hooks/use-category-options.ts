import { useCategoryList } from "@/features/category/hooks/use-category-list";
import type { CategoryOption } from "@/features/category/types/category";

/** 親カテゴリ選択用にルートカテゴリの選択肢一覧を返す。 */
export function useCategoryOptions() {
  const { categories, isLoading, error } = useCategoryList();

  const options: CategoryOption[] = categories.map((category) => ({
    id: category.id ?? "",
    name: category.name ?? "",
  }));

  return { options, isLoading, error };
}
