import { createFileRoute } from "@tanstack/react-router";
import { ProductCreateForm } from "@/features/product/components/product-create-form";

interface ProductNewSearch {
  categoryId?: string | undefined;
}

export const Route = createFileRoute("/products_/new")({
  validateSearch: (search: Record<string, unknown>): ProductNewSearch => {
    const categoryId = typeof search.categoryId === "string" ? search.categoryId : undefined;
    if (categoryId === undefined) {
      return {};
    }
    return { categoryId };
  },
  component: ProductNewPage,
});

function ProductNewPage() {
  const { categoryId } = Route.useSearch();

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">商品作成</h1>
      <ProductCreateForm defaultCategoryId={categoryId} />
    </div>
  );
}
