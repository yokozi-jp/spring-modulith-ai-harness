import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { ProductForm } from "@/features/product/components/product-form";
import { useCreateProduct } from "@/features/product/hooks/use-create-product";
import { ErrorMessage } from "@/components/error-message";

export const Route = createFileRoute("/products_/new")({
  component: ProductNewPage,
});

function ProductNewPage() {
  const { createProduct, isCreating, error } = useCreateProduct();
  const navigate = useNavigate();

  function handleSubmit(data: {
    name: string;
    description: string;
    categoryId: string;
    sku: string;
  }) {
    createProduct(data, {
      onSuccess: () => {
        void navigate({ to: "/products" });
      },
    });
  }

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold">商品作成</h1>
      <ErrorMessage error={error} />
      <ProductForm onSubmit={handleSubmit} isSubmitting={isCreating} submitLabel="作成" />
    </div>
  );
}
