---
name: frontend-feature
description: Scaffold a NEW frontend feature from scratch. Use ONLY when creating a new feature that does not exist yet. Do NOT use for modifying or extending existing features — follow frontend-rules.md instead.
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

1. 必要な情報を確認（不足があれば質問）

2. API の存在確認:
   - `src/api/` に該当する hooks があるか確認
   - なければ「backend 起動 + `npx orval` を実行してください」と案内

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
