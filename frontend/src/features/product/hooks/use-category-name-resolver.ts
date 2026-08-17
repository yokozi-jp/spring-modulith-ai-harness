import { useCategoryOptions } from "@/features/category/hooks/use-category-options";

/** categoryId からカテゴリ名を解決する。 */
export function useCategoryNameResolver() {
  const { options } = useCategoryOptions();

  function resolveCategoryName(categoryId: string): string {
    const found = options.find((option) => option.id === categoryId);
    return found?.name ?? categoryId;
  }

  return { resolveCategoryName };
}
