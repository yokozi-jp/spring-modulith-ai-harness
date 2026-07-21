import { createFileRoute, Link } from "@tanstack/react-router";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { useCategoryList } from "@/features/category/hooks/use-category-list";
import { useProductList } from "@/features/product/hooks/use-product-list";

export const Route = createFileRoute("/")({
  component: DashboardPage,
});

function DashboardPage() {
  const { totalElements: categoryCount, isLoading: isCategoryLoading } = useCategoryList(0, 1);
  const { totalElements: productCount, isLoading: isProductLoading } = useProductList(0, 1);

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">ダッシュボード</h1>
        <p className="text-muted-foreground">Spring Modulith AI Harness の管理画面です。</p>
      </div>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              ルートカテゴリ数
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-3xl font-bold">{isCategoryLoading ? "—" : categoryCount}</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">商品数</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-3xl font-bold">{isProductLoading ? "—" : productCount}</p>
          </CardContent>
        </Card>
      </div>

      <div className="grid gap-4 sm:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="text-base">クイックアクション</CardTitle>
          </CardHeader>
          <CardContent className="flex flex-wrap gap-2">
            <Button asChild size="sm">
              <Link to="/categories/new">カテゴリ作成</Link>
            </Button>
            <Button asChild size="sm" variant="outline">
              <Link to="/products/new">商品作成</Link>
            </Button>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
