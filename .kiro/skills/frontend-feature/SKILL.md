---
name: frontend-feature
description: Scaffold a new frontend feature with components, hooks, types, and tests following project conventions. Use when user wants to create a new feature, add a frontend module, scaffold frontend code, or mentions "new feature" in the frontend context.
---

# Frontend Feature Scaffold

Generate a complete feature structure under `frontend/src/features/<name>/` following project conventions.

## Generated Structure

```
frontend/src/features/<feature-name>/
├── components/
│   └── <feature-name>.tsx       # Presentational component
├── hooks/
│   └── use-<feature-name>.ts   # Logic hook (data fetching, state)
└── types/
    └── <feature-name>.ts       # Feature-specific types (if needed)
```

Plus test file:
```
frontend/src/features/<feature-name>/hooks/use-<feature-name>.test.ts
```

## Workflow

1. Ask user for:
   - Feature name (kebab-case)
   - Brief description of what it does
   - Does it fetch data from an API? (→ import from `@/api/`)
   - Does it need feature-specific types?

2. Generate files following these rules:
   - All file names in **kebab-case**
   - Component uses **named export** (no default export)
   - Hook file named `use-<name>.ts`, exports `function use<PascalName>()`
   - Component imports hook via `./` relative path
   - External imports use `@/` alias
   - Types use `import type`
   - No `any` — use `unknown` with type guards if needed
   - No `console.log`

3. Run verification:
   ```bash
   cd frontend && ./scripts/verify.sh
   ```

## Component Template

```tsx
import { use<PascalName> } from "@/features/<name>/hooks/use-<name>";

export function <PascalName>() {
  const { data, isLoading } = use<PascalName>();

  if (isLoading) {
    return <div>Loading...</div>;
  }

  return (
    <section>
      {/* TODO: implement UI */}
    </section>
  );
}
```

## Hook Template

```typescript
import { useQuery } from "@tanstack/react-query";

export function use<PascalName>() {
  // TODO: implement data fetching or state logic
  return useQuery({
    queryKey: ["<name>"],
    queryFn: async () => {
      // Replace with actual API call from @/api/
      return [];
    },
  });
}
```

## Test Template

```typescript
import { describe, expect, it } from "vite-plus/test";

describe("use<PascalName>", () => {
  it("should return initial state", () => {
    // TODO: implement test
    expect(true).toBe(true);
  });
});
```

## Rules

プロジェクト規約に従うこと。詳細は `.kiro/steering/frontend-rules.md` を参照。

生成後は必ず `./scripts/verify.sh` で規約準拠を確認する。

## Route File Generation

Feature 作成時に対応するルートファイルも生成する。

生成先: `frontend/src/routes/<name>.tsx` (または `<name>/index.tsx` でネスト)

### Route Template

```tsx
import { createFileRoute } from "@tanstack/react-router";
import { <PascalName> } from "@/features/<name>/components/<name>";

export const Route = createFileRoute("/<name>")({
  component: <PascalName>Page,
});

function <PascalName>Page() {
  return <<PascalName> />;
}
```

### ルール
- Route ファイルは**薄く保つ** — feature コンポーネントを呼ぶだけ
- `@/api/*` を直接 import しない（oxlint で阻止される）
- default export は TanStack Router 規約で許可（oxlint override 済み）
- パス付きルート（`/orders/$id`）は `$id.tsx` ファイルを作成する
