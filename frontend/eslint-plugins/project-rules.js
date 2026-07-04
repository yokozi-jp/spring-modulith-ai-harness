/**
 * プロジェクト固有の oxlint カスタムルール
 *
 * これらのルールは AST レベルでコードパターンを検証する。
 * ファイル配置や Git 状態に依存するチェックは shell スクリプトで行う。
 */

/** @type {import('eslint').ESLint.Plugin} */
export default {
  meta: {
    name: "project-rules",
  },
  rules: {
    /**
     * features内hooks内で apiClient を直接 import することを禁止
     *
     * Orval 生成の Hook を使用すべき。
     */
    "no-direct-api-client": {
      meta: {
        type: "problem",
        docs: {
          description:
            "features hooks 内で apiClient を直接 import することを禁止",
          recommended: true,
        },
        messages: {
          forbidden:
            "apiClient を直接 import しないでください。Orval 生成 Hook (@/api/*) を使ってください。",
          forbiddenFetch:
            "fetch を直接呼び出さないでください。Orval 生成 Hook (@/api/*) を使ってください。",
          forbiddenHandwrittenApi:
            "手書き API を import しないでください。Orval 生成 Hook (@/api/*) を使ってください。",
        },
      },
      create(context) {
        const filename = context.filename || context.getFilename();

        // features hooks 内のファイルのみ対象
        if (
          !filename.includes("src/features") ||
          !filename.includes("/hooks/")
        ) {
          return {};
        }

        // テストファイルは除外
        if (filename.includes(".test.")) {
          return {};
        }

        return {
          ImportDeclaration(node) {
            const source = node.source.value;

            if (source === "@/lib/api-client") {
              context.report({ node, messageId: "forbidden" });
            }

            if (
              typeof source === "string" &&
              source.startsWith("@/lib/") &&
              source.endsWith("-api")
            ) {
              context.report({ node, messageId: "forbiddenHandwrittenApi" });
            }
          },

          AwaitExpression(node) {
            if (
              node.argument.type === "CallExpression" &&
              node.argument.callee.type === "Identifier" &&
              node.argument.callee.name === "fetch"
            ) {
              context.report({ node, messageId: "forbiddenFetch" });
            }
          },
        };
      },
    },

    /**
     * Hook 関数は use-*.ts ファイルでのみ定義可能
     *
     * Hook を .tsx コンポーネントファイル内で定義することを禁止。
     * Hook は hooks ディレクトリの use-*.ts ファイルに分離すべき。
     */
    "hook-in-dedicated-file": {
      meta: {
        type: "problem",
        docs: {
          description: "Hook 関数は use-*.ts ファイルでのみ定義可能",
          recommended: true,
        },
        messages: {
          wrongFile:
            "Hook の定義は use-*.ts ファイルで行ってください。コンポーネントファイル内で Hook を定義しないでください。",
        },
      },
      create(context) {
        const filename = context.filename || context.getFilename();
        const basename = filename.split("/").pop() || "";

        // use-*.ts ファイルは OK
        if (basename.startsWith("use-") && basename.endsWith(".ts")) {
          return {};
        }

        // テストファイルは除外
        if (filename.includes(".test.")) {
          return {};
        }

        // src 外は対象外
        if (!filename.includes("/src/")) {
          return {};
        }

        return {
          ExportNamedDeclaration(node) {
            if (
              node.declaration &&
              node.declaration.type === "FunctionDeclaration" &&
              node.declaration.id &&
              node.declaration.id.name.startsWith("use") &&
              node.declaration.id.name.length > 3 &&
              node.declaration.id.name[3] ===
                node.declaration.id.name[3].toUpperCase()
            ) {
              context.report({ node, messageId: "wrongFile" });
            }
          },
        };
      },
    },
  },
};
