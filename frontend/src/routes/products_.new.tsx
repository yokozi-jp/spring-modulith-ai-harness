import { createFileRoute } from "@tanstack/react-router";
import { ProductForm } from "@/features/product/components/product-form";
import { useCreateProduct } from "@/features/product/hooks/use-create-product";
import type { ProductFormValues } from "@/features/product/components/product-form";

export const Route = createFileRoute("/products_/new")({
  component: NewProductPage,
});

function NewProductPage() {
  const { createProduct, isCreating } = useCreateProduct();

  function handleSubmit(values: ProductFormValues) {
    createProduct({
      name: values.name,
      description: values.description,
      categoryId: values.categoryId,
      sku: values.sku ?? "",
    });
  }

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-2xl font-bold tracking-tight">商品を作成</h1>
      <ProductForm
        showSkuField
        onSubmit={handleSubmit}
        isSubmitting={isCreating}
        submitLabel="作成"
      />
    </div>
  );
}
