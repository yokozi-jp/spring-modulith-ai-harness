#!/usr/bin/env bash
# Hook と util のテストファイル存在チェック
#
# 対象:
#   - src/features/*/hooks/*.ts → *.test.ts または *.test.tsx が必要
#   - src/hooks/*.ts → *.test.ts または *.test.tsx が必要
#   - src/lib/*.ts → *.test.ts または *.test.tsx が必要
#
# 除外:
#   - *.test.ts / *.test.tsx（テストファイル自体）
#   - *.d.ts（型定義ファイル）
#   - index.ts（barrel export）

set -euo pipefail

cd "$(dirname "$0")/../.."
# shellcheck source=lib/test-file-exclusion.sh
source "scripts/checks/lib/test-file-exclusion.sh"

errors=()

# features/*/hooks/ 内の Hook ファイル
while IFS= read -r file; do
  base="${file%.ts}"
  test_file_ts="${base}.test.ts"
  test_file_tsx="${base}.test.tsx"
  if [[ ! -f "$test_file_ts" && ! -f "$test_file_tsx" ]]; then
    errors+=("$file: テストファイルがありません → $test_file_ts を作成してください")
  fi
done < <(find src/features -path "*/hooks/*.ts" \
  "${TEST_FILE_EXCLUDE_FIND_ARGS[@]}" \
  ! -name "*.d.ts" \
  ! -name "index.ts" \
  -type f 2>/dev/null)

# src/hooks/ 内の共通 Hook ファイル
while IFS= read -r file; do
  base="${file%.ts}"
  test_file_ts="${base}.test.ts"
  test_file_tsx="${base}.test.tsx"
  if [[ ! -f "$test_file_ts" && ! -f "$test_file_tsx" ]]; then
    errors+=("$file: テストファイルがありません → $test_file_ts を作成してください")
  fi
done < <(find src/hooks -name "*.ts" \
  "${TEST_FILE_EXCLUDE_FIND_ARGS[@]}" \
  ! -name "*.d.ts" \
  ! -name "index.ts" \
  -type f 2>/dev/null)

# src/lib/ 内のユーティリティファイル（設定ファイル的なものは除外）
while IFS= read -r file; do
  base="${file%.ts}"
  test_file_ts="${base}.test.ts"
  test_file_tsx="${base}.test.tsx"
  if [[ ! -f "$test_file_ts" && ! -f "$test_file_tsx" ]]; then
    errors+=("$file: テストファイルがありません → $test_file_ts を作成してください")
  fi
done < <(find src/lib -name "*.ts" \
  "${TEST_FILE_EXCLUDE_FIND_ARGS[@]}" \
  ! -name "*.d.ts" \
  ! -name "index.ts" \
  ! -name "api-client.ts" \
  ! -name "query-client.ts" \
  -type f 2>/dev/null)

if [[ ${#errors[@]} -gt 0 ]]; then
  echo "ERROR: テストファイルが不足しています"
  echo ""
  for err in "${errors[@]}"; do
    echo "  $err"
  done
  echo ""
  echo "テストファイルを作成してください。テンプレート:"
  echo ""
  echo '  import { describe, expect, it } from "vite-plus/test";'
  echo ""
  echo '  describe("<対象の名前>", () => {'
  echo '    it("should work correctly", () => {'
  echo '      expect(true).toBe(true);'
  echo '    });'
  echo '  });'
  exit 1
fi
