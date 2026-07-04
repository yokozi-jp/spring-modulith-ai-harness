export function ProductListSkeleton() {
  return (
    <div className="space-y-1 rounded-xl border p-4">
      {Array.from({ length: 5 }).map((_, i) => (
        <div key={`skeleton-${String(i)}`} className="h-10 animate-pulse rounded-lg bg-muted" />
      ))}
    </div>
  );
}
