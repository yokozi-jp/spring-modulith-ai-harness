import { useState } from "react";
import { Link } from "@tanstack/react-router";
import { Button } from "@/components/ui/button";
import { ErrorMessage } from "@/components/error-message";
import { EmptyState } from "@/components/empty-state";
import { useProductList } from "@/features/product/hooks/use-product-list";

const STATUS_LABELS: Record<string, string> = {
  DRAFT: "下書き",
  PUBLISHED: "公開中",
  ARCHIVED: "アーカイブ",
};

export function ProductList() {
  const [page, setPage] = useState(0);
  const { products, isLoading, error } = useProductList(page, 20);

  if (isLoading) {
    return <ProductListSkeleton />;
  }

  if (error) {
    return <ErrorMessage error={error} />;
  }

  if (products === null || products.content.length === 0) {
    return <EmptyState message="商品がありません" />;
  }

  return (
    <div className="space-y-4">
      <table className="w-full border-collapse">
        <thead>
          <tr className="border-b text-left text-sm text-muted-foreground">
            <th className="px-4 py-2">商品名</th>
            <th className="px-4 py-2">ステータス</th>
            <th className="px-4 py-2">操作</th>
          </tr>
        </thead>
        <tbody>
          {products.content.map((product) => (
            <tr key={product.id} className="border-b hover:bg-muted/50">
              <td className="px-4 py-2">{product.name}</td>
              <td className="px-4 py-2">{STATUS_LABELS[product.status] ?? product.status}</td>
              <td className="px-4 py-2">
                <Button variant="ghost" size="sm" asChild>
                  <Link to="/products/$id" params={{ id: product.id }}>
                    詳細
                  </Link>
                </Button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {products.totalPages > 1 && (
        <div className="flex items-center justify-center gap-2">
          <Button
            variant="outline"
            size="sm"
            disabled={page === 0}
            onClick={() => {
              setPage((p) => p - 1);
            }}
          >
            前へ
          </Button>
          <span className="text-sm text-muted-foreground">
            {page + 1} / {products.totalPages}
          </span>
          <Button
            variant="outline"
            size="sm"
            disabled={page >= products.totalPages - 1}
            onClick={() => {
              setPage((p) => p + 1);
            }}
          >
            次へ
          </Button>
        </div>
      )}
    </div>
  );
}

function ProductListSkeleton() {
  return (
    <div className="space-y-4">
      {Array.from({ length: 5 }).map((_, i) => (
        <div key={`skeleton-${String(i)}`} className="h-12 animate-pulse rounded-md bg-muted" />
      ))}
    </div>
  );
}
