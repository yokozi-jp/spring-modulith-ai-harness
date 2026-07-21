import { Link } from "@tanstack/react-router";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { useProductList } from "@/features/product/hooks/use-product-list";
import { StatusBadge } from "@/features/product/components/status-badge";

interface CategoryProductListProps {
  readonly categoryId: string;
}

export function CategoryProductList({ categoryId }: CategoryProductListProps) {
  const { products, isLoading } = useProductList(0, 50, categoryId);

  return (
    <div className="space-y-3">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <h2 className="text-lg font-semibold">商品</h2>
          {!isLoading && (
            <Badge variant="secondary" className="text-xs">
              {products.length}
            </Badge>
          )}
        </div>
        <Button asChild size="sm">
          <Link to="/products/new" search={{ categoryId }}>
            商品を追加
          </Link>
        </Button>
      </div>

      {isLoading && (
        <div className="space-y-2">
          <div className="h-8 w-48 animate-pulse rounded bg-muted" />
          <div className="h-8 w-36 animate-pulse rounded bg-muted" />
        </div>
      )}

      {!isLoading && products.length === 0 && (
        <p className="text-sm text-muted-foreground">このカテゴリに商品はありません</p>
      )}

      {!isLoading && products.length > 0 && (
        <ul className="space-y-1">
          {products.map((product) => (
            <li key={product.id}>
              <Link
                to="/products/$id"
                params={{ id: product.id ?? "" }}
                className="flex items-center justify-between rounded-lg px-3 py-2 text-sm transition-colors hover:bg-accent/50"
              >
                <span className="font-medium">{product.name}</span>
                <StatusBadge status={product.status ?? "DRAFT"} />
              </Link>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
