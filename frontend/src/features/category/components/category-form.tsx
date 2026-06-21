import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

interface CategoryFormProps {
  readonly initialName?: string;
  readonly initialSortOrder?: number;
  readonly onSubmit: (data: { name: string; sortOrder: number }) => void;
  readonly isSubmitting: boolean;
  readonly submitLabel: string;
  readonly error: Error | null;
}

export function CategoryForm({
  initialName = "",
  initialSortOrder = 0,
  onSubmit,
  isSubmitting,
  submitLabel,
  error,
}: CategoryFormProps) {
  const [name, setName] = useState(initialName);
  const [sortOrder, setSortOrder] = useState(String(initialSortOrder));

  function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    onSubmit({ name, sortOrder: Number(sortOrder) });
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      {error !== null && (
        <div role="alert" className="rounded-md border border-destructive/50 p-4">
          <p className="text-sm text-destructive">{error.message}</p>
        </div>
      )}

      <div className="space-y-2">
        <Label htmlFor="name">カテゴリ名</Label>
        <Input
          id="name"
          type="text"
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
        <Label htmlFor="sortOrder">表示順</Label>
        <Input
          id="sortOrder"
          type="number"
          value={sortOrder}
          onChange={(e) => {
            setSortOrder(e.target.value);
          }}
          required
          min={0}
        />
      </div>

      <Button type="submit" disabled={isSubmitting}>
        {isSubmitting ? "送信中..." : submitLabel}
      </Button>
    </form>
  );
}
