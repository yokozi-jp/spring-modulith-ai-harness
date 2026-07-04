import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";
import { STATUS_LABELS, STATUS_COLORS } from "@/features/product/types/product-status";
import type { ProductStatus } from "@/features/product/types/product-status";

interface StatusBadgeProps {
  readonly status: string;
}

export function StatusBadge({ status }: StatusBadgeProps) {
  const label = STATUS_LABELS[status as ProductStatus] ?? status;
  const color = STATUS_COLORS[status as ProductStatus] ?? "bg-muted text-muted-foreground";

  return (
    <Badge variant="secondary" className={cn(color)}>
      {label}
    </Badge>
  );
}
