/** 価格一覧のローディング表示。 */
export function PricingListSkeleton() {
  return (
    <output className="flex flex-col gap-2">
      {Array.from({ length: 5 }).map((_, i) => (
        <div key={`skeleton-${String(i)}`} className="h-12 animate-pulse rounded-md bg-muted" />
      ))}
    </output>
  );
}
