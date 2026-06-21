import { useState } from "react";
import { Link } from "@tanstack/react-router";
import { Button } from "@/components/ui/button";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { useCategoryList } from "@/features/category/hooks/use-category-list";

export function CategoryListPage() {
  const [page, setPage] = useState(0);
  const { categories, totalPages, isLoading, error } = useCategoryList(page);

  if (isLoading) {
    return (
      <div className="space-y-4">
        {Array.from({ length: 5 }).map((_, i) => (
          <div key={`skeleton-${String(i)}`} className="h-12 animate-pulse rounded-md bg-muted" />
        ))}
      </div>
    );
  }

  if (error) {
    return (
      <div role="alert" className="rounded-md border border-destructive/50 p-4">
        <p className="text-sm text-destructive">{error.message}</p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">カテゴリ管理</h1>
        <Button asChild>
          <Link to="/categories/new">新規作成</Link>
        </Button>
      </div>

      {categories.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-12 text-center">
          <p className="text-muted-foreground">カテゴリがありません</p>
        </div>
      ) : (
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>カテゴリ名</TableHead>
              <TableHead className="w-32">表示順</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {categories.map((category) => (
              <TableRow key={category.id}>
                <TableCell>
                  <Link
                    to="/categories/$id"
                    params={{ id: category.id }}
                    className="text-primary underline-offset-4 hover:underline"
                  >
                    {category.name}
                  </Link>
                </TableCell>
                <TableCell>{category.sortOrder}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}

      {totalPages > 1 && (
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
            {page + 1} / {totalPages}
          </span>
          <Button
            variant="outline"
            size="sm"
            disabled={page >= totalPages - 1}
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
