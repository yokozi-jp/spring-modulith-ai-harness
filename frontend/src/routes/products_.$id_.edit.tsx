import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { ProductForm } from "@/features/product/components/product-form";
import { useProduct } from "@/features/product/hooks/use-product";
import { useUpdateProduct } from "@/features/product/hooks/use-update-product";
import { ErrorMessage } from "@/components/error-message";

export const Route = createFileRoute("/products_/$id_/edit")({
  component: ProductEditPage,
});

function ProductEditPage() {
  const { id } = Route.useParams();
  const { product, isLoading, error: fetchError } = useProduct(id);
  const { updateProduct, isUpdating, error: updateError } = useUpdateProduct();
  const navigate = useNavigate();

  if (isLoading) {
    return <div className="h-32 animate-pulse rounded-md bg-muted" />;
  }

  if (fetchError) {
    return <ErrorMessage error={fetchError} />;
  }

  if (product === null) {
    return <ErrorMessage error={new Error("商品が見つかりません")} />;
  }

  function handleSubmit(data: {
    name: string;
    description: string;
    categoryId: string;
    sku: string;
  }) {
    if (product === null) {
      return;
    }
    updateProduct(
      {
        id,
        input: {
          name: data.name,
          description: data.description,
          categoryId: data.categoryId,
          version: product.version,
        },
      },
      {
        onSuccess: () => {
          void navigate({ to: "/products/$id", params: { id } });
        },
      },
    );
  }

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold">商品編集</h1>
      <ErrorMessage error={updateError} />
      <ProductForm
        initialValues={{
          name: product.name,
          description: product.description,
          categoryId: product.categoryId,
          sku: product.sku,
        }}
        onSubmit={handleSubmit}
        isSubmitting={isUpdating}
        submitLabel="更新"
        showSku={false}
      />
    </div>
  );
}
