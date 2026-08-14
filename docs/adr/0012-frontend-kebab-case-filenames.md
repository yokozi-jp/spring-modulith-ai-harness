# フロントエンド ファイル名は kebab-case に統一する

`frontend-dev-environment.md` の「ファイル命名規則」で「kebab-case を使う（oxlint で強制）」としている設計判断の根拠。

## 決定

`frontend/src/` 配下のファイル名は kebab-case に統一する。`unicorn/filename-case`（`case: "kebabCase"`）で機械的に強制する。

TanStack Router のファイルベースルーティング規約が要求する記号（`$`, `_`, `.`）は、`unicorn/filename-case` が英数字・`-`・`_` 以外の文字を検査対象外として無視する仕様のため、kebab-case ルールと衝突しない（例: `products_.$id.tsx` は `$` が無視され、残りの `products_`, `id` が小文字+アンダースコアなので違反にならない）。

## 理由

### 1. ファイルシステムの大文字小文字非依存性

macOS・Windows のデフォルトファイルシステムは大文字小文字を区別しないが、Linux（本プロジェクトの CI・コンテナ実行環境）は区別する。`UserList.tsx` と `userList.tsx` を両方コミットしようとすると、大文字小文字を区別しない環境では git が違いを検知できず、意図しない上書き・欠落が発生し得る。kebab-case は小文字のみを使うため、この種の環境依存の事故が構造的に発生しない。

### 2. URL との対応

TanStack Router のファイルベースルーティングでは、ファイル名がそのまま URL パスに変換される（`products.tsx` → `/products`）。URL パスは `rest-api-standards.md` の規約上すでに複数形・ケバブケース（`/performance-ideas` 等）であり、ファイル名を同じ表記に揃えることでファイル名 → URL の変換規則が単純になる。

### 3. ファイル名とエクスポート識別子の役割分離

React コンポーネントは `export function UserList()` のように PascalCase で書く必要があるが、ファイル名まで PascalCase にすると「ファイル名」と「識別子名」が同じ見た目になり、ファイル名だけでは中身が型なのかコンポーネントなのか判別しにくくなる。ファイル名を常に kebab-case に固定することで、ファイル名は「配置・検索のためのラベル」、識別子名は「中身の型を表すシグナル」という役割分担が明確になる。

### 4. import 時の表記揺れ防止

大文字小文字混在の命名規則を許すと、同一ファイルに対して `./UserList` と `./userList` のような表記揺れが発生し得る。特に大文字小文字を区別しないファイルシステム上で開発すると、誤った大文字小文字の import でもローカルでは動いてしまい、Linux 環境（CI）でのみ失敗する。kebab-case は常に小文字なので、この種のゆらぎが構造的に発生しない。

## 例外

- 自動生成ファイル（`src/api/`, `routeTree.gen.ts` 等）は生成元ツール（Orval, TanStack Router CLI）の命名規則に従うため対象外とする
- TanStack Router のルートファイル（`__root.tsx`, `$id.tsx` 等）はフレームワーク規約に従うが、前述の通り `unicorn/filename-case` の仕様上 kebab-case ルールと衝突しない

## Consequences

- 新しいファイルを作成する際、コンポーネント名・Hook 名等の識別子は PascalCase/camelCase のままで良いが、ファイル名は必ず kebab-case にする
- `unicorn/filename-case` が `vp lint` で機械的に強制するため、レビューでの目視確認は不要
