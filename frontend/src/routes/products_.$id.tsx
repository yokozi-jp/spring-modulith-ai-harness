import { createFileRoute } from "@tanstack/react-router";
import { ProductDetail } from "@/features/product/components/product-detail";

export const Route = createFileRoute("/products_/$id")({
  component: ProductDetailPage,
});

function ProductDetailPage() {
  const { id } = Route.useParams();

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">商品詳細</h1>
      <ProductDetail id={id} />
    </div>
  );
}
