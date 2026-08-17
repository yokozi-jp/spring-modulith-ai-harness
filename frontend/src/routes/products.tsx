import { Link, createFileRoute } from "@tanstack/react-router";
import { Button } from "@/components/ui/button";
import { ProductListContent } from "@/features/product/components/product-list-content";

export const Route = createFileRoute("/products")({
  component: ProductsPage,
});

function ProductsPage() {
  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold tracking-tight">商品管理</h1>
        <Button asChild>
          <Link to="/products/new">新規作成</Link>
        </Button>
      </div>
      <ProductListContent />
    </div>
  );
}
