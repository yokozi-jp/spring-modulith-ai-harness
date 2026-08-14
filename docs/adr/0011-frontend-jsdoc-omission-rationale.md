# フロントエンド コンポーネント/Hook への JSDoc は不要

`frontend-code-patterns.md` で「コンポーネント/Hook に JSDoc は不要」としている設計判断の根拠。backend（Java）はテストコードで Javadoc を必須化しており（`test-coding-standards.md`）、一見矛盾するように見えるため記録する。

## 背景

backend の実装コード（`Category` 集約等）を確認すると、実際は以下の選択的な運用になっている:

| 対象 | Javadoc |
|---|---|
| クラス | あり |
| 全フィールド | あり（1行） |
| public コンストラクタ・メソッド | あり（`@param`/`@return` 付き） |
| `@Override` メソッド | なし |
| private メソッド | なし |

つまり backend も「全メソッドに必ずコメントする」わけではなく、**public な API サーフェスにのみ Javadoc を必須化**している。

## frontend で JSDoc を省略できる理由

Java の `@param name カテゴリ名` のような Javadoc は、パラメータの**意味**（「これは何を表す値か」）を補足するために必要になる。Java の型システム単体では `String name` が何を意味するかまでは表現できない。

一方 frontend の TypeScript では、Props を interface として明示的に定義するため、型定義自体が Javadoc の `@param` と同じ役割を担う:

```tsx
interface OrderListProps {
  readonly orders: Order[];
  readonly onSelect: (id: string) => void;
}

export function OrderList({ orders, onSelect }: OrderListProps) {
```

`OrderListProps` を見れば「注文一覧を受け取り、選択時のコールバックを持つ」ことが分かる。これに `/** 注文一覧を表示するコンポーネント */` を追加しても、型シグネチャの言い換えにしかならず、情報量が増えない。

## 一貫している点

- backend: public API には Javadoc（型では表現できない意味を補う）、private/自明な Override には付けない
- frontend: 型で表現できる情報（何を受け取るか）は型定義に任せる。型で表現できない情報（**なぜ**そうするかというビジネスロジックの理由）は `frontend-code-patterns.md` の「ビジネスロジックには理由コメントを付ける」でカバーしている

両者とも「型シグネチャで表現できない情報にのみコメントを書く」という同じ原則に基づいており、矛盾ではない。TypeScript と Java の型表現力の違いに起因する運用差である。
