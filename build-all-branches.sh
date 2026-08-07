#!/usr/bin/env bash
# 遍历所有分支执行构建，报告每个分支的结果。
#
# 用法：
#   ./build-all-branches.sh            # 默认 compileJava（快，Mixin AP 检查注入点 + 编译错误）
#   ./build-all-branches.sh full       # 完整 build（含 jar 产出）
#   ./build-all-branches.sh publish    # 发布到本地 Maven 仓库（publishToMavenLocal → ~/.m2）
#   ./build-all-branches.sh compile 26  # 只构建名字包含 "26" 的分支（可选过滤，也可传任意子串，如 publish 26）
#
# 注意：脚本会依次 git checkout 每个分支（会改变工作区），构建完停在最后一个分支。
# 每个分支的完整构建日志保存在 build-all-logs/<分支名>.log。

set -u

cd "$(dirname "$0")" || exit

MODE="${1:-compile}"
FILTER="${2:-}"

GRADLE_TASK="compileJava"
case "$MODE" in
  full)    GRADLE_TASK="build" ;;
  publish) GRADLE_TASK="publishToMavenLocal" ;;
esac

JAVA_HOME_DEFAULT="D:/Java/jdk-25.0.3"
LOG_DIR="build-all-logs"
mkdir -p "$LOG_DIR"

FAILED=""
COUNT=0
for b in $(git for-each-ref --format='%(refname:short)' refs/heads/); do
  if [ -n "$FILTER" ] && [[ "$b" != *"$FILTER"* ]]; then
    continue
  fi
  COUNT=$((COUNT+1))
  echo "========== [$COUNT] $b ($GRADLE_TASK) =========="
  if ! git checkout "$b" 2>&1; then
    echo ">>> FAILED: checkout $b（工作区可能有未提交改动）"
    FAILED="$FAILED $b"
    continue
  fi
  LOG="$LOG_DIR/${b//\//-}.log"
  if JAVA_HOME="$JAVA_HOME_DEFAULT" ./gradlew "$GRADLE_TASK" --console=plain > "$LOG" 2>&1; then
    echo ">>> OK: $b"
  else
    echo ">>> FAILED: $b（日志: $LOG）"
    FAILED="$FAILED $b"
    grep -E "error:|BUILD FAILED|FAILURE:|> Task .* FAILED" "$LOG" | head -8
  fi
done

echo ""
echo "========== 总结 =========="
echo "共构建 $COUNT 个分支"
if [ -z "$FAILED" ]; then
  echo "全部成功"
else
  echo "失败分支:$FAILED"
  echo "失败详情见 $LOG_DIR/"
fi
