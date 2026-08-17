import { useState } from "react";
import { Link } from "@tanstack/react-router";
import { Button } from "@/components/ui/button";
import { getIndentClass } from "@/features/category/components/tree-indent";
import { useCategoryTreeNodeChildren } from "@/features/category/hooks/use-category-tree-node-children";
import { cn } from "@/lib/utils";

interface CategoryTreeNodeProps {
  readonly id: string;
  readonly name: string;
  readonly depth: number;
}

/** ツリーの単一ノード。展開ボタン押下時に子カテゴリを遅延取得し再帰的に描画する。 */
export function CategoryTreeNodeItem({ id, name, depth }: CategoryTreeNodeProps) {
  const [isExpanded, setIsExpanded] = useState(false);
  const { childNodes, isLoading } = useCategoryTreeNodeChildren(id, isExpanded);

  function handleToggle() {
    setIsExpanded((prev) => !prev);
  }

  return (
    <div>
      <div
        className={cn(
          "flex items-center gap-2 rounded-md py-1.5 hover:bg-accent",
          getIndentClass(depth),
        )}
      >
        <Button
          type="button"
          variant="ghost"
          size="sm"
          className="size-6 p-0"
          onClick={handleToggle}
          aria-label={isExpanded ? "折りたたむ" : "展開する"}
        >
          {isExpanded ? "−" : "+"}
        </Button>
        <Button asChild variant="link" className="h-auto p-0 text-sm">
          <Link to="/categories/$id" params={{ id }}>
            {name}
          </Link>
        </Button>
      </div>
      {isExpanded && isLoading && (
        <p className={cn("text-xs text-muted-foreground", getIndentClass(depth + 1))}>
          読み込み中...
        </p>
      )}
      {isExpanded &&
        !isLoading &&
        childNodes.map((child) => (
          <CategoryTreeNodeItem key={child.id} id={child.id} name={child.name} depth={depth + 1} />
        ))}
    </div>
  );
}
