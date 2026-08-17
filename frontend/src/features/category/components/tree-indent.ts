/** ツリーの階層深度に応じたインデントクラス。動的テンプレートリテラルでの生成は禁止のため事前定義する。 */
const INDENT_CLASSES = [
  "pl-2",
  "pl-7",
  "pl-12",
  "pl-17",
  "pl-22",
  "pl-27",
  "pl-32",
  "pl-37",
] as const;

/** depth に応じたインデントクラスを返す。配列の最大深度を超える場合は頭打ちにする。 */
export function getIndentClass(depth: number): string {
  return INDENT_CLASSES[Math.min(depth, INDENT_CLASSES.length - 1)] ?? "pl-2";
}
