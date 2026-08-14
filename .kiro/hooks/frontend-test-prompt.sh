#!/usr/bin/env bash
# Kiro CLI postToolUse hook: Hook・ユーティリティファイルを作成した後、
# 対応するテストファイルが存在しなければ作成を促す。
# 対象は check-test-exists.sh と同じ（features/*/hooks/, hooks/, lib/）。
#
# exit 0 で STDOUT を AI コンテキストに追加。

set -euo pipefail

EVENT=$(cat)

# tool_input から path を抽出
FILE_PATH=$(echo "$EVENT" | grep -oP '"path"\s*:\s*"[^"]*"' | head -1 | grep -oP '(?<=")[^"]+(?="$)' || true)

if [[ -z "$FILE_PATH" ]]; then
  exit 0
fi

# 対象: features/*/hooks/, hooks/, lib/（check-test-exists.sh と同一範囲）
if [[ "$FILE_PATH" != *frontend/src/features/*/hooks/* \
   && "$FILE_PATH" != *frontend/src/hooks/* \
   && "$FILE_PATH" != *frontend/src/lib/* ]]; then
  exit 0
fi

# テストファイルは除外
if [[ "$FILE_PATH" == *.test.* ]]; then
  exit 0
fi

# .gitkeep, index.ts, api-client.ts, query-client.ts, .d.ts は除外
FILENAME=$(basename "$FILE_PATH")
if [[ "$FILENAME" == ".gitkeep" || "$FILENAME" == "index.ts" \
   || "$FILENAME" == "api-client.ts" || "$FILENAME" == "query-client.ts" \
   || "$FILENAME" == *.d.ts ]]; then
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
