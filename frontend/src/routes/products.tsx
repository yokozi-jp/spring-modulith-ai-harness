import { createFileRoute, Link } from "@tanstack/react-router";
import { Button } from "@/components/ui/button";
import { ProductList } from "@/features/product/components/product-list";

export const Route = createFileRoute("/products")({
  component: ProductsPage,
});

function ProductsPage() {
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">商品管理</h1>
        <Button asChild>
          <Link to="/products/new">新規作成</Link>
        </Button>
      </div>
      <ProductList />
    </div>
  );
}
