import { Link } from "@tanstack/react-router";
import { cn } from "@/lib/utils";

const MENU_ITEMS = [
  { to: "/", label: "ダッシュボード", icon: "◆" },
  { to: "/categories", label: "カテゴリ", icon: "◇" },
  { to: "/products", label: "商品", icon: "□" },
] as const;

export function Sidebar() {
  return (
    <aside className="hidden w-56 shrink-0 border-r bg-muted/30 md:block">
      <nav className="flex flex-col gap-1 p-3">
        <p className="mb-2 px-3 text-xs font-semibold uppercase tracking-wider text-muted-foreground">
          メニュー
        </p>
        {MENU_ITEMS.map((item) => (
          <Link
            key={item.to}
            to={item.to}
            className={cn(
              "flex items-center gap-2.5 rounded-lg px-3 py-2 text-sm transition-colors",
              "text-muted-foreground hover:bg-accent hover:text-accent-foreground",
            )}
            activeProps={{
              className: "bg-accent text-accent-foreground font-medium shadow-sm",
            }}
          >
            <span className="text-xs">{item.icon}</span>
            {item.label}
          </Link>
        ))}
      </nav>
    </aside>
  );
}
