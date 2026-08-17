import { Skeleton } from "@/components/ui/skeleton";

/** 価格詳細のローディング表示。 */
export function PricingDetailSkeleton() {
  return (
    <output className="flex flex-col gap-4">
      <Skeleton className="h-8 w-48" />
      <Skeleton className="h-40 w-full" />
    </output>
  );
}
