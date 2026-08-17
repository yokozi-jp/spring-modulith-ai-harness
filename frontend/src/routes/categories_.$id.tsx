import { createFileRoute } from "@tanstack/react-router";
import { CategoryDetailPageContent } from "@/features/category/components/category-detail-page-content";

export const Route = createFileRoute("/categories_/$id")({
  component: CategoryDetailPage,
});

function CategoryDetailPage() {
  const { id } = Route.useParams();

  return <CategoryDetailPageContent categoryId={id} />;
}
