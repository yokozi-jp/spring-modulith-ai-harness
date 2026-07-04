import { createFileRoute } from "@tanstack/react-router";
import { CategoryCreateForm } from "@/features/category/components/category-create-form";

interface CategoryNewSearch {
  parentCategoryId?: string | undefined;
}

export const Route = createFileRoute("/categories_/new")({
  validateSearch: (search: Record<string, unknown>): CategoryNewSearch => {
    const parentCategoryId =
      typeof search.parentCategoryId === "string" ? search.parentCategoryId : undefined;
    if (parentCategoryId === undefined) {
      return {};
    }
    return { parentCategoryId };
  },
  component: CategoryNewPage,
});

function CategoryNewPage() {
  const { parentCategoryId } = Route.useSearch();

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">カテゴリ作成</h1>
      <CategoryCreateForm defaultParentCategoryId={parentCategoryId} />
    </div>
  );
}
