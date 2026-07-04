// @ts-check

/** @type {import('lint-staged').Config} */
export default {
  // TypeScript/TSX ファイル: vp check + カスタムチェック
  "src/**/*.{ts,tsx}": (files) => {
    // ファイルパスを相対パスに変換
    const relativeFiles = files.map((f) => f.replace(process.cwd() + "/", ""));

    return [
      // フォーマットチェック
      `vp fmt ${relativeFiles.join(" ")} --check --ignore-path .oxfmtignore`,
      // Lint + 型チェック（対象ファイルのみ）
      `vp lint ${relativeFiles.join(" ")}`,
      // カスタムチェック（全体スキャン）
      "./scripts/checks/check-hook-location.sh",
      "./scripts/checks/check-features-structure.sh",
      "./scripts/checks/check-ui-readonly.sh",
      "./scripts/checks/api-readonly.sh",
      "./scripts/checks/check-no-direct-api-client.sh",
    ];
  },
};
