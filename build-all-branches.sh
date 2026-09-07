#!/usr/bin/env bash
# 遍历所有分支执行构建，报告每个分支的结果。
#
# 用法：
#   ./build-all-branches.sh [compile|full|publish] [--only <branch...>] [--all]
#
#   compile  默认；快速：Mixin AP 检查注入点 + 编译错误
#   full     完整 build（含 jar 产出）；多平台分支构建后校验 fabric/neoforge 双 jar
#   publish  发布到本地 Maven 仓库（publishToMavenLocal → ~/.m2）
#   --only   精确分支名白名单（空格分隔，需完整匹配分支名）；未指定则构建全部未跳过分支
#   --all    强制构建「被新多平台分支覆盖的旧单加载器分支」（默认跳过，避免同 MC 版本重复构建）
#
# 多平台分支：根 build.gradle 声明了 common/fabric/neoforge 子模块（如合并后的 26.2）。
#   根 gradle 任务自动覆盖全部子模块，无需 :fabric:build；full 模式校验 fabric/neoforge 双 jar 落盘。
# 单加载器分支（fabric/X 或 neoforge/X，尚未合并）：原逻辑，直接 ./gradlew <task>。
#
# 默认跳过「已有多平台分支覆盖的旧单加载器分支」：对 fabric/X、neoforge/X，若存在「裸 X」多平台分支
# （即 $x/common/build.gradle 存在）则跳过（当前即 fabric/26.2、neoforge/26.2）。
# 每个分支的完整构建日志保存在 build-all-logs/<分支名>.log。

set -u

cd "$(dirname "$0")" || exit

MODE="${1:-compile}"
shift || true

ONLY=""
ALL=0
while [ $# -gt 0 ]; do
  case "$1" in
    --only) shift; ONLY="$ONLY $1" ;;
    --all)  ALL=1 ;;
    *)      ONLY="$ONLY $1" ;;
  esac
  shift
done

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
SKIPPED=0
for b in $(git for-each-ref --format='%(refname:short)' refs/heads/); do
  # --only 精确白名单（完整匹配分支名）
  if [ -n "$ONLY" ]; then
    matched=0
    for o in $ONLY; do [ "$o" = "$b" ] && matched=1; done
    [ "$matched" = "1" ] || continue
  fi

  # 默认跳过「被多平台分支覆盖的旧单加载器分支」：裸 X 多平台 → 跳过 fabric/X、neoforge/X
  is_legacy_single=0
  if [[ "$b" == fabric/* || "$b" == neoforge/* ]]; then
    x="${b#*/}"
    if git cat-file -e "$x:common/build.gradle" 2>/dev/null; then
      is_legacy_single=1
    fi
  fi
  if [ "$is_legacy_single" = "1" ] && [ "$ALL" = "0" ]; then
    echo "== 跳过 $b（已有多平台分支 $x 覆盖，用 --all 强制构建）=="
    SKIPPED=$((SKIPPED+1))
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

  # 多平台分支：根 build.gradle 声明 common/fabric/neoforge 子模块
  is_multi=0
  [ -f common/build.gradle ] && [ -f fabric/build.gradle ] && [ -f neoforge/build.gradle ] && is_multi=1

  if JAVA_HOME="$JAVA_HOME_DEFAULT" ./gradlew "$GRADLE_TASK" --console=plain > "$LOG" 2>&1; then
    # full 模式下多平台分支校验 fabric/neoforge 双 jar 落盘
    if [ "$GRADLE_TASK" = "build" ] && [ "$is_multi" = "1" ]; then
      if ls fabric/build/libs/pehkui-rebuilt-fabric-*.jar neoforge/build/libs/pehkui-rebuilt-neoforge-*.jar >/dev/null 2>&1; then
        echo ">>> OK: $b（含 fabric/neoforge 双 jar）"
      else
        echo ">>> FAILED: $b 缺 fabric/neoforge jar（日志: $LOG）"
        FAILED="$FAILED $b"
        grep -E "error:|BUILD FAILED|FAILURE:" "$LOG" | head -5
      fi
    else
      echo ">>> OK: $b"
    fi
  else
    echo ">>> FAILED: $b（日志: $LOG）"
    FAILED="$FAILED $b"
    grep -E "error:|BUILD FAILED|FAILURE:|> Task .* FAILED" "$LOG" | head -8
  fi
done

echo ""
echo "========== 总结 =========="
echo "共构建 $COUNT 个分支，跳过 $SKIPPED 个"
if [ -z "$FAILED" ]; then
  echo "全部成功"
else
  echo "失败分支:$FAILED"
  echo "失败详情见 $LOG_DIR/"
fi
