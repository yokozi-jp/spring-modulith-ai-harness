import { createFileRoute } from "@tanstack/react-router";
import { CategoryEditForm } from "@/features/category/components/category-edit-form";

export const Route = createFileRoute("/categories_/$id_/edit")({
  component: CategoryEditPage,
});

function CategoryEditPage() {
  const { id } = Route.useParams();

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">カテゴリ編集</h1>
      <CategoryEditForm id={id} />
    </div>
  );
}
