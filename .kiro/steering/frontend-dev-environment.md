# Frontend 開発ルール

本プロジェクトのフロントエンド開発環境・ツールチェーン・プロジェクト構造に関するルール。
すべての作業は `frontend/` ディレクトリを起点とする。

---

## 技術スタック

- React 19 + TypeScript (strict)
- React Compiler（`babel-plugin-react-compiler`、`vite.config.ts` で有効化）
- TanStack Router（ファイルベースルーティング）
- Tailwind CSS v4 + Shadcn/ui
- Vite+（`vp` コマンドで操作）
- oxlint（Lint）+ oxfmt（フォーマッター）

---

## バックエンド連携

- バックエンドは `http://localhost:18080` で稼働（8080 は Keycloak）
- `/api/v1/*` は Vite プロキシ経由でアクセス

### Vite プロキシ設定（vite.config.ts）

```typescript
server: {
  proxy: {
    "/api": {
      target: "http://localhost:18080",
      changeOrigin: true,
    },
  },
}
```

---

## コマンド

```bash
cd frontend
vp install      # 依存インストール
vp dev          # 開発サーバー起動
vp check        # Lint + 型チェック + フォーマットチェック
vp test         # テスト実行
vp build        # 本番ビルド
```

- `npm`, `pnpm`, `yarn` を直接使わない。すべて `vp` 経由。
- `vitest`, `oxlint`, `oxfmt` を直接インストールしない。Vite+ に内蔵されている。

---

## Vite+ / oxlint のアップグレード

`vite-plus` は oxlint/oxfmt/vitest 等をバージョン固定で内蔵している。個別のツールだけを更新することはできず、`vite-plus` 自体をアップグレードする。

### 手順

```bash
# 1. グローバル vp を最新化（プロジェクトの vp バージョンはグローバル vp 以下にしかならない）
vp upgrade

# 2. プロジェクトの vite-plus を最新化
cd frontend
vp migrate

# 3. 依存関係を再インストール（migrate が失敗した場合）
vp install

# 4. バージョン確認
vp --version

# 5. 新しいツールバージョンで差分が出ないか確認
vp check
vp fmt      # フォーマッタの挙動が変わっている場合はここで一括修正
vp test run
```

### 注意事項

- **0.1.x → 0.2.x のようなメジャー版アップグレードは破壊的変更を含む**。事前にリリースノート（GitHub Releases）を確認する
  - Node.js バージョン要件の変更（例: `0.2.0` で Node 20 サポート終了）
  - `vitest` の内蔵ラッパー廃止（upstream 直接依存に変更）
  - 環境変数名のリネーム（`VITE_*` → `VP_*` 等）
- oxfmt / oxlint のバージョンが上がると、**それまで通っていたコードが新たにエラー・warning になることがある**。アップグレード後は必ず `vp check` → `vp fmt` → 再度 `vp check` の順で確認する
- `.oxlintrc.json` を作成しない。oxlint 設定（`categories`, `plugins`, `rules`, `overrides`, `settings`, `env`, `ignorePatterns`）は `vite.config.ts` の `lint` セクションに一元化する（同一ディレクトリに両方存在すると `vite.config.ts` の `lint` のみが読み込まれ `.oxlintrc.json` は無視されるため、併存させても意味がない）
- カテゴリを一括 `error` にする際は、そのカテゴリに属する全ルールを実際に `vp lint` で発火させて確認すること。**`restriction`/`style` カテゴリには相互に矛盾するルールが混在している**（例: `import/no-named-export` と `import/prefer-default-export` が同じ `style` カテゴリに存在し、両方を `error` にすると常に片方が違反になる）。本プロジェクトは全カテゴリを `error` にしたうえで、矛盾するルールや本プロジェクトの規約と衝突するルール（TanStack Router のファイルベースルーティング規約、Shadcn/ui の標準パターン等）を個別に `off` にする方式を採用している。`warn` に落として様子を見るのではなく、矛盾の原因を特定して該当ルールのみ無効化すること

---

## カスタム oxlint ルール vs 組み込みルール

`oxlint-plugins/project-rules.js` にカスタムルールを追加する前に、**oxlint の組み込みルールで同じ検証ができないか確認する**。

### 判断基準

| 状況 | 対応 |
|------|------|
| oxlint / ESLint プラグインに同等のルールが存在する | 組み込みルールを使う（保守コスト削減、Rust ネイティブで高速） |
| プロジェクト固有のアーキテクチャ制約（Orval Hook 強制等） | カスタムルールを書く |
| ファイル名パターンとの照合が必要（`use-*.ts` 限定等） | カスタムルールを書く（汎用ルールでは表現できない） |

### 確認方法

```bash
# oxlint の公式ドキュメントで該当ルールを検索
# https://oxc.rs/docs/guide/usage/linter/rules/

# 現在の oxlint バージョンを確認（組み込みルールがバージョン依存で追加される場合がある）
vp --version
```

新しいルールがある特定バージョン以降でしか使えない場合は、`vp upgrade` / `vp migrate` でアップグレードしてから移行する（上記「Vite+ / oxlint のアップグレード」参照）。移行・採用の経緯は `docs/adr/0010-frontend-lint-config-sources.md` を参照。

---

## ディレクトリ構成

ディレクトリ構成は `frontend/README.md` の「ディレクトリ構成」を参照する。

共通コンポーネント（複数 feature で使うもの）は `components/` 直下に配置する。

---

## ファイル命名規則

- **kebab-case** を使う（oxlint で強制）
- コンポーネント: `user-list.tsx`
- Hook: `use-user-list.ts`
- 型定義: `member.ts`
- ルートファイル: TanStack Router の規約に従う（`__root.tsx`, `_layout.tsx`, `$param.tsx`, `index.tsx`）

### TanStack Router ルート命名規則

| パターン | ファイル名 | URL | 親ルート |
|----------|-----------|-----|----------|
| 基本 | `products.tsx` | `/products` | `__root` |
| ネスト（レイアウトなし） | `products_.new.tsx` | `/products/new` | `__root`（独立） |
| ネスト（レイアウトあり） | `products/new.tsx` | `/products/new` | `products.tsx` が layout |
| 動的パラメータ | `products_.$id.tsx` | `/products/:id` | `__root`（独立） |
| 動的パラメータの子 | `products_.$id.edit.tsx` | `/products/:id/edit` | `products_.$id.tsx`（子ルート） |
| 動的パラメータ後の独立 | `products_.$id_.edit.tsx` | `/products/:id/edit` | `__root`（独立） |

- ネストルートでレイアウトを共有しない場合は `_` を使う（例: `products_.new.tsx`）
- `createFileRoute` の引数はファイル名と一致させる（例: `createFileRoute("/products_/new")`）
- **動的パラメータの後に独立ルートを作る場合は `$id_` のように `_` を追加する**
  - `products_.$id.edit.tsx` → 詳細ページの子ルート（詳細ページに `<Outlet />` が必要）
  - `products_.$id_.edit.tsx` → 独立ルート（詳細ページと並列、`<Outlet />` 不要）
- CRUD 画面では編集・削除は詳細ページと**独立**させる（`$id_.edit.tsx` 形式を使う）

### ネスト vs 独立の判断基準（一覧・詳細・編集すべてに適用）

親ルートに `<Outlet />` を実装する意図が本当にあるかで判断する。一覧ページに `<Outlet />` がなければ、詳細ページも独立ルート（`categories_.$id.tsx` 形式）にする。同様に詳細ページに `<Outlet />` がなければ、編集ページも独立ルート（`$id_.edit.tsx` 形式）にする。

CRUD 画面は基本的に「一覧」「詳細」「編集」がそれぞれ全画面を使う別レイアウトであり、親ルートが共通レイアウト（タブ・サイドパネル等）を提供する設計でない限り、ネスト（`_` なし）を使う理由はない。ネストは「親ルートが実際に子ルートを差し込む `<Outlet />` を持ち、共通のレイアウトを提供する」場合にのみ使う。

`<Outlet />` のないルートを親として `_` なしでファイルを作ると、子ルートのコンテンツがどこにも表示されない不具合になる。ファイル作成前に親ルートの実装を確認し、`<Outlet />` の有無で独立/ネストを決めること。

---

## ページファイル（routes/）のルール

ページファイルは**薄く保つ**。以下のみ記述する:

- ルート定義（`createFileRoute`）
- loader（データ取得の宣言）
- features コンポーネントの組み合わせ

以下は書かない:

- ビジネスロジック → `features/<feature>/hooks/`
- UI 部品の実装 → `features/<feature>/components/`
- API 呼び出しの詳細 → `features/<feature>/hooks/`

### import(max-dependencies) 超過を避ける事前分割

oxlint の `import/max-dependencies`（デフォルト閾値 10）により、1ファイルの import 数が多すぎるとエラーになる。詳細ページのようにヘッダー・データ表示・状態ハンドリング・確認ダイアログ等が集まるページでは、実装してから1個ずつ超過分を削るのではなく、**最初から以下の単位で `features/<feature>/components/` に分割する**:

| 分割単位 | 責務 | 命名例 |
|---|---|---|
| `<Resource>DetailHeader` | 見出し・編集/削除ボタン・削除確認ダイアログ | `CategoryDetailHeader` |
| `<Resource>DetailState` | Loading/Error/NotFound の状態ハンドリング（early return） | `CategoryDetailState` |
| `<Resource>DetailCard` | 詳細情報の表示本体 | `CategoryDetailCard` |
| `<Resource>DetailSkeleton` | ローディング時のスケルトン | `CategoryDetailSkeleton` |

ルートファイル（`routes/<resource>_.$id.tsx`）はこれらを組み合わせるだけにし、`import` はコンポーネント・Hook・`createFileRoute` 程度に収める。分割後もエラーが出る場合は、同じ粒度でさらに機能単位のコンポーネントに切り出す。

---

## 変更後の確認

コード変更後は必ず以下を実行する:

```bash
./scripts/verify.sh    # Lint + 型チェック + フォーマット + カスタムチェック全実行
```

エラーが出た場合は修正してからコミットする。
自動修正可能なものは `./scripts/verify.sh --fix` で修正できる。

検証コマンドの一覧・使い分け、Kiro CLI hook（`preToolUse`/`stop`）と Git pre-commit hook を含めた品質チェックの全体像は `frontend/README.md` の「コード品質の仕組み」を参照する。

### 既知の制約: `routeTree.gen.ts` が `--fix` なしの `vp check`/`vp fmt --check` でフォーマット差分として検出される

`src/routeTree.gen.ts`（TanStack Router の自動生成ファイル）は `.oxfmtignore` に登録済みだが、`vp fmt --check`（`vp check` の内部でも同様）実行時にこの ignore 設定が適用されず、毎回フォーマット差分として検出される（`vp` v0.2.9 で確認）。`vp fmt`（`--fix` 相当のデフォルト書き込みモード）では正しく除外される。

原因は `.oxfmtignore` の設定ミスではなく、vite-plus が `vite.config.ts` の `fmt.ignorePath` 設定を write モードと check モードで異なる経路で oxfmt に渡しており、check モード側で設定が伝わっていない vite-plus 側の不具合と判断している（`--ignore-path=.oxfmtignore` を明示指定すれば check モードでも正しく除外されることを確認済み）。oxfmt 本体の既知バグ（[oxc-project/oxc#16621](https://github.com/oxc-project/oxc/issues/16621)、ファイルパス直接指定時に ignore が無視される）は現行バージョンで修正済みのため無関係。

対処は常に `--fix` を使う:

```bash
# ✅ --fix を使えば解消する
./scripts/verify.sh --fix
vp check --fix

# ❌ --fix なしでは routeTree.gen.ts のフォーマット差分で毎回失敗する
vp check
```

`vite-plus` のアップグレード時にこの挙動が解消されているか確認する（上記「Vite+ / oxlint のアップグレード」参照）。

---

## チェックの線引き（oxlint vs shell）

判断基準（コードパターンか配置ルールか）は `frontend/README.md` の「コード品質の仕組み」の「oxlint と shell の役割分担」を参照する。

### oxlint カスタムルール（`oxlint-plugins/project-rules.js`）

- `project-rules/no-direct-api-client`: features/*/hooks/ 内で apiClient を直接 import することを禁止
- `project-rules/hook-in-dedicated-file`: Hook 関数（`export function use...`）は use-*.ts ファイルでのみ定義可能
- `project-rules/no-arrow-function-hook`: Hook はアロー関数ではなく関数宣言で定義
- `project-rules/no-props-object-param`: Props は分割代入で受け取る（`props: XProps` 禁止）

コンポーネントの関数宣言強制は oxlint 組み込みの `react/function-component-definition`（v1.75.0 以降）を使用する。自作ルールから移行済み（「カスタム oxlint ルール vs 組み込みルール」参照）。

### shell スクリプト（`scripts/checks/`）

- `check-hook-location.sh`: use-*.ts ファイルが正しいディレクトリ（hooks/）にあるか
- `check-features-structure.sh`: features/ ディレクトリ構造の検証
- `check-ui-readonly.sh`: components/ui/ の変更を検出
- `api-readonly.sh`: src/api/ の変更を検出

---

## 新規 feature 作成時の調査フロー

新規 feature（新しいリソースの CRUD 画面等）を作成する場合、実装計画を立てる前に以下の順序で調査する。順序を守ることで、調査の手戻り（ツール呼び出しのキャンセル・後戻り）や、スコープの見誤りを防ぐ。

1. **フロントエンド既存構造の確認** — `frontend/src` 配下（`routes/`, `features/`, `api/`）をディレクトリ一覧で確認し、対象リソースの Orval 生成コード（`src/api/<resource>/`）が既に存在するかを見る
2. **Orval 生成コードの確認** — `src/api/<resource>/<resource>.ts` と `openAPIDefinition.schemas.ts` を読み、利用可能な Hook・リクエスト/レスポンス型を把握する。CRUD 以外の操作（`move`, `publish` 等のドメインアクション）がないか確認する
3. **バックエンド Controller の確認** — `backend/src/main/java/.../presentation/controller/<Resource>Controller.java` を読み、エンドポイント一覧と各操作の入出力を正確に把握する（フロントの型だけでは操作の意図が読み取れない場合がある）
4. **既存 feature・共通コンポーネントの確認** — `frontend/src/features/` に類似 feature が既にあればパターンを参照する。`frontend/src/components/`（`EmptyState`, `ErrorMessage`, `ConfirmDialog` 等）を確認し、再実装しない
5. **実装計画の提示** — 1〜4 の調査結果を踏まえ、画面構成・スコープ（後述「複雑な関連リソースAPIのスコープ判断」参照）をユーザーに提示してから実装に着手する

この調査は基本的に並列実行可能な read/glob/grep で完結するため、都度ユーザーに確認を挟まず一括で行ってよい。ただしツール呼び出しがキャンセルされた場合は、再実行前に「何を確認しようとしていたか」を一言で示してから続行する。

## 複雑な関連リソースAPIのスコープ判断

対象リソースが階層構造やドメインアクション（`move`, `publish`, `archive` 等）を持つ場合、実装スコープを自己判断で絞らず、以下をユーザーに一度提示してから実装に着手する。

- 基本 CRUD（一覧・詳細・作成・更新・削除）に加えて、ドメインアクション系 API が存在すること
- どこまでを今回のスコープに含めるか（例:「階層移動 API は今回のスコープ外とし、基本 CRUD のみ実装します」）

一言の確認で済むため、実装を進めてから「実は移動機能も必要だった」という手戻りを避けられる。単純な CRUD のみのリソース（ドメインアクションのないリソース）ではこの確認は不要。

---

## 改修時のルール

既存機能を変更する場合:

1. **既存コードを読む** — 変更対象のファイルと関連ファイルを確認
2. **既存パターンに合わせる** — 命名、構造、スタイルを統一
3. **影響範囲を確認** — import 元、テスト、関連コンポーネント
4. **テストを更新** — 変更に応じてテストも修正

新規作成時は上記「新規 feature 作成時の調査フロー」+ ステアリングに従い、改修時は既存コード + ステアリングの両方に従う。

---

## ライブラリドキュメントの参照（Context7）

TanStack Router、TanStack Query、Radix UI 等のライブラリについて不明点がある場合は、**Context7 CLI** でドキュメントを参照する。

```bash
# ライブラリを検索
npx ctx7@latest library "TanStack Router" "createFileRoute の使い方"

# ドキュメントを取得（library コマンドで取得した ID を使用）
npx ctx7@latest docs /tanstack/router "createFileRoute params"
```

以下のケースで Context7 を使う:
- TanStack Router のルート定義（`createFileRoute` の引数、params の取り方）
- TanStack Query のオプション（`queryKey`、`staleTime`、`gcTime`）
- Radix UI / Shadcn/ui のコンポーネント Props（`asChild`、`onOpenChange`）
- Tailwind CSS v4 の新しい構文

---

## Lint エラー修正ワークフロー（AI 向け）

Lint エラーが発生した場合:

1. エラーメッセージのルール名を確認する
2. `.kiro/steering/frontend-lint-fix-guide.md` を参照して修正方法を特定する
3. 修正を適用する
4. `./scripts/verify.sh` で再確認する

ステアリング `frontend-lint-fix-guide.md` に各ルールの具体的な修正方法が記載されている。
