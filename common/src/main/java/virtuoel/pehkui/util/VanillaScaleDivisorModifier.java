package virtuoel.pehkui.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import virtuoel.pehkui.api.PehkuiConfig;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleModifier;

/**
 * 把 {@link VanillaScaleModifier} 折进 {@code pehkui:base} 的原版
 * {@code minecraft:scale} 贡献除掉，使玩法类缩放（motion、reach、drops、
 * projectiles、explosions……）不受原版 scale 属性影响。
 * 这遵守原版契约：{@code scale} 只影响几何（hitbox / 模型 / 眼高），
 * 而 {@code pehkui:base} 继续折入该属性，所以几何类缩放仍能感知它。
 *
 * <p>当 {@code vanillaScaleAffectsGameplay} 开启（整体 Pehkui 语义：折入的原版
 * 缩放也驱动玩法类缩放）或 {@code vanillaScaleSyncBack} 开启（该属性是 base 的镜像，
 * 无需除掉）时失效。
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
