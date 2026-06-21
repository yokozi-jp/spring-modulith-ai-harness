import { Link } from "@tanstack/react-router";

const MENU_ITEMS = [
  { to: "/", label: "ホーム" },
  { to: "/categories", label: "カテゴリ管理" },
] as const;

export function Sidebar() {
  return (
    <aside className="w-56 border-r bg-muted/40 p-4">
      <nav className="space-y-1">
        {MENU_ITEMS.map((item) => (
          <Link
            key={item.to}
            to={item.to}
            className="block rounded-md px-3 py-2 text-sm hover:bg-muted"
            activeProps={{ className: "bg-muted font-medium" }}
          >
            {item.label}
          </Link>
        ))}
      </nav>
    </aside>
  );
}
