import { Link } from "@tanstack/react-router";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

const QUICK_LINKS = [
  { to: "/products/new", label: "商品を作成" },
  { to: "/categories/new", label: "カテゴリを作成" },
  { to: "/pricings/new", label: "価格を作成" },
] as const;

/** ダッシュボードのクイックリンク（各リソースの新規作成）。 */
export function DashboardQuickLinks() {
  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-lg">クイックリンク</CardTitle>
      </CardHeader>
      <CardContent className="flex gap-2">
        {QUICK_LINKS.map((link) => (
          <Button key={link.to} asChild variant="outline">
            <Link to={link.to}>{link.label}</Link>
          </Button>
        ))}
      </CardContent>
    </Card>
  );
}
