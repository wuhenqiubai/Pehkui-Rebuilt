# NeoForge 事件总线替代 Mixin + NeoForge mixin/事件适配知识

日期：2026-08-06
分支：`nf1.21.2`（NeoForge 1.21.2，MojMap）
状态：已执行（用户批准）

## 背景与目标

nf1.21.2 分支移植完成后（接口注入崩溃修复见 git `42d3829a`），用 **NeoForge 原生事件总线** 替代部分「行为触发型」mixin，减少 mixin 数量。参考已完成的 `SlimeEntityMixin → MobSplitEvent` 案例（`PehkuiEvents.onMobSplit`）。同时沉淀 NeoForge 与 Fabric 在 mixin / 事件上的关键差异，供后续 1.21.x 分支迭代参考。

**核心结论**：NeoForge 事件总线是**通知机制**，不是**拦截修改机制**——它不支持修改方法返回值/参数。因此事件替代只适用于「行为触发型」mixin（某个原版动作发生时做副作用操作），**不适用**于 Pehkui 的核心缩放逻辑（值替换型注入点）。

## 本次替代清单（4 个 mixin 文件 + 1 个注入点）

| 原 Mixin | NeoForge 事件 | 替代逻辑 | 处理 |
|---|---|---|---|
| `AnimalEntityMixin` | `BabyEntitySpawnEvent`（fired from `Animal.spawnChildFromBreeding`）| `ScaleUtils.loadAverageScales(e.getChild(), e.getParentA(), e.getParentB())` | 删文件 |
| `FoxEntityMateGoalMixin` | `BabyEntitySpawnEvent`（fired from `Fox.spawnChildFromBreeding`）| 同上 | 删文件 |
| `ServerPlayerEntityMixin` | `PlayerEvent.Clone`（死亡重生 + 末地跨维度）| `loadScaleOnRespawn(e.getEntity(), e.getOriginal(), !e.isWasDeath())` | 删文件 |
| `PlayerManagerMixin` | `PlayerEvent.PlayerLoggedInEvent` | 遍历 `SCALE_TYPES` 逐 type `markForSync(true)` | 删文件 |
| `EntityMixin.startSeenByPlayer` | `PlayerEvent.StartTracking`（`getTarget()`=被追踪实体）| `syncScalesOnTrackingStart(e.getTarget(), ((ServerPlayer)e.getEntity()).connection)` | 减注入点，文件保留 |

全部 handler 集中在 `src/main/java/virtuoel/pehkui/PehkuiEvents.java`（该类已在 `Pehkui` 构造器注册到 `NeoForge.EVENT_BUS`）。

### 未替代的原因（事件替代的边界）

| 保留 | 原因 |
|---|---|
| `VillagerBreedTaskMixin` | 村民繁殖走 `VillagerMakeLove` behavior，**不触发** `BabyEntitySpawnEvent`（javadoc 只列了 Animal/Fox 两个触发点）|
| ~50 个值替换型注入点 | 事件不能改返回值/参数（`getDimensions`/`getMaxHealth`/`move`/`getBoundingBox`/伤害计算等）——Pehkui 核心功能 |
| 每 tick 轮询（`EntityMixin.tick`/`PreEntityTickMixin`/`EntityTrackerEntryMixin.sendChanges`）| 无 per-entity 每 tick 事件（`TickEvent` 是全局的）|
| NBT 序列化（`load`/`saveWithoutId`/`NbtPredicateMixin`/`EntitySelectorOptionsMixin`）| core data layer，无事件对应 |
| 17 个客户端渲染 mixin | 渲染是值替换，客户端无对应事件 |
| 弹射物 `<init>` 继承（ExplosiveProjectile/LlamaSpit/EvokerFangs/PersistentProjectile 等）| `EntityJoinLevelEvent` 触发时实体已在世界中，owner 引用时机有风险；且这些 mixin 含位置调整逻辑——列为后续可选 |

## NeoForge vs Fabric：mixin 配置差异

| 项 | Fabric | NeoForge |
|---|---|---|
| mixin 配置声明 | `fabric.mod.json` 的 `"mixins": [...]` 数组 | `neoforge.mods.toml` 的 `[[mixins]]` 头（指向 mixin **配置文件**）|
| `[[mixins]]` 属性 | — | `config`（必填，mixin json 路径）、`requiredMods`（可选，应用所需 mod）、`behaviorVersion`（可选，匹配的 fabric mixin 行为版本）|
| mixin json 本身 | 同 | 同（`pehkui.mixins.json` 结构一致）|
| 运行时 mixin 库 | Fabric Loom 注入 + fabric mixin | NeoForge 1.21.2 用 `net.fabricmc:sponge-mixin`（标准 Mixin 0.8.7 fork）+ Mercury 编译期预处理 |

本项目 `neoforge.mods.toml` 只需 `config="pehkui.mixins.json"`（无 `requiredMods`——不依赖特定 mod；无 `behaviorVersion`——标准 API 用默认行为）。

## NeoForge 事件总线（本项目用法）

事件总线分两条：
- **游戏总线 `NeoForge.EVENT_BUS`**：大多数事件（`MobSplitEvent`/`BabyEntitySpawnEvent`/`PlayerEvent.*`）
- **mod 总线**：`@Mod` 构造器注入的 `IEventBus modEventBus`（`RegisterCommandsEvent`、payload 注册等）

4 种注册方式（本项目用了 2 种）：
1. `bus.addListener(方法引用)` — `modEventBus.addListener(PehkuiPacketHandler::register)`
2. `@SubscribeEvent` + 实例 + `bus.register(instance)` — `NeoForge.EVENT_BUS.register(this)`（`Pehkui.onRegisterCommands`）
3. `@SubscribeEvent` + 静态 + `bus.register(Class)` — `NeoForge.EVENT_BUS.register(PehkuiEvents.class)`（**本项目事件替代全部走此方式**）
4. `@EventBusSubscriber(modid=...)` 自动发现（handler 必须 static）— 本项目未用（手动注册更明确）

注意事项（官方文档）：
- handler 必须是「单事件参数 + void 返回」的方法
- **不要监听抽象超类事件**（如 `EntityEvent` 基类）会崩游戏——必须监听具体子事件
- mod 总线事件并行触发；`@EventBusSubscriber` 建议指定 `modid`

## @Implements 接口注入陷阱（nf1.21.2 崩溃根因）

NeoForge 下 `@Mixin` 类直接 `implements` 自定义接口不生效（Fabric Loom 会编译期把接口注入 MC 类，NeoForge 没有），需用 Mixin 的 `@Implements` 显式声明。两个坑（导致 `ClassCastException: XxxRenderState cannot be cast to PehkuiEntityRenderStateExtensions`）：

1. **prefix 拼接规则**：`@Interface(prefix = "pehkui$")` 会让 Mixin 查找 `prefix + 接口方法名` 的方法。**接口方法名不能带 `pehkui$` 前缀**（否则查找 `pehkui$pehkui$xxx` 永远找不到，接口从不注入）。正确写法：接口方法 `float getModelWidthScale()`，mixin 私有/公有方法 `pehkui$getModelWidthScale()`。
2. **handler 必须 public**：`InterfaceInfo.renameMethod`（sponge-mixin 0.8.7 源码）要求 handler 方法满足 `ACC_PUBLIC`，否则抛 `"xxx cannot implement yyy because it is not visible"`。注意不是 private（Mixin 文档历史版本有混淆，以源码为准）。

参考实现：
- `src/main/java/virtuoel/pehkui/util/PehkuiEntityRenderStateExtensions.java`（接口方法**不带**前缀）
- `src/main/java/virtuoel/pehkui/mixin/client/EntityRenderStateMixin.java`（`@Implements` + `prefix="pehkui$"` + public handler）

## 迁移其他 1.21.x 版本时的检查清单

1. mixin 注入点方法串（MojMap）用 IDEA MCP `get_file_problems` 逐个验证
2. NeoForge 事件签名（javap `neoforge-<ver>.jar` 的 `net.neoforged.neoforge.event.*`）——事件类签名随版本可能变
3. `BabyEntitySpawnEvent` / `PlayerEvent.Clone` 等事件的触发点确认（GitHub javadoc）
4. 新分支直接拷贝本骨架，按新版本调注入点，不要恢复 compat 子包
