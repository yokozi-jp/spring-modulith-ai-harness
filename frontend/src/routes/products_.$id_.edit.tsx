import { createFileRoute } from "@tanstack/react-router";
import { ProductEditPageContent } from "@/features/product/components/product-edit-page-content";

export const Route = createFileRoute("/products_/$id_/edit")({
  component: ProductEditPage,
});

function ProductEditPage() {
  const { id } = Route.useParams();

  return <ProductEditPageContent productId={id} />;
}
