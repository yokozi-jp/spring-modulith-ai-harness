import { createFileRoute } from "@tanstack/react-router";
import { ProductEditForm } from "@/features/product/components/product-edit-form";

export const Route = createFileRoute("/products_/$id_/edit")({
  component: ProductEditPage,
});

function ProductEditPage() {
  const { id } = Route.useParams();

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">商品編集</h1>
      <ProductEditForm id={id} />
    </div>
  );
}
