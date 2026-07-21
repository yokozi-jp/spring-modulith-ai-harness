import { useState } from "react";
import { Link } from "@tanstack/react-router";
import { ChevronRight, ChevronDown } from "lucide-react";
import { cn } from "@/lib/utils";
import { useCategoryChildren } from "@/features/category/hooks/use-category-children";

interface CategoryTreeNodeProps {
  readonly id: string;
  readonly name: string;
  readonly sortOrder: number;
  readonly depth: number;
}

export function CategoryTreeNode({ id, name, sortOrder, depth }: CategoryTreeNodeProps) {
  const [isExpanded, setIsExpanded] = useState(false);

  return (
    <li>
      <div
        className={cn(
          "group flex items-center gap-2 rounded-lg px-2.5 py-2 transition-colors",
          "hover:bg-accent/50",
        )}
        style={{ paddingLeft: `${String(depth * 1.25 + 0.625)}rem` }}
      >
        <button
          type="button"
          onClick={() => {
            setIsExpanded((prev) => !prev);
          }}
          className="flex size-5 items-center justify-center rounded-md text-muted-foreground transition-colors hover:bg-muted hover:text-foreground"
          aria-label={isExpanded ? "折りたたむ" : "展開する"}
        >
          {isExpanded ? (
            <ChevronDown className="size-3.5" />
          ) : (
            <ChevronRight className="size-3.5" />
          )}
        </button>
        <Link
          to="/categories/$id"
          params={{ id }}
          className="flex-1 truncate text-sm font-medium transition-colors hover:text-primary"
        >
          {name}
        </Link>
        <span className="text-[11px] tabular-nums text-muted-foreground opacity-0 transition-opacity group-hover:opacity-100">
          #{sortOrder}
        </span>
      </div>
      {isExpanded && <ChildNodes parentId={id} depth={depth + 1} />}
    </li>
  );
}

interface ChildNodesProps {
  readonly parentId: string;
  readonly depth: number;
}

function ChildNodes({ parentId, depth }: ChildNodesProps) {
  const { children, isLoading } = useCategoryChildren(parentId);

  if (isLoading) {
    return (
      <div className="py-1.5" style={{ paddingLeft: `${String(depth * 1.25 + 0.625)}rem` }}>
        <div className="h-4 w-24 animate-pulse rounded bg-muted" />
      </div>
    );
  }

  if (children.length === 0) {
    return (
      <div
        className="py-1.5 text-xs italic text-muted-foreground/60"
        style={{ paddingLeft: `${String(depth * 1.25 + 2.25)}rem` }}
      >
        子カテゴリなし
      </div>
    );
  }

  return (
    <ul>
      {children.map((child) => (
        <CategoryTreeNode
          key={child.id}
          id={child.id ?? ""}
          name={child.name ?? ""}
          sortOrder={child.sortOrder ?? 0}
          depth={depth}
        />
      ))}
    </ul>
  );
}
