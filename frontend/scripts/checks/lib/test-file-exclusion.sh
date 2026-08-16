#!/usr/bin/env bash
# テストファイル判定の共有ヘルパー
#
# 複数の check-*.sh スクリプトが「このファイルはテストファイルか」を
# それぞれ個別に判定しており、一部のスクリプトだけ *.test.tsx の除外漏れが
# 発生するバグが過去に2回発生した（check-features-structure.sh の
# --file モード / 全体スキャンモードの両方で発生）。
# この共有関数に一元化し、判定ロジックの重複・非対称を防ぐ。
#
# 使い方:
#   source "$(dirname "$0")/lib/test-file-exclusion.sh"
#   if is_test_file "$FILENAME"; then ...; fi

# 引数のファイル名（basename）がテストファイル（*.test.ts / *.test.tsx）かどうかを判定する。
# 戻り値: 0 = テストファイルである, 1 = テストファイルでない
is_test_file() {
  local filename="$1"
  [[ "$filename" == *.test.ts || "$filename" == *.test.tsx ]]
}

# find の -name 条件で使う除外パターン（配列展開して使用する）
# 例: find src -type f "${TEST_FILE_EXCLUDE_FIND_ARGS[@]}"
TEST_FILE_EXCLUDE_FIND_ARGS=(! -name "*.test.ts" ! -name "*.test.tsx")
