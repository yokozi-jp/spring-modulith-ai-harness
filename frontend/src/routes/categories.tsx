import { Link, createFileRoute } from "@tanstack/react-router";
import { Button } from "@/components/ui/button";
import { CategoryTree } from "@/features/category/components/category-tree";

export const Route = createFileRoute("/categories")({
  component: CategoriesPage,
});

function CategoriesPage() {
  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold tracking-tight">カテゴリ管理</h1>
        <Button asChild>
          <Link to="/categories/new">新規作成</Link>
        </Button>
      </div>
      <CategoryTree />
    </div>
  );
}
