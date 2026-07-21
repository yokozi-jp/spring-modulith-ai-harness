import { Separator } from "@/components/ui/separator";

export function Header() {
  return (
    <header className="sticky top-0 z-50 border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="flex h-14 items-center justify-between px-6">
        <div className="flex items-center gap-2">
          <div className="flex size-7 items-center justify-center rounded-md bg-primary text-primary-foreground">
            <span className="text-sm font-bold">S</span>
          </div>
          <Separator orientation="vertical" className="mx-1 h-4" />
          <span className="text-sm font-semibold tracking-tight">Spring Modulith</span>
        </div>
        <div className="flex items-center gap-3">
          <div className="flex size-7 items-center justify-center rounded-full bg-muted text-xs font-medium">
            A
          </div>
          <span className="text-sm text-muted-foreground">管理者</span>
        </div>
      </div>
    </header>
  );
}
