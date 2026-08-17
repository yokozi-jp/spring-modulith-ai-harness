import { createFileRoute } from "@tanstack/react-router";
import { ProductDetailPageContent } from "@/features/product/components/product-detail-page-content";

export const Route = createFileRoute("/products_/$id")({
  component: ProductDetailPage,
});

function ProductDetailPage() {
  const { id } = Route.useParams();

  return <ProductDetailPageContent productId={id} />;
}
