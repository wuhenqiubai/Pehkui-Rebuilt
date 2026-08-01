# Pehkui Mixin 目录重构设计

日期：2026-08-01
分支：`fabric/1.21.1`（MC 1.21.1，工作区版本）
状态：已获用户批准

## 背景与目标

Pehkui 原作者以增量开发方式维护，mixin 层累积了横跨 **1.14~1.21** 的版本兼容实现：单一配置 `pehkui.mixins.json` 登记 285 个条目，其中 **167 个（58.6%）在 1.21.1 上被 `VersionUtils.shouldApplyCompatibilityMixin` 判定为不生效**。臃肿、可读性差、稳定性难保证，原作者因此弃坑。

本分支是为重构而拉出的，将作为后续向 1.21.x 各版本迭代的**基础模板**。

**目标**：把 mixin 层精简为「仅含 1.21.1 生效注入点的 ~81 个核心类」，彻底取缔 `compat<major><minor>[plus|minus]` 版本分类模式。未来每个 MC 版本开独立分支，从本骨架拷贝并调整注入点。

## 现状分析（数据依据）

模拟 `VersionUtils.shouldApplyCompatibilityMixin`（MINOR=21, PATCH=1）的判定结果：

| 分类 | 数量 | 说明 |
|---|---|---|
| json 登记 mixin 条目 | 285 | 含 `DataCommandInvoker$Get/Path/Scaled` 内部类 |
| 1.21.1 实际生效 | 118 | 纯版本 107 + mod 适配 11 |
| 1.21.1 被禁用（历史遗留） | 167 | 全部来自 `compat114`~`compat1206minus` 包 |
| 磁盘 .java 文件 | 284 | + `PehkuiMixinConfigPlugin`（不入 json） |

**关键发现——重名类同时生效**：15 个重名类（`LivingEntityMixin ×6`、`EntityMixin ×5`、`PlayerEntityMixin ×4`、`MobEntityMixin ×3`、`PersistentProjectileEntityMixin ×3` 等）各自 `@Mixin` 同一目标类、注入**不同方法**，且因「plus」语义在 1.21.1 上同时生效。例如 6 个 `LivingEntityMixin` 注入 `tickMovement`/`applyArmorToDamage`/`getMaxHealth`/`getAttackDistanceScalingFactor`/`applyClimbingSpeed`/`tickCramming`/`onKilledBy`/`isClimbing`/`updateLimbs`/`getPassengerRidingPos`/`getDimensions`。

因此重构核心不是「删旧文件」，而是**按目标类合并**：同一目标类的全部生效注入点收敛到单一 mixin 类。

## 决策记录（用户已确认）

1. **mod 适配层**：`reach / step_height / identity / magna / optifine` 的第三方 mod 适配 **全部删除**（identity 在 1.20 已死，其余停更或删库跑路；SSC 只调用核心公开 API，不依赖任何适配层）。
2. **目录组织**：服务端核心 mixin 平铺到 `mixin/` 根目录，客户端平铺到 `mixin/client/`，无 mod 适配子目录。
3. **基础设施**：最小删除——删 `BackwardsCompatibility`；`VersionUtils` 只删 `shouldApplyCompatibilityMixin`、保留 `MAJOR/MINOR/PATCH` 常量；`PehkuiMixinConfigPlugin` 只留 ThreadSafe/Unsafe 开关。
4. **分支策略**：直接在 `fabric/1.21.1` 上重构，产物作为未来 1.21.x 分支的模板。
5. **验证方式**：重构全程用 IDEA MCP `get_file_problems` 直接确认 mixin 注入是否生效（编译器不检查注入点字符串）。

## 目标目录结构

```
mixin/
├── <81 个核心类>              # 服务端：66 个单例平铺 + 15 组重名合并
├── PehkuiMixinConfigPlugin.java
└── client/                     # 客户端核心类（合并 + 平铺）
```

## 合并映射（15 组，107 个纯版本生效类 → 81 个类）

| 合并后类 | 源文件 |
|---|---|
| `EntityMixin` | root + compat116plus + compat117plus + compat1205plus + compat120plus |
| `LivingEntityMixin` | root + compat115plus + compat116plus + compat117plus + compat1194plus + compat1205plus |
| `PlayerEntityMixin` | root + compat117plus + compat1194plus + compat1205plus |
| `MobEntityMixin` | root + compat116plus + compat1202plus |
| `PersistentProjectileEntityMixin` | root + compat116plus + compat121plus |
| `BoatEntityMixin` | root + compat1205plus |
| `ItemEntityMixin` | root + client.compat116plus |
| `PotionEntityMixin` | root + compat1205plus |
| `PreEntityTickMixin` | root + compat121plus |
| `ProjectileUtilMixin` | compat117plus + compat1203plus |
| `WitherEntityMixin` | root + compat1205plus |
| `ClientPlayerEntityMixin` | client root + client.compat1182plus |
| `EntityRenderDispatcherMixin` | client.compat115plus + client.compat121plus |
| `GameRendererMixin` | client.compat1193plus + client.compat121plus |
| `InventoryScreenMixin` | client.compat1202plus + client.compat1205plus |

合并规则：把各 compat 版的注入点方法体并入 root 类（无 root 版则用任一 compat 版为基底），**去重并集**，逐注入点核对 1.21.1 方法签名。

## 删除清单

删除总数：285 登记条目 → ~81 条目（-204）。按类别组织：

**A. 版本 compat 子包内容（178 个条目）**
1. 全部被禁用的 167 个条目（1.21.1 上 `VersionUtils` 判定不生效，纯版本 143 + mod 适配 24）。
2. 1.21.1 上生效的 11 个 mod 适配条目（见 B 类）。

**B. 第三方 mod 适配（35 个条目，启用 11 + 禁用 24，全部删除）**
3. `reach.*`（22 个）——reach-entity-attributes 已死。核心 `REACH/BLOCK_REACH/ENTITY_REACH` 缩放能力保留（`ScaleTypes` + `ScaleUtils.getBlockReachScale` 等）。
4. `step_height.*`（5 个）。核心 `STEP_HEIGHT` 缩放能力保留（SSC 的 `ModifyStepHeightPower` 依赖）。
5. `identity.*`（4 个）。
6. `magna.*`（1 个）。
7. OptiFabric 适配（`client.compat115plus.optifine.compat.InGameOverlayRendererMixin` 等 3 个）。

**C. 特殊 / 基础设施**
8. `pehkui.compat.ScaleTypeMixin`（NOOP 占位，仅为 `preApply` 的 ASM 提供挂载点）。
9. `BackwardsCompatibility` 类 + `ScaleType` 静态块调用 + `PehkuiMixinConfigPlugin.preApply` 的 ASM 逻辑（该逻辑只对 MC<1.18 生效，1.21.1 永不触发）。
10. `IdentityCompatibility`、`ReachEntityAttributesCompatibility` 单例 + `Pehkui.onInitialize` 里对应触发（引用面已确认：前者仅被 `identity/compat/PlayerEntityMixin` 引用，后者仅被 `reach/compat/client/GameRendererMixin` 引用）。
11. `VersionUtils.shouldApplyCompatibilityMixin`。
12. **`pehkui.mixins.json` 重写**为仅含保留 mixin（285 → ~81 条目）。

## 基础设施处理

| 组件 | 处理 |
|---|---|
| `BackwardsCompatibility` | 删类 + 清 `ScaleType` 静态块 + 清 `PehkuiMixinConfigPlugin.preApply` |
| `VersionUtils` | 删 `shouldApplyCompatibilityMixin`，保留 `MAJOR/MINOR/PATCH`（网络 `ScalePayload` 注册、命令回退路径仍引用） |
| `PehkuiMixinConfigPlugin` | 删全部 mod 门控 + 版本门控，只留 ThreadSafe/Unsafe 开关 + `onLoad` 包名校验 |
| `IdentityCompatibility` / `ReachEntityAttributesCompatibility` | 删类 + 清 `Pehkui.onInitialize` |
| `GravityChangerCompatibility` | 保留（`ScaleUtils.getEyePos` 依赖） |
| `MulticonnectCompatibility` | 保留（root `LivingEntityMixin` 依赖） |
| `ImmersivePortalsCompatibility` | 保留（无 mixin 引用，仅 `onInitialize` 触发加载，非 mixin） |
| `ThreadSafeScaledEntityMixin` / `ThreadUnsafeScaledEntityMixin` | 保留（线程安全开关，与版本无关） |

## 实施步骤（5 个阶段）

1. **合并 15 组重名类**（38 文件 → 15 类）。逐个用 `get_file_problems` 确认合并后每个注入点仍命中目标方法。
2. **平铺单例**：~30 个带 compat 前缀的单例改 package 到 `mixin/` 或 `mixin/client/`（无重名，纯移动 + 改包声明）。
3. **删除**：全部 compat 子包（禁用 167 + 已合并来源 + mod 适配 + optifine）+ `pehkui.compat` 目录。
4. **重写 `pehkui.mixins.json`** + 基础设施清理（见上表）。
5. **全量验证**：`mcp__idea__build_project` → `./gradlew build` → `runClient` 进游戏测 `/scale` 命令与缩放行为。

## 验证策略

- **每阶段**：IDEA MCP `get_file_problems` 逐个检查合并/平铺后的 mixin（含注入点命中）。这是核心验证手段，编译器不检查注入点。
- **阶段 5**：`build_project` 全项目诊断 → `./gradlew build` 最终确认 → `runClient` 运行时验证。
- **json 与文件漂移**：阶段 4 用脚本从实际存在的类生成 json 条目，再人工校对。
- SSC 不直接编译本仓库（依赖 Modrinth 发布 jar），重构验证以 Pehkui 自身构建 + 运行时为准。

## 风险与对策

- **同目标类同方法的多个注入点**：合并后注入顺序可能变化。对策：对照原文件逐方法核对集合完整性，`get_file_problems` 把关。
- **删除引用断裂**：删除适配单例前 grep 确认无残留引用（已做，见基础设施表）。
- **重名类遗漏**：合并映射表已从模拟结果生成，阶段 1 用脚本再次核对其完整性。
- **升级模板**：重构后的骨架需在 `CLAUDE.md` 记录「每版本分支 = 拷贝此骨架 + 调整注入点」的新维护约定。