#!/bin/bash
set -e

vp install -g kspec

echo ""
echo "=== 完了 ==="
echo "kspec $(kspec --version) をインストールしました"
echo ""
echo "プロジェクトで初期化するには："
echo "  cd /home/projects/spring-modulith-ai-harness"
echo "  kspec init"
