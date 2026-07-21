export function CategoryListSkeleton() {
  return (
    <div className="space-y-3">
      {Array.from({ length: 5 }).map((_, i) => (
        <div key={`skeleton-${String(i)}`} className="h-12 animate-pulse rounded-md bg-muted" />
      ))}
    </div>
  );
}
