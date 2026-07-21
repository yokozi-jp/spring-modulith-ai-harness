import { useQuery } from "@tanstack/react-query";
import { list2, findChildren } from "@/api/category/category";
import type { CategorySummaryResponse } from "@/api/openAPIDefinition.schemas";

interface CategoryOption {
  readonly id: string;
  readonly name: string;
  readonly depth: number;
}

async function fetchAllCategories(): Promise<CategoryOption[]> {
  const rootResponse = await list2({
    param: {},
    pageable: { page: 0, size: 1000, sort: ["sortOrder,asc"] },
  });

  const roots = (rootResponse.data?.content ?? []) as CategorySummaryResponse[];
  const result: CategoryOption[] = [];

  async function fetchChildren(parentId: string, depth: number) {
    const response = await findChildren(parentId);
    const children = (response.data ?? []) as CategorySummaryResponse[];

    for (const child of children) {
      if (child.id !== undefined && child.name !== undefined) {
        result.push({ id: child.id, name: child.name, depth });
        await fetchChildren(child.id, depth + 1);
      }
    }
  }

  for (const root of roots) {
    if (root.id !== undefined && root.name !== undefined) {
      result.push({ id: root.id, name: root.name, depth: 0 });
      await fetchChildren(root.id, 1);
    }
  }

  return result;
}

export function useCategoryOptions() {
  const query = useQuery({
    queryKey: ["categories", "all-tree"],
    queryFn: fetchAllCategories,
  });

  const options = query.data ?? [];

  return {
    options,
    isLoading: query.isLoading,
    error: query.error,
  };
}
