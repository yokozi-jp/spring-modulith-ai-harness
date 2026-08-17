import { useCategoryChildren } from "@/features/category/hooks/use-category-children";
import { toCategoryTreeNode } from "@/features/category/types/category";
import type { CategoryTreeNode } from "@/features/category/types/category";

/** 指定ノードの子カテゴリを展開時に遅延取得する。 */
export function useCategoryTreeNodeChildren(nodeId: string, isExpanded: boolean) {
  const query = useCategoryChildren(isExpanded ? nodeId : "");

  const childNodes: CategoryTreeNode[] = query.children.map(toCategoryTreeNode);

  return {
    childNodes,
    isLoading: isExpanded && query.isLoading,
    error: query.error,
  };
}
