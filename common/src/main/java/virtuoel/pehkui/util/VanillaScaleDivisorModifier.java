package virtuoel.pehkui.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import virtuoel.pehkui.api.PehkuiConfig;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleModifier;

/**
 * Divides out the vanilla {@code minecraft:scale} attribute contribution that
 * {@link VanillaScaleModifier} folded into {@code pehkui:base}, so gameplay
 * scale types (motion, reach, drops, projectiles, explosions, ...) stay
 * unaffected by the vanilla scale attribute. This respects the vanilla
 * contract that {@code scale} only affects geometry (hitbox / model / eye
 * height), while {@code pehkui:base} keeps folding the attribute in so
 * geometric scale types still see it.
 *
 * <p>Inactive when {@code vanillaScaleAffectsGameplay} is enabled (whole-Pehkui
 * semantics: the folded-in vanilla scale also drives gameplay types) or when
 * {@code vanillaScaleSyncBack} is enabled (the attribute is a mirror of base,
 * nothing to divide out).
 */
public class VanillaScaleDivisorModifier extends ScaleModifier
{
	public VanillaScaleDivisorModifier()
	{
		super(512.0F);
	}

	@Override
	public float modifyScale(final ScaleData scaleData, float modifiedScale, final float delta)
	{
		if (
			PehkuiConfig.COMMON.applyVanillaScale.get() &&
			!PehkuiConfig.COMMON.vanillaScaleAffectsGameplay.get() &&
			!PehkuiConfig.COMMON.vanillaScaleSyncBack.get()
		)
		{
			final Entity entity = scaleData.getEntity();

			if (entity instanceof LivingEntity living)
			{
				return modifiedScale / living.getScale();
			}
		}

		return modifiedScale;
	}

	@Override
	public float modifyPrevScale(final ScaleData scaleData, float modifiedScale)
	{
		if (
			PehkuiConfig.COMMON.applyVanillaScale.get() &&
			!PehkuiConfig.COMMON.vanillaScaleAffectsGameplay.get() &&
			!PehkuiConfig.COMMON.vanillaScaleSyncBack.get()
		)
		{
			final Entity entity = scaleData.getEntity();

			if (entity instanceof LivingEntity living)
			{
				return modifiedScale / living.getScale();
			}
		}

		return modifiedScale;
	}
}
