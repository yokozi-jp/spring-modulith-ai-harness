import { createFileRoute } from "@tanstack/react-router";
import { CategoryEditPage } from "@/features/category/components/category-edit-page";

export const Route = createFileRoute("/categories_/$id_/edit")({
  component: CategoryEditRoute,
});

function CategoryEditRoute() {
  const { id } = Route.useParams();
  return <CategoryEditPage id={id} />;
}
