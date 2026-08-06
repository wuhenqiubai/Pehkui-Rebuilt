package virtuoel.pehkui;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.entity.living.MobSplitEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import virtuoel.pehkui.api.ScaleRegistries;
import virtuoel.pehkui.api.ScaleType;
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
}
