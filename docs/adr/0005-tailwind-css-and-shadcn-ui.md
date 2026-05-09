# フロントエンドCSS手法・UIコンポーネント: Tailwind CSS + Shadcn/ui

フロントエンドのスタイリングに Tailwind CSS、UIコンポーネントに Shadcn/ui を採用する。スタイルの書き方が1通りに限定されるため、Lint で機械的に統制でき、AI エージェントが規約に準拠したコードを安定して生成できる。

## Considered Options

| 選択肢 | 不採用理由 |
|---|---|
| MUI (Material UI) | スタイリング手法が `sx` / `styled()` / `styleOverrides` / CSS Modules と3〜4通り混在し、「正しい1つの書き方」を Lint で強制できない。公式 ESLint プラグインが存在しない。バージョン間の破壊的変更（v4→v5→v6）が多く、AI 生成コードが古い記法になりやすい |
| Ant Design | カスタマイズに CSS 変数の上書きが必要で暗黙的。Lint で統制する仕組みがない |
| Chakra UI v3 | Panda CSS ベースに移行中で過渡期。v2→v3 の破壊的変更が大きく、AI の学習データとの乖離が激しい |
| Radix UI（素のまま） | ヘッドレスでスタイルなし。Shadcn/ui が Radix をラップ済みなので直接使う理由がない |
| Headless UI | コンポーネント数が約10個で業務アプリには不足 |
| daisyUI | コードが手元にないため構造テスト・カスタム Lint が書けない |
| CSS Modules | 命名規則・ネスト・セレクタ設計が人依存。コンポーネントとスタイルの対応関係を Lint で強制できない |
| styled-components / Emotion | ランタイム CSS-in-JS。React 19+ の Server Components と相性が悪い。React チームが離脱を推奨 |
| Panda CSS | 技術的には優秀だが UI コンポーネントライブラリがない。AI の学習データが少なく生成精度が低い |
| vanilla-extract | `.css.ts` ファイルが別途必要でファイル数が倍増。AI が2ファイル間の整合性を保つのが難しい |
| StyleX (Meta) | Vite 統合が公式サポートされていない。UI ライブラリがない |

## 採用理由

- **書き方が1通り**: `className="..."` にユーティリティクラスを並べるだけ。`eslint-plugin-tailwindcss` でクラス順序・重複・矛盾を機械的に検出可能
- **コードが手元にある**: Shadcn/ui はソースコードをプロジェクトにコピーする形式。構造テスト・カスタム Lint が書ける
- **AI 生成精度が高い**: Tailwind は学習データが豊富で記法がシンプル。構造的に壊れにくい
- **React Compiler と完全互換**: ランタイムコストゼロ。ゼロランタイムなので React 19+ の最適化を阻害しない
- **業務アプリ向けコンポーネントが揃っている**: テーブル、フォーム、ダイアログ、コマンドパレット等

## Consequences

- Tailwind CSS の設定（`tailwind.config.ts`）でデザイントークン（色、spacing 等）を一元管理する
- `eslint-plugin-tailwindcss` を導入し、クラス順序の自動ソート・重複検出を CI で強制する
- Shadcn/ui コンポーネントは `src/components/ui/` に配置し、プロジェクト固有のカスタマイズはその上のラッパーで行う
