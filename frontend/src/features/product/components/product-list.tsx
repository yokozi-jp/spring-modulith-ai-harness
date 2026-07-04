import { useState } from "react";
import { Link } from "@tanstack/react-router";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { ErrorMessage } from "@/components/error-message";
import { EmptyState } from "@/components/empty-state";
import { toError } from "@/lib/utils";
import { useProductList } from "@/features/product/hooks/use-product-list";
import { ProductListSkeleton } from "@/features/product/components/product-list-skeleton";
import {
  PRODUCT_STATUS,
  STATUS_LABELS,
  STATUS_COLORS,
} from "@/features/product/types/product-status";
import type { ProductStatus } from "@/features/product/types/product-status";

const FILTER_OPTIONS = [
  { value: "", label: "すべて" },
  { value: PRODUCT_STATUS.DRAFT, label: STATUS_LABELS.DRAFT },
  { value: PRODUCT_STATUS.PUBLISHED, label: STATUS_LABELS.PUBLISHED },
  { value: PRODUCT_STATUS.ARCHIVED, label: STATUS_LABELS.ARCHIVED },
] as const;

export function ProductList() {
  const [statusFilter, setStatusFilter] = useState("");
  const { products, isLoading, error } = useProductList(
    0,
    20,
    undefined,
    statusFilter !== "" ? statusFilter : undefined,
  );

  return (
    <div className="space-y-4">
      <div className="flex gap-2">
        {FILTER_OPTIONS.map((opt) => (
          <button
            key={opt.value}
            type="button"
            onClick={() => {
              setStatusFilter(opt.value);
            }}
            className={`rounded-full px-3 py-1 text-xs font-medium transition-colors ${
              statusFilter === opt.value
                ? "bg-primary text-primary-foreground"
                : "bg-muted text-muted-foreground hover:bg-muted/80"
            }`}
          >
            {opt.label}
          </button>
        ))}
      </div>

      {isLoading && <ProductListSkeleton />}

      {!isLoading && error !== null && <ErrorMessage error={toError(error)} />}

      {!isLoading && error === null && products.length === 0 && (
        <EmptyState message="商品がありません" />
      )}

      {!isLoading && error === null && products.length > 0 && (
        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-base">商品リスト</CardTitle>
          </CardHeader>
          <CardContent className="pb-4">
            <ul className="space-y-0.5">
              {products.map((product) => (
                <li key={product.id}>
                  <Link
                    to="/products/$id"
                    params={{ id: product.id ?? "" }}
                    className="group flex items-center justify-between rounded-lg px-3 py-2 transition-colors hover:bg-accent/50"
                  >
                    <span className="text-sm font-medium group-hover:text-primary">
                      {product.name}
                    </span>
                    <Badge
                      variant="secondary"
                      className={STATUS_COLORS[product.status as ProductStatus] ?? ""}
                    >
                      {STATUS_LABELS[product.status as ProductStatus] ?? product.status}
                    </Badge>
                  </Link>
                </li>
              ))}
            </ul>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
