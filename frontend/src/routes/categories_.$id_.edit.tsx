import { createFileRoute } from "@tanstack/react-router";
import { CategoryEditPageContent } from "@/features/category/components/category-edit-page-content";

export const Route = createFileRoute("/categories_/$id_/edit")({
  component: CategoryEditPage,
});

function CategoryEditPage() {
  const { id } = Route.useParams();

  return <CategoryEditPageContent categoryId={id} />;
}
