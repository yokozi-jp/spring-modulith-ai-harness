import { useState } from "react";
import { Link } from "@tanstack/react-router";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { ErrorMessage } from "@/components/error-message";
import { toError } from "@/lib/utils";
import { useCreateProduct } from "@/features/product/hooks/use-create-product";
import { useCategoryOptions } from "@/features/category/hooks/use-category-options";

interface ProductCreateFormProps {
  readonly defaultCategoryId?: string | undefined;
}

export function ProductCreateForm({ defaultCategoryId }: ProductCreateFormProps) {
  const { createProduct, isCreating, error } = useCreateProduct();
  const { options: categories, isLoading: isLoadingCategories } = useCategoryOptions();
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [categoryId, setCategoryId] = useState(defaultCategoryId ?? "");
  const [sku, setSku] = useState("");

  function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    createProduct({ name, description, categoryId, sku });
  }

  return (
    <Card className="max-w-lg">
      <CardHeader>
        <CardTitle>商品情報</CardTitle>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="space-y-5">
          {error !== null && error !== undefined && <ErrorMessage error={toError(error)} />}

          <div className="space-y-2">
            <Label htmlFor="name">商品名</Label>
            <Input
              id="name"
              value={name}
              onChange={(e) => {
                setName(e.target.value);
              }}
              required
              placeholder="商品名を入力"
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="description">説明</Label>
            <Textarea
              id="description"
              value={description}
              onChange={(e) => {
                setDescription(e.target.value);
              }}
              required
              rows={3}
              placeholder="商品の説明を入力"
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="categoryId">カテゴリ</Label>
            <select
              id="categoryId"
              value={categoryId}
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

          <div className="space-y-2">
            <Label htmlFor="sku">SKU</Label>
            <Input
              id="sku"
              value={sku}
              onChange={(e) => {
                setSku(e.target.value);
              }}
              required
              className="font-mono"
              placeholder="SKU-001"
            />
          </div>

          <div className="flex gap-2 pt-2">
            <Button type="submit" disabled={isCreating}>
              {isCreating ? "作成中..." : "作成"}
            </Button>
            <Button asChild variant="outline">
              {defaultCategoryId !== undefined && defaultCategoryId !== "" ? (
                <Link to="/categories/$id" params={{ id: defaultCategoryId }}>
                  キャンセル
                </Link>
              ) : (
                <Link to="/products">キャンセル</Link>
              )}
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  );
}
