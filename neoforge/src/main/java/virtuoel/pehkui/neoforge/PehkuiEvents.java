package virtuoel.pehkui.neoforge;

import virtuoel.pehkui.Pehkui;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.entity.living.MobSplitEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import virtuoel.pehkui.api.PehkuiConfig;
import virtuoel.pehkui.api.ScaleRegistries;
import virtuoel.pehkui.api.ScaleType;
import virtuoel.pehkui.neoforge.data.ScaleRuleLoader;
import virtuoel.pehkui.neoforge.data.ScaleRules;
import virtuoel.pehkui.util.ScaleUtils;

/**
 * NeoForge 原生事件处理器，替代部分 Mixin 注入（减少 mixin）。在 {@link Pehkui} 构造器中手动注册到 {@code NeoForge.EVENT_BUS}。
 */
public class PehkuiEvents
{
	// 替代 SlimeEntityMixin：NeoForge 原生 MobSplitEvent，分裂子体继承父体缩放
	@SubscribeEvent
	public static void onMobSplit(final MobSplitEvent event)
	{
		final Mob parent = event.getParent();
		final float scale = ScaleUtils.getBoundingBoxHeightScale(parent);

		for (final Mob child : event.getChildren())
		{
			ScaleUtils.loadScale(child, parent);

			if (scale != 1.0F)
			{
				// 替代原 mixin 的 0.5D 垂直偏移注入：子体已 moveTo(getY() + 0.5)，调整为 getY() + 0.5 * scale
				child.setPos(child.getX(), child.getY() - 0.5D + 0.5D * scale, child.getZ());
			}
		}
	}

	// 替代 AnimalEntityMixin + FoxEntityMateGoalMixin：繁殖子体继承双亲平均缩放
	// BabyEntitySpawnEvent 触发于 Animal.spawnChildFromBreeding 与 Fox.spawnChildFromBreeding（村民繁殖不触发，保留 VillagerBreedTaskMixin）
	@SubscribeEvent
	public static void onBabySpawn(final BabyEntitySpawnEvent event)
	{
		ScaleUtils.loadAverageScales(event.getChild(), event.getParentA(), event.getParentB());
	}

	// 替代 ServerPlayerEntityMixin：死亡重生 / 末地跨维度时从旧玩家继承缩放
	@SubscribeEvent
	public static void onPlayerClone(final PlayerEvent.Clone event)
	{
		ScaleUtils.loadScaleOnRespawn(event.getEntity(), event.getOriginal(), !event.isWasDeath());
	}

	// 替代 PlayerManagerMixin：玩家加入时标记所有缩放待同步
	@SubscribeEvent
	public static void onPlayerLoggedIn(final PlayerEvent.PlayerLoggedInEvent event)
	{
		for (final ScaleType type : ScaleRegistries.SCALE_TYPES.values())
		{
			type.getScaleData(event.getEntity()).markForSync(true);
		}
	}

	// 替代 EntityMixin.startSeenByPlayer：实体开始被玩家追踪时同步缩放
	@SubscribeEvent
	public static void onStartTracking(final PlayerEvent.StartTracking event)
	{
		if (event.getEntity() instanceof ServerPlayer player)
		{
			ScaleUtils.syncScalesOnTrackingStart(event.getTarget(), player.connection);
		}
	}

	// 数据包缩放规则：注册数据包 reload listener 并缓存条件上下文
	@SubscribeEvent
	public static void onAddServerReloadListeners(final AddServerReloadListenersEvent event)
	{
		ScaleRules.setConditionContext(event.getConditionContext());
		event.addListener(Pehkui.id("scale_rules"), new ScaleRuleLoader());
	}

	// 数据包缩放规则：服务端启动后提供 registry lookup 供规则解码
	@SubscribeEvent
	public static void onServerStarted(final ServerStartedEvent event)
	{
		ScaleRules.setRegistryLookup(event.getServer().registryAccess());
	}

	// 数据包缩放规则改进：实体加入世界时立即应用一次规则（不等下一检测周期）
	@SubscribeEvent
	public static void onEntityJoinLevel(final EntityJoinLevelEvent event)
	{
		final Entity entity = event.getEntity();
		final Level level = event.getLevel();

		if (PehkuiConfig.COMMON.enableScaleRules.get() && !ScaleRules.isEmpty() && level instanceof ServerLevel)
		{
			final boolean affectPlayers = PehkuiConfig.COMMON.scaleRulesAffectPlayers.get();

			ScaleRules.applyRuleToEntity(entity, (ServerLevel) level, affectPlayers || !(entity instanceof Player));
		}
	}
}
