# フロントエンド API クライアント: OpenAPI + Orval コードファースト生成

フロントエンドの API クライアント（型定義 + TanStack Query Hook）を手書きせず、
backend の OpenAPI spec から Orval で自動生成する。

## Considered Options

| 選択肢 | 不採用理由 |
|---|---|
| 手書き `fetch` / `apiClient` 呼び出し | backend の DTO 変更に追従できず型がすぐ乖離する。エンドポイント・パラメータ名の変更をコンパイル時に検出できない |
| GraphQL + codegen | backend が REST（Spring MVC + SpringDoc）で GraphQL 層を追加する理由がない |
| tRPC | backend が Java（Spring Boot）であり tRPC は TypeScript 前提のため採用不可 |
| OpenAPI Generator（Java 版） | 生成コードが TanStack Query と統合されておらず、Hook 層を別途手書きする必要がある |
| Swagger Codegen | メンテナンスが停滞気味。TanStack Query 統合のテンプレートが弱い |

## 採用理由

- **backend がドキュメントの正**: SpringDoc OpenAPI が Controller のアノテーションから spec を自動生成するため、
  backend の実装と OpenAPI spec が常に一致する（コードファースト）
- **型安全性**: Request/Response の型が backend の DTO と自動的に同期する。backend の破壊的変更は
  frontend のビルドエラーとして即座に検出できる
- **TanStack Query 統合**: Orval は `client: "react-query"` 設定で `useQuery`/`useMutation` ベースの
  Hook を直接生成する。キャッシュ・再検証のボイラープレートを書く必要がない
- **タグ単位のディレクトリ分割**: `mode: "tags-split"` により OpenAPI の tag（Controller 単位）ごとに
  `src/api/<tag>/` が生成され、機能追加時の見通しが良い

## 生成フロー

```text
backend (Spring Boot + SpringDoc)
  ↓ /v3/api-docs で OpenAPI spec を提供
curl -u admin:admin http://localhost:18080/v3/api-docs -o frontend/openapi.json
  ↓
npx orval（frontend/orval.config.ts の設定に従う）
  ↓
src/api/<tag>/<tag>.ts        … TanStack Query Hook（useList, useCreate 等）
src/api/openAPIDefinition.schemas.ts … 共通型定義
```

- backend が起動していることが前提（`docker ps | grep smah-backend`）
- `frontend/openapi.json` は生成物の中間ファイルであり、Git 管理される（backend 未起動でも `npx orval` だけで再生成できるようにするため）
- `src/api/` は Orval の生成物であり手動編集しない（再生成で上書きされる）

## カスタム fetch 実装（`src/lib/api-client.ts`）

Orval の `httpClient: "fetch"` はカスタム mutator を要求する。`api-client.ts` が以下を担う:

- CSRF トークン送信（Spring Security 連携、`XSRF-TOKEN` Cookie → `X-XSRF-TOKEN` ヘッダー）
- `credentials: "include"` で Cookie 送信
- エラーレスポンスの `ProblemDetail`（RFC 9457）パース
- Orval 生成コードが期待する `{ data, status, headers }` 構造でレスポンスを返す

このファイルは Orval の生成コードではなく、`orval.config.ts` の `mutator` として指定する手書きの薄いラッパー。

## Consequences

- backend の OpenAPI spec が変わるたびに `npx orval` を再実行し、`src/api/` を再生成する必要がある
- backend が起動していない、または `frontend/openapi.json` が古い場合、frontend は古い API 形状のまま開発が進む
  リスクがあるため、API 変更時は必ず regenerate してからコミットする
- `src/api/` を直接 import せず、`src/features/*/hooks/` で Orval Hook をラップして使う
  （詳細は `.kiro/steering/frontend-data-patterns.md` 参照）
- 手書き API 関数・`apiClient` の直接呼び出しは oxlint カスタムルール `project-rules/no-direct-api-client`
  で検出し、ビルドを失敗させる
