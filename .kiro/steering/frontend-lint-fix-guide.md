# Frontend Lint エラー修正ガイド

Lint エラーが発生したとき、ルール名からこのガイドを参照して修正する。

---

## oxlint ルール別修正方法

### typescript/no-explicit-any

**エラー**: `any` 型を使用している。

**修正**: 具体的な型に置き換える。不明な場合は `unknown` を使い、型ガードで絞り込む。

```typescript
// ❌
function parse(data: any) { ... }

// ✅
function parse(data: unknown) {
  if (typeof data === "string") { ... }
}
```

---

### typescript/no-non-null-assertion

**エラー**: `!` による非 null アサーションを使用している。

**修正**: null チェックまたは optional chaining で安全に扱う。

```typescript
// ❌
const value = map.get(key)!;

// ✅
const value = map.get(key);
if (value === undefined) throw new Error(`Key not found: ${key}`);
```

---

### typescript/consistent-type-imports

**エラー**: 型のみの import に `import type` を使っていない。

**修正**: 型として使う import は `import type` に変更する。

```typescript
// ❌
import { User } from "@/types/user";

// ✅
import type { User } from "@/types/user";
```

---

### typescript/no-restricted-types (React.FC)

**エラー**: `React.FC` / `React.FunctionComponent` / `FC` は使用禁止です。

**修正**: 通常の関数宣言を使う。Props は引数で型注釈する。

```typescript
// ❌
const OrderList: React.FC<OrderListProps> = ({ orders }) => { ... }

// ✅
function OrderList({ orders }: OrderListProps) { ... }
```

**理由**: `React.FC` は暗黙の `children` を含み、型が不正確になる。関数宣言の方がシンプルで明確。

---

### project-rules/no-arrow-function-component

**エラー**: コンポーネントはアロー関数ではなく関数宣言で定義してください。

**修正**: `export const X = () =>` を `export function X()` に変更する。

```typescript
// ❌
export const OrderList = () => {
  return <div />;
};

// ✅
export function OrderList() {
  return <div />;
}
```

**理由**: React 公式推奨、DevTools での表示名が明確、一貫性。

---

### project-rules/no-props-object-param

**エラー**: Props は分割代入で受け取ってください。

**修正**: `(props: XProps)` を `({ a, b }: XProps)` に変更する。

```typescript
// ❌
export function OrderCard(props: OrderCardProps) {
  return <div>{props.name}</div>;
}

// ✅
export function OrderCard({ name }: OrderCardProps) {
  return <div>{name}</div>;
}
```

**理由**: `props.xxx` は冗長、使用している Props が明確になる。

---

### react/no-array-index-key

**エラー**: 配列の index を key に使用している。

**修正**: 一意な ID フィールドを key に使う。

```typescript
// ❌
items.map((item, index) => <li key={index}>{item.name}</li>)

// ✅
items.map((item) => <li key={item.id}>{item.name}</li>)
```

---

### react/no-danger

**エラー**: `dangerouslySetInnerHTML` を使用している。

**修正**: サニタイズ済み HTML を使うか、React コンポーネントで構造化する。

---

### unicorn/filename-case

**エラー**: ファイル名が kebab-case でない。

**修正**: ファイル名を kebab-case に変更する。

```
// ❌ UserList.tsx, userList.tsx
// ✅ user-list.tsx
```

---

### import/no-default-export

**エラー**: default export を使用している。

**修正**: named export に変更する。ルートファイル (`src/routes/`) と設定ファイルは例外。

```typescript
// ❌
export default function UserList() { ... }

// ✅
export function UserList() { ... }
```

---

### import/no-cycle

**エラー**: 循環 import が検出された。

**修正**: 依存関係を整理する。共通の型・ユーティリティを `lib/` や `types/` に切り出す。

---

### no-restricted-imports (親ディレクトリ相対パス)

**エラー**: `../` による親ディレクトリへの相対パスを使用している。

**修正**: `@/` エイリアスに置き換える。

```typescript
// ❌
import { cn } from "../../lib/utils";

// ✅
import { cn } from "@/lib/utils";
```

---

### no-restricted-imports (barrel export)

**エラー**: `index.ts` からの re-export を import している。

**修正**: モジュールを直接 import する。

```typescript
// ❌
import { Button } from "@/components/ui/index";

// ✅
import { Button } from "@/components/ui/button";
```

---

### no-restricted-imports (routes から @/api/*)

**エラー**: ルートファイルから `@/api/*` を直接 import している。

**修正**: `features/` 内の Hook 経由で API を呼び出す。

```typescript
// ❌ (src/routes/orders.tsx)
import { useGetOrders } from "@/api/order";

// ✅ (src/routes/orders.tsx)
import { useOrderList } from "@/features/order/hooks/use-order-list";
```

---

## カスタムチェック修正方法

### Hook 配置ルール違反

**エラー**: `Hookの定義は use-*.ts ファイルで行ってください。`

**修正**: Hook 関数を `use-<name>.ts` ファイルに移動する。

```
// ❌ src/features/order/components/order-list.tsx 内で export function useOrderList() {}

// ✅ src/features/order/hooks/use-order-list.ts に分離
```

---

**エラー**: `Hookファイルは src/hooks/ または src/components/features/ 内に配置してください。`

**修正**: Hook ファイルを正しい場所に移動する。
- 機能固有 Hook → `src/features/<feature>/hooks/use-<name>.ts`
- 汎用 Hook → `src/hooks/use-<name>.ts`

---

### components/ui/ 編集チェック

**エラー**: `components/ui/ 内のファイルが変更されています。`

**修正**: Shadcn/ui コンポーネントは直接編集しない。カスタマイズが必要な場合:
1. `src/components/` 直下にラッパーコンポーネントを作成する
2. または `src/features/<feature>/components/` に機能固有のカスタムコンポーネントを作る

---

## TypeScript コンパイラエラー

### noUncheckedIndexedAccess

**エラー**: 配列やオブジェクトのインデックスアクセスの結果が `T | undefined` になる。

**修正**: undefined チェックを追加する。

```typescript
// ❌
const first = items[0];
console.log(first.name); // items[0] は T | undefined

// ✅
const first = items[0];
if (first === undefined) throw new Error("empty");
console.log(first.name);
```

---

### exactOptionalPropertyTypes

**エラー**: optional プロパティに `undefined` を明示的に代入している。

**修正**: プロパティ自体を省略するか、型定義に `| undefined` を追加する。

```typescript
// ❌
interface Config { timeout?: number; }
const config: Config = { timeout: undefined };

// ✅
const config: Config = {};
```

#### Orval 生成型での対処

Orval 生成の Request 型（例: `CreateCategoryRequest`, `UpdateCategoryRequest`）は optional プロパティが `prop?: T` で定義されており、`undefined` を明示的に代入すると `exactOptionalPropertyTypes` エラーになる。

```typescript
// ❌ Orval 生成型にそのまま代入するとエラー
createCategory({
  data: {
    name: data.name,
    sortOrder: data.sortOrder,
    parentCategoryId: data.parentId,  // string | undefined → エラー
  },
});

// ✅ undefined のときはプロパティ自体を省略
createCategory({
  data: {
    name: data.name,
    sortOrder: data.sortOrder,
    ...(data.parentId !== undefined && { parentCategoryId: data.parentId }),
  },
});

// ✅ または条件分岐でオブジェクトを構築
const requestData: CreateCategoryRequest = {
  name: data.name,
  sortOrder: data.sortOrder,
};
if (data.parentId !== undefined) {
  requestData.parentCategoryId = data.parentId;
}
createCategory({ data: requestData });
```
