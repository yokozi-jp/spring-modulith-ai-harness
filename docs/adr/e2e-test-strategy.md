# E2Eテスト戦略 — 決定事項

**最終更新**: 2026-07-13

---

## 1. テストフレームワーク

**Playwright を採用する。**

理由:
- AI 連携の公式サポート（MCP サーバー）
- CI/CD 適性（ヘッドレス・Docker・並列実行・シャーディング）
- 自動待機がデフォルト、テスト分離が強力

---

## 2. アーキテクチャ方針: 実行とメンテナンスの分離

- **テスト実行**: セレクタベースで決定的に動作（LLM 不要、高速、低コスト）
- **テストメンテナンス**: AI 活用でセレクタ更新・テスト修正を自動化

LLM コストは「実行回数」ではなく「変更頻度」に比例する。

---

## 3. テスト環境

| 項目 | 決定 |
|------|------|
| 言語 | TypeScript |
| 実行方式 | 全コンテナ完結（compose 内） |
| compose 分離 | ベース + オーバーライド（`compose.yaml` + `compose.e2e.yaml`） |
| backend ステージ | `runtime`（ビルド済み JAR 実行） |
| frontend | nginx + 静的 HTML スタブ（本物の SPA はマージ後に差し替え） |
| DB | ベース compose の postgres をオーバーライドで再利用（開発環境と同時起動しない） |
| Keycloak / Redis | ベース compose をオーバーライドで再利用 |
| ネットワーク | compose デフォルト。コンテナ名でアクセス |
| CI | GitHub Actions（全コンテナ毎回起動 → テスト → `down -v`） |
| ローカル | `make e2e-up` で起動しっぱなし、`make e2e-check` でテストだけ再実行 |

---

## 4. ディレクトリ構成

```
e2e/
├── tests/
│   ├── scenarios/           # ブラウザ操作による業務フロー
│   ├── api/                 # API レベル E2E（ブラウザ不要）
│   ├── global.setup.ts      # DB シード + 認証
│   └── global.teardown.ts   # クリーンアップ
├── pages/                   # Page Object Model（ページ単位）
├── frontend-stub/           # スタブ HTML + nginx 設定
├── scripts/                 # hook スクリプト等
├── playwright.config.ts
├── eslint.config.ts
├── package.json
├── tsconfig.json
└── Dockerfile               # e2e-runner イメージ
```

---

## 5. Page Object Model

- **ページ単位**で分割する（コンポーネント単位ではない）
- E2E はブラウザに見える「ページ」という安定したインターフェースにのみ依存する
- フロント内部のコンポーネント構成には関知しない
- コンポーネント単位のテストはフロント UT（Testing Library）の責務

---

## 6. テストデータシード

- Playwright 公式推奨の **Project Dependencies** パターンを採用
- `global.setup.ts` を setup project として定義
- `pg` クライアントで直接 PostgreSQL に TRUNCATE → INSERT
- HTML レポート・トレースに setup 結果が表示される

---

## 7. 認証

- **API テスト**: Keycloak Resource Owner Password Grant でアクセストークン取得 → Authorization ヘッダー付与
- **ブラウザテスト**: `global.setup.ts` でブラウザ経由の OAuth2 ログインを通し、`storageState`（セッションクッキー）を保存 → 全テストで共有

---

## 8. AI 駆動テスト — コマンド設計

| コマンド | 用途 | AI に許可 |
|---------|------|-----------|
| `make e2e-up` | 環境起動 | ❌ |
| `make e2e-check` | テスト実行 | ✅ |
| `make e2e-only T='...'` | 特定テスト実行 | ✅ |
| `make e2e-lint` | lint のみ | ❌（hook で自動実行） |
| `make e2e-down` | 環境停止 | ❌ |
| `make e2e-clean` | 完全リセット | ❌ |

---

## 9. AI 駆動テスト — ルール強制の3層構造

| レイヤー | 仕組み | 強制力 |
|---------|--------|--------|
| steering | エージェント設定（`.kiro/agents/e2e-test.json`） | ソフト |
| ESLint | `postToolUse` hook でファイル書き込み後に即時実行 | ハード |
| scaffold | 骨格生成して穴埋めさせる（今後設計） | ハード |

### lint 即時実行フロー

```
AI が fs_write でテストファイルを書く
  → postToolUse hook 発火（lint-on-write.sh）
  → 対象パス判定（e2e/tests/** or e2e/pages/**）
  → ESLint 実行
  → 違反時: STDERR が AI に warning → AI が修正
  → 通過時: 次の作業へ
```

### ESLint ルール（現時点）

- `@typescript-eslint/no-floating-promises` — await 忘れ防止
- `no-restricted-syntax` — `page.locator()` / `page.$()` 直接使用禁止（POM 経由を強制）

### エージェント制約

- `allowedPaths`: `e2e/tests/**`, `e2e/pages/**` のみ書き込み可
- `deniedPaths`: `playwright.config.ts`, `eslint.config.ts` 等の設定ファイル変更禁止

---

## 10. フロントエンド前提

- `yaguchi/frontend-setup` ブランチの構成をテスト対象の前提とする
- React 19 + TanStack Router + TanStack Query + Tailwind v4 + Shadcn/ui
- features: category, product, pricing
- フロント PR マージ後に `frontend-e2e` をビルド済み SPA に差し替え

---

## 未決定事項

- [ ] ブラウザテストの認証クッキードメイン問題の解消
- [ ] ESLint カスタムルールの追加（運用フィードバック後）
- [ ] steering の具体的な記述内容（`e2e-test-standards.md`）
- [ ] scaffold テンプレート設計
- [ ] GitHub Actions ワークフロー
- [ ] Keycloak 起動時間短縮（optimized build イメージ）
