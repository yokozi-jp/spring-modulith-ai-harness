import { useState } from "react";
import { Link } from "@tanstack/react-router";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { ErrorMessage } from "@/components/error-message";
import { toError } from "@/lib/utils";
import { useProduct } from "@/features/product/hooks/use-product";
import { useUpdateProduct } from "@/features/product/hooks/use-update-product";
import { useCategoryOptions } from "@/features/category/hooks/use-category-options";
import { PRODUCT_STATUS } from "@/features/product/types/product-status";

interface ProductEditFormProps {
  readonly id: string;
}

export function ProductEditForm({ id }: ProductEditFormProps) {
  const { product, isLoading, error: fetchError } = useProduct(id);
  const { updateProduct, isUpdating, error: updateError } = useUpdateProduct(id);
  const { options: categories, isLoading: isLoadingCategories } = useCategoryOptions();
  const [name, setName] = useState<string | null>(null);
  const [description, setDescription] = useState<string | null>(null);
  const [categoryId, setCategoryId] = useState<string | null>(null);

  if (isLoading) {
    return (
      <Card className="max-w-lg">
        <CardContent className="space-y-4 pt-6">
          <div className="h-10 animate-pulse rounded bg-muted" />
          <div className="h-20 animate-pulse rounded bg-muted" />
          <div className="h-10 animate-pulse rounded bg-muted" />
        </CardContent>
      </Card>
    );
  }

  if (fetchError) {
    return <ErrorMessage error={toError(fetchError)} />;
  }

  if (product === null) {
    return <ErrorMessage error={new Error("商品が見つかりません")} />;
  }

  if (product.status === PRODUCT_STATUS.ARCHIVED) {
    return <ErrorMessage error={new Error("アーカイブ済みの商品は編集できません")} />;
  }

  const currentName = name ?? product.name;
  const currentDescription = description ?? product.description;
  const currentCategoryId = categoryId ?? product.categoryId;

  function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (product === null) {
      return;
    }
    updateProduct({
      name: currentName,
      description: currentDescription,
      categoryId: currentCategoryId,
      version: product.version,
    });
  }

  return (
    <Card className="max-w-lg">
      <CardHeader>
        <CardTitle>商品情報を編集</CardTitle>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="space-y-5">
          {updateError !== null && updateError !== undefined && (
            <ErrorMessage error={toError(updateError)} />
          )}

          <div className="space-y-2">
            <Label htmlFor="name">商品名</Label>
            <Input
              id="name"
              value={currentName}
              onChange={(e) => {
                setName(e.target.value);
              }}
              required
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="description">説明</Label>
            <Textarea
              id="description"
              value={currentDescription}
              onChange={(e) => {
                setDescription(e.target.value);
              }}
              required
              rows={3}
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="categoryId">カテゴリ</Label>
            <select
              id="categoryId"
              value={currentCategoryId}
              onChange={(e) => {
                setCategoryId(e.target.value);
              }}
              required
              disabled={isLoadingCategories}
              className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-xs transition-colors placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
            >
              <option value="">選択してください</option>
              {categories.map((cat) => (
                <option key={cat.id} value={cat.id}>
                  {"　".repeat(cat.depth)}
                  {cat.name}
                </option>
              ))}
            </select>
          </div>

          <div className="flex gap-2 pt-2">
            <Button type="submit" disabled={isUpdating}>
              {isUpdating ? "更新中..." : "更新"}
            </Button>
            <Button asChild variant="outline">
              <Link to="/products/$id" params={{ id }}>
                キャンセル
              </Link>
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  );
}
