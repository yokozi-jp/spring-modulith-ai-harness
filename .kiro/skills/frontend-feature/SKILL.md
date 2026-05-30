---
name: frontend-feature
description: Scaffold a new frontend feature with components, hooks, types, and tests following project conventions. Use when user wants to create a new feature, add a frontend module, scaffold frontend code, or mentions "new feature" in the frontend context.
---

# Frontend Feature Scaffold

Generate a complete feature structure under `frontend/src/features/<name>/`.

## Generated Structure

```
frontend/src/features/<feature-name>/
├── components/
│   ├── <feature-name>.tsx
│   └── <feature-name>-skeleton.tsx
├── hooks/
│   ├── use-<feature-name>.ts
│   └── use-<feature-name>.test.ts
└── types/                          # 必要な場合のみ
    └── <feature-name>.ts
```

## Workflow

1. ユーザーに確認:
   - Feature 名（kebab-case）
   - 何をする機能か
   - API からデータ取得するか（→ `@/api/` から import）

2. ステアリングに従ってファイル生成:
   - コンポーネント → `frontend-code-patterns.md`
   - Hook → `frontend-code-patterns.md`, `frontend-data-patterns.md`
   - UI 状態 → `frontend-ui-patterns.md`
   - テスト → `frontend-test-patterns.md`

3. 検証:
   ```bash
   cd frontend && ./scripts/verify.sh
   ```

## Route File Generation

Feature 作成時に対応するルートファイルも生成する。

生成先: `frontend/src/routes/<name>.tsx`

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

## Rules

すべてのコード生成は以下のステアリングに従う:

- `frontend-rules.md` — ディレクトリ構成、import ルール、禁止事項
- `frontend-code-patterns.md` — コンポーネント、Hook、イベントハンドラの書き方
- `frontend-data-patterns.md` — useQuery、useMutation、エラーハンドリング
- `frontend-ui-patterns.md` — Loading、Error、Empty、フォームの UI パターン
- `frontend-test-patterns.md` — テストの書き方、モック方針

生成後は必ず `./scripts/verify.sh` で規約準拠を確認する。
