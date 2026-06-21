export function Header() {
  return (
    <header className="flex h-14 items-center justify-between border-b px-6">
      <div className="font-semibold">Spring Modulith AI Harness</div>
      <div className="flex items-center gap-4">
        <span className="text-sm text-muted-foreground">ユーザー</span>
      </div>
    </header>
  );
}
