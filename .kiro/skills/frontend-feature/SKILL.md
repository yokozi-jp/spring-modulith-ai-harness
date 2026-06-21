---
name: frontend-feature
description: >-
  Scaffold a NEW frontend feature from scratch. Use when user says
  "フロントエンドを作って", "画面を作って", "CRUD画面", "一覧画面", "詳細画面",
  "create frontend", "create UI", or requests a new feature that does not exist yet.
  Do NOT use for modifying existing features.
---

# Frontend Feature Scaffold

Generate a complete feature structure under `frontend/src/features/<name>/`.

## When to Use This Skill

Use this skill when the user:
- Asks to create a new frontend feature (e.g., "カテゴリのCRUDを作って")
- Requests a new screen/page (e.g., "商品一覧画面を作って")
- Mentions creating UI for an existing backend API
- Uses keywords: フロントエンド, 画面, CRUD, 一覧, 詳細, create frontend, create UI

Do NOT use when:
- Modifying an existing feature (follow `frontend-rules.md` instead)
- Fixing bugs in existing code
- Adding fields to existing components

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

1. 必要な情報を確認（不足があれば質問）

2. API の存在確認:
   - `src/api/` に該当する hooks があるか確認
   - なければ Orval でコード生成:
     ```bash
     cd frontend
     ./scripts/orval-generate.sh   # backend 起動中に実行
     ```
   - スクリプトが backend から OpenAPI spec を取得し、`src/api/` に hooks を生成する

3. ステアリングに従ってファイル生成:
   - コンポーネント → `frontend-code-patterns.md`
   - Hook → `frontend-code-patterns.md`, `frontend-data-patterns.md`
   - UI 状態 → `frontend-ui-patterns.md`
   - テスト → `frontend-test-patterns.md`

4. 検証:
   ```bash
   cd frontend && ./scripts/verify.sh
   ```

## 必要な入力

以下を指示に含めてください。不足している場合は確認します。

| 項目 | 必須 | 例 |
|------|:----:|-----|
| 機能名 | ✅ | 注文一覧、ユーザー詳細 |
| API エンドポイント | ✅ | GET /api/v1/orders |
| 表示するフィールド | ✅ | id, customerName, status |
| 操作（CRUD） | — | 作成、更新、削除 |
| 特殊な UI 要件 | — | ページネーション、フィルタ |

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
