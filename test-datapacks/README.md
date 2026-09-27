# Pehkui 缩放规则 — 测试数据包

验证数据包缩放规则（`data/<namespace>/pehkui_scale_rules/*.json`）的行为。三个包可以同时装，各自用不同命名空间，不会互相覆盖。

> **为什么要放仓库根目录**：这些包原先放在 `run/` 下，而 `run/` 在 `.gitignore` 里 —— 清理构建产物目录时会连同一起被删掉。放根目录可进版本控制。

## 怎么用

把要测的包整个目录拷进世界/服务端的 `datapacks/`：

```bash
# 单机（客户端存档）
cp -r test-datapacks/01-valid-rule-test run/saves/<世界名>/datapacks/

# 专用服务端
cp -r test-datapacks/01-valid-rule-test run/world/datapacks/
```

进游戏后 `/reload` 或重启。用 `/datapack list` 确认已启用。

观察方式：

```mcfunction
/scale get @e[type=zombie,limit=1] pehkui:width     # 读实际值
/summon minecraft:zombie                            # 新生成的实体才会被规则作用
/kill @e[type=!player]                              # 清场重来
```

日志在 `run/logs/latest.log`，过滤 `[pehkui]` 看规则加载期的报错。

---

## 01-valid-rule-test — 正经用例

命名空间 `pehkui_test`。**全部应正常加载，日志里无 ERROR。**

| 文件 | 目标 | 预期 |
|---|---|---|
| `01_zombie_set` | 僵尸 | width / height = **0.5**（纯数字写法等价于 `set`） |
| `02_creeper_multiply` | 苦力怕 | width / height = **1.5**（在默认 1.0 上乘） |
| `03_skeleton_stack_low` | 骷髅 | priority 0 |
| `04_skeleton_stack_high` | 骷髅 | priority 10 |
| ↑ 两条一起 | 骷髅 | width = **2.25**（1 × 1.5 × 1.5，**两条规则都生效**，按 priority 升序折叠） |
| `05_pig_smooth` | 猪 | width 1 → 2，**约 2 秒渐变**（`delay: 40` + `quadratic_out`）。若瞬间跳变即为回归 |
| `06_sheep_legacy` | 羊 | width = 0.75（旧式 `scale_type` + `value` 顶层写法） |
| `07_cow_modifiers` | 牛 | `modifiers` 段生效，`base_multiplier` 挂在 width 上 |
| `08_chicken_add` | 鸡 | width = **1.5**（1 + 0.5） |
| `09_bat_divide` | 蝙蝠 | width = **0.5**（1 / 2） |
| `10_spider_tag` | 蜘蛛 | 标签条件 `#minecraft:arthropod` 命中（1.21.1 是单数；26.x 才叫 `arthropods`），width = 1.4 |
| `11_vindicator_persist` | 卫道士 | height = 1.6，`persist: true` → 写入 NBT，`/reload` 后仍在 |
| `12_husk_team` | 尸壳 | 需先 `/team add boss` + `/team join boss @e[type=husk]`，然后 width = 1.25 |

**关键回归点**：`03`+`04` 必须叠加成 2.25。如果只得到 1.5，说明「多规则叠加」退回了「只有最高优先级生效」。

**快照基准**：反复 `/reload` 后 `05_pig_smooth` 的猪不应越来越小/越来越大 —— 每轮都从实体原始尺寸重新折叠，不累积。

---

## 02-invalid-rule-test — 故意错误的用例

命名空间 `pehkui_test_invalid`。**加载期应逐条报 ERROR/WARN 并跳过该条，但服务器正常启动、其余规则照常工作。**

| 文件 | 构造的错误 | 预期 |
|---|---|---|
| `01_unknown_scale_type` | `pehkui:not_a_scale` | ERROR 未知 scale type，该条跳过 |
| `02_unknown_operation` | `pehkui:frobnicate` | ERROR 未知运算符 |
| `03_missing_value` | 有 `operation` 无 `value` | ERROR 缺必需字段 |
| `04_divide_by_zero` | `divide` 0 | 加载期 WARN「folds to Infinity」；运行时该类型保留原值 |
| `05_negative_result` | `subtract` 5 → -4 | 加载期 WARN「folds to -4.0」；运行时保留原值 |
| `06_negative_delay` | `delay: -1` | ERROR 非负校验 |
| `07_unknown_easing` | `pehkui:not_an_easing` | ERROR 未知缓动 |
| `08_unknown_modifier` | `pehkui:not_a_modifier` | ERROR 未知 modifier（必须已注册，否则客户端会因 id 缺失而 desync） |
| `09_no_scales` | 只有 `conditions` | 整条跳过，不崩 |
| `10_noop_operation` | `pehkui:noop` | ERROR —— `noop` 是注册表默认值，不是可用运算符 |
| `11_infinite_value` | `value: 1e400` | ERROR「Expected a finite number」（Gson 会解析成 Infinity） |
| `12_malformed_id` | `"not a valid id"` | ERROR 非法标识符 |
| `13_bad_conditions` | 不存在的实体类型 | 不应崩服；按未命中处理 |

**关键回归点**：这些包**不应导致服务器无法启动**。任何一条让 `Failed to load datapacks, can't proceed` 都是回归。

---

## 03-malformed-json — 语法错误

命名空间 `pehkui_test_malformed`。**单独一个包，避免污染上面两个。**

- `broken.json` —— 结尾多一个逗号，JSON 语法非法
- `good_alongside.json` —— 同目录下的正常规则

用来判断**单个坏文件是否会让整包规则一起失效**。想让 `01`/`02` 正常测时，别装这个包。

---

## ⚠️ 版本差异：原版 `scale` 属性在 1.21.2 改名了

尺寸属性是 **1.20.5** 加入原版的（`Attributes.SCALE`），但它的**序列化 id 在 1.21.2（快照 24w33a）从 `generic.scale` 改名为 `scale`**：

| MC | 属性 id | 翻译键 |
|---|---|---|
| 1.20.5 ~ 1.21.1 | `minecraft:generic.scale` | `attribute.name.generic.scale` |
| **1.21.2+** | `minecraft:scale` | `attribute.name.scale` |

对本分支（`minecraft_version_range=>=1.21 <1.21.2`）的影响：

- **代码零影响。** Pehkui 全程通过 `Attributes.SCALE` 这个 `Holder<Attribute>` 字段引用该属性（见 `util/VanillaScaleSyncBack.java`），没有按字符串查，也没有定义原版属性的翻译键。重命名在 1.21.1 与 1.21.2+ 上都自动正确。
- **命令与文档受影响。** 本分支上验证原版 scale 兼容要用：
  ```mcfunction
  /attribute @s minecraft:generic.scale base set 2
  ```
  在 1.21.2+ 分支上须改成 `minecraft:scale`。
- **移植到 1.21.2+ 时**：这是唯一需要留意的点，其余 scale 相关代码无需改动。

## 文档来源与分支间的不一致

本分支的 `DATAPACK-SCALE-RULES.md` 是**从 `26.3` 分支同步**过来的（426 行那份，`Operations` / `Per-scale adjustments` / `Modifiers` 都是独立章节），再按 1.21.1 改了三处：

1. `pack.mcmeta`：26.3 用 `min_format` / `max_format`（1.21.9+ 的写法），1.21.1 用单个 `pack_format: 48` + `supported_formats`
2. 实体类型字段：26.3 写 `entity_type`，**1.21.1 是 `type`**（`EntityPredicate.CODEC` 里是 `optionalFieldOf("type")`）
3. 补了 `fabric:load_conditions` 的说明 —— 本包是双平台构建，Fabric 读 `fabric:load_conditions`，**NeoForge 读 `neoforge:conditions`**，用错的那个会被忽略

> ⚠️ **`1.21.11` / `26.1.2` / `26.2` 三个分支的文档还没同步**，仍是旧的 270 行版本（`## Behavior` 里写着「Multiple rules do not stack / 只有最高优先级的规则生效」，与实现矛盾，且完全没写运算符）。需要逐个从 26.3 同步。

`01`+`04_skeleton_stack_*` 这两条就是用来复核叠加行为的：骷髅 width 应为 **2.25**，不是 1.5。
