import { createFileRoute } from "@tanstack/react-router";
import { Link } from "@tanstack/react-router";
import { Button } from "@/components/ui/button";
import { CategoryList } from "@/features/category/components/category-list";

export const Route = createFileRoute("/categories")({
  component: CategoriesPage,
});

function CategoriesPage() {
  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">カテゴリ管理</h1>
        <Button asChild>
          <Link to="/categories/new">新規作成</Link>
        </Button>
      </div>
      <CategoryList />
    </div>
  );
}
