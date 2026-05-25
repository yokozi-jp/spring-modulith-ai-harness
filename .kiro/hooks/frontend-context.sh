#!/usr/bin/env bash
# Kiro CLI agentSpawn hook: セッション開始時にフロントエンドの現在構造を出力し、
# AIのコンテキストに注入する。

set -euo pipefail

FRONTEND_DIR="frontend/src"

if [[ ! -d "$FRONTEND_DIR" ]]; then
  exit 0
fi

echo "## フロントエンド現在構造"
echo ""

# features 一覧
FEATURES=$(find "$FRONTEND_DIR/features" -mindepth 1 -maxdepth 1 -type d 2>/dev/null | sort)
if [[ -n "$FEATURES" ]]; then
  echo "### features/"
  while IFS= read -r dir; do
    name=$(basename "$dir")
    echo "- $name/"
    # サブディレクトリとファイル
    for sub in components hooks types; do
      if [[ -d "$dir/$sub" ]]; then
        files=$(find "$dir/$sub" -type f -name "*.ts" -o -name "*.tsx" 2>/dev/null | sort)
        if [[ -n "$files" ]]; then
          echo "  - $sub/: $(echo "$files" | xargs -I{} basename {} | tr '\n' ' ')"
        fi
      fi
    done
  done <<< "$FEATURES"
  echo ""
fi

# routes 一覧
ROUTES=$(find "$FRONTEND_DIR/routes" -type f -name "*.tsx" 2>/dev/null | sort)
if [[ -n "$ROUTES" ]]; then
  echo "### routes/"
  echo "$ROUTES" | sed "s|$FRONTEND_DIR/routes/||" | sed 's/^/- /'
  echo ""
fi

# hooks 一覧（汎用）
HOOKS=$(find "$FRONTEND_DIR/hooks" -type f -name "use-*.ts" 2>/dev/null | sort)
if [[ -n "$HOOKS" ]]; then
  echo "### hooks/ (汎用)"
  echo "$HOOKS" | xargs -I{} basename {} | sed 's/^/- /'
  echo ""
fi

# api 生成済みファイル
API_FILES=$(find "$FRONTEND_DIR/api" -type f -name "*.ts" ! -name ".gitkeep" 2>/dev/null | sort)
if [[ -n "$API_FILES" ]]; then
  echo "### api/ (Orval生成)"
  echo "$API_FILES" | xargs -I{} basename {} | sed 's/^/- /'
  echo ""
fi
