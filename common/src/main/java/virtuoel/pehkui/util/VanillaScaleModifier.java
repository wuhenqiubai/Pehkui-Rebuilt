package virtuoel.pehkui.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import virtuoel.pehkui.api.PehkuiConfig;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleModifier;

/**
 * 把原版 {@code minecraft:scale} 属性折入 Pehkui 的 {@code pehkui:base}，
 * 让 Pehkui 把原版缩放当作自身全局基础缩放的一部分。
 * 那些会自动带上原版缩放的消费方（hitbox 尺寸、模型渲染）会再把它除掉，避免二次应用。
 */
public class VanillaScaleModifier extends ScaleModifier
{
	public VanillaScaleModifier()
	{
		super(512.0F);
	}

	@Override
	public float modifyScale(final ScaleData scaleData, float modifiedScale, final float delta)
	{
		if (PehkuiConfig.COMMON.applyVanillaScale.get() && !PehkuiConfig.COMMON.vanillaScaleSyncBack.get())
		{
			final Entity entity = scaleData.getEntity();

			if (entity instanceof LivingEntity living)
			{
				return modifiedScale * living.getScale();
			}
		}

		return modifiedScale;
	}

	@Override
	public float modifyPrevScale(final ScaleData scaleData, float modifiedScale)
	{
		if (PehkuiConfig.COMMON.applyVanillaScale.get() && !PehkuiConfig.COMMON.vanillaScaleSyncBack.get())
		{
			final Entity entity = scaleData.getEntity();

			if (entity instanceof LivingEntity living)
			{
				return modifiedScale * living.getScale();
			}
		}

		return modifiedScale;
	}
}
