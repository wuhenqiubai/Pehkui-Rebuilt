#!/usr/bin/env bash
#
# 校验三份 ScaleType.java 没有发生漂移。
#
# 背景：上游 Pehkui 不是 Architectury 项目，而是每个 loader 一条分支、各持一份源码树，
# 同一套 API 在两个平台上本就不同 —— ScaleType 的三个事件成员在 Fabric 是
# net.fabricmc.fabric.api.event.Event，在 NeoForge 是 java.util.Collection。
# Rebuilt 因此按平台各存一份完整实现，另加一份 common 用的平台中性编译占位。
#
# 三份之间**允许**不同的只有被 ==== PLATFORM-SPECIFIC BEGIN/END ==== 标出的区域
# （类 javadoc、三个事件成员、Builder 里的事件注册）。其余部分必须**逐字一致** ——
# 一旦有人只改了一份、忘了同步另外两份，本脚本就会失败。
#
# 用法：./check-scale-type-drift.sh
# 退出码：0 = 无漂移；1 = 有漂移或文件缺失。

set -uo pipefail

cd "$(dirname "$0")" || exit 1

FILES=(
	"common/src/main/java/virtuoel/pehkui/api/ScaleType.java"
	"fabric/src/main/java/virtuoel/pehkui/api/ScaleType.java"
	"neoforge/src/main/java/virtuoel/pehkui/api/ScaleType.java"
)

# 剥离以下内容后输出，用于逐字比对：
#   - PLATFORM-SPECIFIC 标记区（各平台必然不同）
#   - import 行（Fabric 引 Event/EventFactory、NeoForge 引 ArrayList，必然不同）
#   - 行尾 \r（本仓库检出可能是 CRLF）
#   - 空行（三份在标记区前后的空行分布天然不同；空行无语义，丢弃以免误报）
normalize() {
	awk '
		/PLATFORM-SPECIFIC BEGIN/ { skip = 1 }
		skip { if (/PLATFORM-SPECIFIC END/) skip = 0; next }
		/^import / { next }
		{ gsub(/\r/, ""); if ($0 ~ /^[[:space:]]*$/) next; lines[++n] = $0 }
		END { for (i = 1; i <= n; i++) print lines[i] }
	' "$1"
}

WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT

status=0
key() { echo "$WORK/$(echo "$1" | tr '/' '_')"; }

for f in "${FILES[@]}"; do
	if [ ! -f "$f" ]; then
		echo "❌ 文件不存在：$f"
		status=1
		continue
	fi
	normalize "$f" > "$(key "$f")"
done

if [ "$status" -eq 0 ]; then
	base="$(key "${FILES[0]}")"
	for f in "${FILES[@]:1}"; do
		if ! diff -u "$base" "$(key "$f")" > "$WORK/d"; then
			echo "❌ 漂移："
			echo "     基准  ${FILES[0]}"
			echo "     对比  $f"
			echo "   平台无关部分出现差异（应为逐字一致）："
			sed 's/^/     /' "$WORK/d" | head -40
			echo ""
			status=1
		fi
	done
fi

if [ "$status" -eq 0 ]; then
	echo "✅ 三份 ScaleType.java 的平台无关部分逐字一致"
fi

exit "$status"
