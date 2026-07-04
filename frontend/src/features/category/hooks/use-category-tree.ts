import { useQuery } from "@tanstack/react-query";
import { list2, findChildren } from "@/api/category/category";
import type { CategorySummaryResponse } from "@/api/openAPIDefinition.schemas";

export interface CategoryTreeItem {
  readonly id: string;
  readonly name: string;
  readonly sortOrder: number;
  readonly depth: number;
}

async function fetchAllCategories(): Promise<CategoryTreeItem[]> {
  const rootResponse = await list2({
    param: {},
    pageable: { page: 0, size: 100, sort: ["sortOrder,asc"] },
  });
  const roots = rootResponse.data?.content ?? [];
  const result: CategoryTreeItem[] = [];

  async function fetchRecursive(categories: CategorySummaryResponse[], depth: number) {
    for (const category of categories) {
      const id = category.id ?? "";
      result.push({
        id,
        name: category.name ?? "",
        sortOrder: category.sortOrder ?? 0,
        depth,
      });
      if (id.length > 0) {
        const childrenResponse = await findChildren(id);
        const children = childrenResponse.data ?? [];
        if (children.length > 0) {
          await fetchRecursive(children, depth + 1);
        }
      }
    }
  }

  await fetchRecursive(roots, 0);
  return result;
}

export function useCategoryTree() {
  const query = useQuery({
    queryKey: ["categories", "tree"],
    queryFn: fetchAllCategories,
  });

  return {
    categories: query.data ?? [],
    isLoading: query.isLoading,
    error: query.error,
  };
}
