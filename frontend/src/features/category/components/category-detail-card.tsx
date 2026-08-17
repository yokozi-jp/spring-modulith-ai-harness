import { Link } from "@tanstack/react-router";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import type { CategoryDetailResponse } from "@/api/openAPIDefinition.schemas";

interface CategoryDetailCardProps {
  readonly category: CategoryDetailResponse;
}

/** カテゴリ詳細の表示本体。祖先パス（breadcrumb）と基本情報を表示する。 */
export function CategoryDetailCard({ category }: CategoryDetailCardProps) {
  const ancestors = category.ancestors ?? [];

  return (
    <Card>
      <CardHeader>
        {ancestors.length > 0 && (
          <nav
            aria-label="カテゴリの階層パス"
            className="flex flex-wrap items-center gap-1 text-sm text-muted-foreground"
          >
            {ancestors.map((ancestor) => (
              <span key={ancestor.id} className="flex items-center gap-1">
                <Link
                  to="/categories/$id"
                  params={{ id: ancestor.id ?? "" }}
                  className="hover:underline"
                >
                  {ancestor.name}
                </Link>
                <span aria-hidden="true">/</span>
              </span>
            ))}
          </nav>
        )}
        <CardTitle className="text-xl">{category.name}</CardTitle>
      </CardHeader>
      <CardContent className="flex flex-col gap-2 text-sm">
        <div className="flex justify-between">
          <span className="text-muted-foreground">並び順</span>
          <span>{category.sortOrder}</span>
        </div>
      </CardContent>
    </Card>
  );
}
