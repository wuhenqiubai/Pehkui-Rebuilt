package virtuoel.pehkui;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.MobSplitEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import virtuoel.pehkui.api.PehkuiConfig;
import virtuoel.pehkui.data.ScaleRuleLoader;
import virtuoel.pehkui.data.ScaleRules;
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

	// 数据包缩放规则：注册数据包 reload listener 并缓存条件上下文（1.21.1 用 AddReloadListenerEvent，无 key）
	@SubscribeEvent
	public static void onAddReloadListeners(final AddReloadListenerEvent event)
	{
		ScaleRules.setConditionContext(event.getConditionContext());
		event.addListener(new ScaleRuleLoader());
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
