import type { CategorySummaryResponse } from "@/api/openAPIDefinition.schemas";

/** ツリー表示用のカテゴリノード。子カテゴリは展開時に遅延取得するため isLoaded で管理する。 */
export interface CategoryTreeNode {
  readonly id: string;
  readonly name: string;
  readonly sortOrder: number;
  readonly children: CategoryTreeNode[];
  readonly isExpanded: boolean;
  readonly isLoaded: boolean;
}

/** CategorySummaryResponse からツリーノードを生成する（子は未読み込み状態）。 */
export function toCategoryTreeNode(summary: CategorySummaryResponse): CategoryTreeNode {
  return {
    id: summary.id ?? "",
    name: summary.name ?? "",
    sortOrder: summary.sortOrder ?? 0,
    children: [],
    isExpanded: false,
    isLoaded: false,
  };
}

/** 移動先選択などで使うカテゴリの選択肢。 */
export interface CategoryOption {
  readonly id: string;
  readonly name: string;
}
