# フロントエンド UI パターン規約

繰り返し登場する UI 状態の表現を決定的にする。

---

## UI コンポーネントの選択

1. **Shadcn/ui に存在するものは Shadcn/ui を使う**（未追加なら先に `vp dlx shadcn@latest add <name>` で追加）
2. **Shadcn/ui にないものは `src/components/` に自作する**
3. **生の HTML タグ + Tailwind でのスタイリングは避ける**

```bash
# 例: Table コンポーネントを追加
vp dlx shadcn@latest add table
```

---

## 共通 UI コンポーネントの作成

**原則: 同じ構造の UI パターンを 2 箇所以上に書く前に、`src/components/` に共通コンポーネントを作成する。**

判断基準:
- JSX 構造が同じ（props の値だけ違う）→ 共通化する
- 構造が異なる → feature 固有で OK

```
src/components/
├── error-message.tsx    # エラー表示
├── empty-state.tsx      # 空状態表示
├── confirm-dialog.tsx   # 削除確認等のダイアログ
├── list-skeleton.tsx    # 一覧ローディング
└── layout/              # レイアウト
```

初回の feature 作成時に必要な共通コンポーネントがなければ作成する。
2 つ目の feature で同じパターンが必要になったら、先に共通化してから使う。

---

## ローディング状態

### Skeleton を使う（Spinner は使わない）

```tsx
// ✅ Skeleton（灰色ブロックで「もうすぐ表示される」感を出す）
function OrderListSkeleton() {
  return (
    <div className="space-y-4">
      {Array.from({ length: 5 }).map((_, i) => (
        <div key={`skeleton-${String(i)}`} className="h-16 animate-pulse rounded-md bg-muted" />
      ))}
    </div>
  );
}

// ❌ Spinner
return <Spinner />;
```

Skeleton はコンポーネントと同じディレクトリに `<name>-skeleton.tsx` で配置する。

---

## エラー状態

### ErrorMessage コンポーネントを使う

```tsx
// src/components/error-message.tsx
interface ErrorMessageProps {
  readonly error: Error | null;
  readonly onRetry?: () => void;
}

export function ErrorMessage({ error, onRetry }: ErrorMessageProps) {
  if (error === null) {
    return null;
  }

  return (
    <div role="alert" className="rounded-md border border-destructive/50 p-4">
      <p className="text-sm text-destructive">{error.message}</p>
      {onRetry !== undefined && (
        <button type="button" onClick={onRetry} className="mt-2 text-sm underline">
          再試行
        </button>
      )}
    </div>
  );
}
```

### 使い方

```tsx
export function OrderList({ orders, isLoading, error, refetch }: OrderListProps) {
  if (error) {
    return <ErrorMessage error={error} onRetry={refetch} />;
  }
  // ...
}
```

---

## 空状態

### EmptyState コンポーネントを使う

```tsx
// src/components/empty-state.tsx
interface EmptyStateProps {
  readonly message: string;
  readonly action?: {
    readonly label: string;
    readonly onClick: () => void;
  };
}

export function EmptyState({ message, action }: EmptyStateProps) {
  return (
    <div className="flex flex-col items-center justify-center py-12 text-center">
      <p className="text-muted-foreground">{message}</p>
      {action !== undefined && (
        <button type="button" onClick={action.onClick} className="mt-4 text-sm underline">
          {action.label}
        </button>
      )}
    </div>
  );
}
```

---

## 一覧表示の定型パターン

```tsx
export function OrderList({ orders, isLoading, error, refetch }: OrderListProps) {
  if (isLoading) {
    return <OrderListSkeleton />;
  }

  if (error) {
    return <ErrorMessage error={error} onRetry={refetch} />;
  }

  if (orders.length === 0) {
    return <EmptyState message="注文がありません" />;
  }

  return (
    <ul className="space-y-2">
      {orders.map((order) => (
        <li key={order.id}>
          <OrderCard order={order} />
        </li>
      ))}
    </ul>
  );
}
```

順序は常に: **Loading → Error → Empty → Content**

---

## フォーム

### HTML ネイティブ + Shadcn/ui を使う（フォームライブラリは使わない）

```tsx
interface OrderFormProps {
  readonly onSubmit: (data: CreateOrderInput) => void;
  readonly isSubmitting: boolean;
}

export function OrderForm({ onSubmit, isSubmitting }: OrderFormProps) {
  const [name, setName] = useState("");

  function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    onSubmit({ name });
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div>
        <label htmlFor="name" className="text-sm font-medium">
          名前
        </label>
        <input
          id="name"
          type="text"
          value={name}
          onChange={(e) => { setName(e.target.value); }}
          required
          className="mt-1 w-full rounded-md border px-3 py-2"
        />
      </div>
      <button type="submit" disabled={isSubmitting}>
        {isSubmitting ? "送信中..." : "作成"}
      </button>
    </form>
  );
}
```

### フォームのルール

- `<form>` タグを使う（`onSubmit` で送信処理）
- `event.preventDefault()` は `handleSubmit` 内で呼ぶ
- ボタンには `type="submit"` を付ける
- **enum / FK（外部キー）フィールドは `<select>` で選択させる**（手入力させない）
- 送信中は `disabled` で二重送信防止
- バリデーションは HTML 属性（`required`, `pattern`, `min` 等）を優先
- 複雑なバリデーションが必要になったら `zod` を導入する（先に入れない）

---

## モーダル / ダイアログ

### Shadcn/ui の Dialog を使う

```tsx
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";

interface ConfirmDialogProps {
  readonly isOpen: boolean;
  readonly onClose: () => void;
  readonly onConfirm: () => void;
  readonly title: string;
  readonly message: string;
}

export function ConfirmDialog({ isOpen, onClose, onConfirm, title, message }: ConfirmDialogProps) {
  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
        </DialogHeader>
        <p className="text-sm text-muted-foreground">{message}</p>
        <div className="flex justify-end gap-2">
          <button type="button" onClick={onClose}>キャンセル</button>
          <button type="button" onClick={onConfirm}>確認</button>
        </div>
      </DialogContent>
    </Dialog>
  );
}
```

### モーダルの状態管理

```tsx
// 親コンポーネントで開閉を管理
const [isDialogOpen, setIsDialogOpen] = useState(false);

function handleDelete() {
  setIsDialogOpen(true);
}

function handleConfirmDelete() {
  deleteOrder(id);
  setIsDialogOpen(false);
}
```

---

## 共通コンポーネントの配置

| コンポーネント | 配置先 |
|---------------|--------|
| ErrorMessage | `src/components/error-message.tsx` |
| EmptyState | `src/components/empty-state.tsx` |
| Skeleton（機能固有） | `src/features/<feature>/components/<name>-skeleton.tsx` |
| Dialog（機能固有） | `src/features/<feature>/components/<name>-dialog.tsx` |
| Shadcn/ui | `src/components/ui/`（編集禁止） |
