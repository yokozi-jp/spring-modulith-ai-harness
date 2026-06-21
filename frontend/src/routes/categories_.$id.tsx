import { createFileRoute } from "@tanstack/react-router";
import { CategoryDetail } from "@/features/category/components/category-detail";

export const Route = createFileRoute("/categories_/$id")({
  component: CategoryDetailPage,
});

function CategoryDetailPage() {
  const { id } = Route.useParams();

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold">カテゴリ詳細</h1>
      <CategoryDetail id={id} />
    </div>
  );
}
