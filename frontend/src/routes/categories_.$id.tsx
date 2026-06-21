import { createFileRoute } from "@tanstack/react-router";
import { CategoryDetailPage } from "@/features/category/components/category-detail-page";

export const Route = createFileRoute("/categories_/$id")({
  component: CategoryDetailRoute,
});

function CategoryDetailRoute() {
  const { id } = Route.useParams();
  return <CategoryDetailPage id={id} />;
}
