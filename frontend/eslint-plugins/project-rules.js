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
          description: "features hooks 内で apiClient を直接 import することを禁止",
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
        if (!filename.includes("src/features") || !filename.includes("/hooks/")) {
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
     * コンポーネントはアロー関数ではなく関数宣言で定義する
     *
     * export const X = () => {} ではなく export function X() {} を使う。
     * 理由: React 公式推奨、DevTools での表示名が明確、一貫性。
     */
    "no-arrow-function-component": {
      meta: {
        type: "problem",
        docs: {
          description: "コンポーネントはアロー関数ではなく関数宣言で定義する",
          recommended: true,
        },
        messages: {
          useFunction:
            "コンポーネントはアロー関数ではなく関数宣言で定義してください。export const {{ name }} = () => {} → export function {{ name }}() {}",
        },
      },
      create(context) {
        const filename = context.filename || context.getFilename();

        // .tsx ファイルのみ対象
        if (!filename.endsWith(".tsx")) {
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
            if (node.declaration && node.declaration.type === "VariableDeclaration") {
              for (const declarator of node.declaration.declarations) {
                if (
                  declarator.init &&
                  declarator.init.type === "ArrowFunctionExpression" &&
                  declarator.id &&
                  declarator.id.type === "Identifier"
                ) {
                  const name = declarator.id.name;
                  // PascalCase（コンポーネント名）のみ対象
                  if (name[0] === name[0].toUpperCase() && name[0] !== name[0].toLowerCase()) {
                    context.report({
                      node: declarator,
                      messageId: "useFunction",
                      data: { name },
                    });
                  }
                }
              }
            }
          },
        };
      },
    },

    /**
     * Hook はアロー関数ではなく関数宣言で定義する
     *
     * export const useX = () => {} ではなく export function useX() {} を使う。
     * 理由: コンポーネントと統一、一貫性。
     */
    "no-arrow-function-hook": {
      meta: {
        type: "problem",
        docs: {
          description: "Hook はアロー関数ではなく関数宣言で定義する",
          recommended: true,
        },
        messages: {
          useFunction:
            "Hook はアロー関数ではなく関数宣言で定義してください。export const {{ name }} = () => {} → export function {{ name }}() {}",
        },
      },
      create(context) {
        const filename = context.filename || context.getFilename();

        // .ts ファイルのみ対象（.tsx は no-arrow-function-component でカバー）
        if (!filename.endsWith(".ts") || filename.endsWith(".d.ts")) {
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

        // Orval 生成コード（src/api/）は除外
        if (filename.includes("/src/api/")) {
          return {};
        }

        return {
          ExportNamedDeclaration(node) {
            if (node.declaration && node.declaration.type === "VariableDeclaration") {
              for (const declarator of node.declaration.declarations) {
                if (
                  declarator.init &&
                  declarator.init.type === "ArrowFunctionExpression" &&
                  declarator.id &&
                  declarator.id.type === "Identifier"
                ) {
                  const name = declarator.id.name;
                  // use で始まる Hook のみ対象
                  if (
                    name.startsWith("use") &&
                    name.length > 3 &&
                    name[3] === name[3].toUpperCase()
                  ) {
                    context.report({
                      node: declarator,
                      messageId: "useFunction",
                      data: { name },
                    });
                  }
                }
              }
            }
          },
        };
      },
    },

    /**
     * Props は分割代入で受け取る
     *
     * function X(props: XProps) ではなく function X({ a, b }: XProps) を使う。
     * 理由: props.xxx は冗長、使用している Props が明確になる。
     */
    "no-props-object-param": {
      meta: {
        type: "problem",
        docs: {
          description: "Props は分割代入で受け取る",
          recommended: true,
        },
        messages: {
          useDestructuring:
            "Props は分割代入で受け取ってください。(props: {{ type }}) → ({ ... }: {{ type }})",
        },
      },
      create(context) {
        const filename = context.filename || context.getFilename();

        // .tsx ファイルのみ対象
        if (!filename.endsWith(".tsx")) {
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

        function checkFunctionParams(node, functionName) {
          // PascalCase の関数（コンポーネント）のみ対象
          if (!functionName || functionName[0] !== functionName[0].toUpperCase()) {
            return;
          }

          const params = node.params;
          if (params.length !== 1) {
            return;
          }

          const param = params[0];
          // Identifier + TypeAnnotation で *Props で終わる型の場合
          if (
            param.type === "Identifier" &&
            param.typeAnnotation &&
            param.typeAnnotation.typeAnnotation
          ) {
            const typeNode = param.typeAnnotation.typeAnnotation;
            let typeName = "";
            if (typeNode.type === "TSTypeReference" && typeNode.typeName) {
              typeName = typeNode.typeName.name || "";
            }
            if (typeName.endsWith("Props")) {
              context.report({
                node: param,
                messageId: "useDestructuring",
                data: { type: typeName },
              });
            }
          }
        }

        return {
          FunctionDeclaration(node) {
            checkFunctionParams(node, node.id?.name);
          },
          FunctionExpression(node) {
            checkFunctionParams(node, node.id?.name);
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

        // Orval 生成コード（src/api/）は除外
        if (filename.includes("/src/api/")) {
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
              node.declaration.id.name[3] === node.declaration.id.name[3].toUpperCase()
            ) {
              context.report({ node, messageId: "wrongFile" });
            }
          },
        };
      },
    },
  },
};
