#!/usr/bin/env bash
# Kiro CLI postToolUse hook: features/ 内にコンポーネントや Hook を作成した後、
# 対応するテストファイルが存在しなければ作成を促す。
#
# exit 0 で STDOUT を AI コンテキストに追加。

set -euo pipefail

EVENT=$(cat)

# tool_input から path を抽出
FILE_PATH=$(echo "$EVENT" | grep -oP '"path"\s*:\s*"[^"]*"' | head -1 | grep -oP '(?<=")[^"]+(?="$)' || true)

if [[ -z "$FILE_PATH" ]]; then
  exit 0
fi

# features/ 内の hooks/ または components/ のファイルのみ対象
if [[ "$FILE_PATH" != *frontend/src/features/*/hooks/* && "$FILE_PATH" != *frontend/src/features/*/components/* ]]; then
  exit 0
fi

# テストファイルは除外
if [[ "$FILE_PATH" == *.test.* ]]; then
  exit 0
fi

# .gitkeep は除外
FILENAME=$(basename "$FILE_PATH")
if [[ "$FILENAME" == ".gitkeep" ]]; then
  exit 0
fi

# 対応するテストファイルのパスを生成
BASE="${FILE_PATH%.*}"
EXT="${FILE_PATH##*.}"
TEST_PATH="${BASE}.test.${EXT}"

# テストファイルが既に存在するか確認
if [[ -f "$TEST_PATH" ]]; then
  exit 0
fi

cat <<EOF
📝 テストファイル未作成: $(basename "$TEST_PATH")

作成したファイル: $FILENAME
対応テスト: $TEST_PATH

テストファイルを作成してください。テンプレート:

\`\`\`typescript
import { describe, expect, it } from "vite-plus/test";

describe("$(basename "$BASE")", () => {
  it("should work correctly", () => {
    // TODO: implement test
    expect(true).toBe(true);
  });
});
\`\`\`

テストは対象ファイルと同じディレクトリに配置します。
実行: cd frontend && vp test
EOF
