package virtuoel.pehkui;

import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.MobSplitEvent;
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
}
