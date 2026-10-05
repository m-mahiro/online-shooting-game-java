#!/usr/bin/env bash
# 描画最適化(変更点1〜3)のビフォーアフターを計測するベンチマークを実行する(Linux/macOS用)。
# 使い方や結果の見方は benchmark/README.md を参照。
#
# 使用例:
#   ./run_benchmark.sh              # デフォルト設定（warmup=200, iterations=2000）で実行
#   ./run_benchmark.sh 50 500       # 手早く確認したい場合（warmup=50, iterations=500）

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/.."

WARMUP="${1:-200}"
ITER="${2:-2000}"

echo "Compiling benchmark sources (and the game sources they depend on)..."
mkdir -p benchmark/out

SRCS=$(find src benchmark/src -name "*.java")
javac -d benchmark/out -encoding UTF-8 $SRCS

echo
echo "Running benchmarks (warmup=$WARMUP, iterations=$ITER)..."
# java.awt.headless=true: このベンチマーク(変更点1〜3)はディスプレイ・GPUに依存しない
# 純粋なCPU/IOの計測なので、ヘッドレス環境でもWindowsの通常環境でも同じ条件で測れる。
java -Djava.awt.headless=true -cp benchmark/out benchmark.BenchmarkMain "$(pwd)" "$WARMUP" "$ITER"
