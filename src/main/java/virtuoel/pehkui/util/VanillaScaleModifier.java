package virtuoel.pehkui.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import virtuoel.pehkui.api.PehkuiConfig;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleModifier;

/**
 * Folds the vanilla {@code minecraft:scale} attribute value into Pehkui's
 * {@code pehkui:base} scale, so Pehkui perceives the vanilla scale property
 * as part of its own global base scale. Consumers that had the vanilla scale
 * applied automatically (hitbox dimensions, model rendering) divide it out
 * again to avoid double application.
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
		if (PehkuiConfig.COMMON.applyVanillaScale.get())
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
		if (PehkuiConfig.COMMON.applyVanillaScale.get())
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
