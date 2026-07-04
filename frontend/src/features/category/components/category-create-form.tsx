import { useState } from "react";
import { Link } from "@tanstack/react-router";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { ErrorMessage } from "@/components/error-message";
import { toError } from "@/lib/utils";
import { useCreateCategory } from "@/features/category/hooks/use-create-category";
import { useCategoryOptions } from "@/features/category/hooks/use-category-options";

interface CategoryCreateFormProps {
  readonly defaultParentCategoryId?: string | undefined;
}

export function CategoryCreateForm({ defaultParentCategoryId }: CategoryCreateFormProps) {
  const { createCategory, isCreating, error } = useCreateCategory();
  const { options: categories, isLoading: isLoadingCategories } = useCategoryOptions();
  const [name, setName] = useState("");
  const [sortOrder, setSortOrder] = useState(0);
  const [parentCategoryId, setParentCategoryId] = useState(defaultParentCategoryId ?? "");

  function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    createCategory({
      name,
      sortOrder,
      ...(parentCategoryId !== "" && { parentCategoryId }),
    });
  }

  return (
    <Card className="max-w-md">
      <CardHeader>
        <CardTitle>カテゴリ情報</CardTitle>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="space-y-5">
          {error !== null && error !== undefined && <ErrorMessage error={toError(error)} />}

          <div className="space-y-2">
            <Label htmlFor="name">カテゴリ名</Label>
            <Input
              id="name"
              value={name}
              onChange={(e) => {
                setName(e.target.value);
              }}
              required
              maxLength={50}
              placeholder="カテゴリ名を入力"
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="parentCategoryId">親カテゴリ（任意）</Label>
            <select
              id="parentCategoryId"
              value={parentCategoryId}
              onChange={(e) => {
                setParentCategoryId(e.target.value);
              }}
              disabled={isLoadingCategories}
              className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-xs transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
            >
              <option value="">ルート（親なし）</option>
              {categories.map((cat) => (
                <option key={cat.id} value={cat.id}>
                  {"　".repeat(cat.depth)}
                  {cat.name}
                </option>
              ))}
            </select>
          </div>

          <div className="space-y-2">
            <Label htmlFor="sortOrder">並び順</Label>
            <Input
              id="sortOrder"
              type="number"
              value={sortOrder}
              onChange={(e) => {
                setSortOrder(Number(e.target.value));
              }}
              required
              min={0}
            />
          </div>

          <div className="flex gap-2 pt-2">
            <Button type="submit" disabled={isCreating}>
              {isCreating ? "作成中..." : "作成"}
            </Button>
            <Button asChild variant="outline">
              {parentCategoryId !== "" ? (
                <Link to="/categories/$id" params={{ id: parentCategoryId }}>
                  キャンセル
                </Link>
              ) : (
                <Link to="/categories">キャンセル</Link>
              )}
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  );
}
