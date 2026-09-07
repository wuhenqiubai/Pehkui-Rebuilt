package virtuoel.pehkui.util;

import java.util.WeakHashMap;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import virtuoel.pehkui.api.PehkuiConfig;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

/**
 * Reverse synchronization between Pehkui's {@code pehkui:base} and the vanilla
 * {@code minecraft:scale} attribute, enabled by {@code vanillaScaleSyncBack}.
 *
 * <p>When enabled the attribute becomes a mirror of {@code pehkui:base}: base
 * changes are written back to the attribute (so vanilla code and other mods
 * reading the attribute see Pehkui's scale), and external attribute changes
 * are detected each tick and folded back into base. {@link
 * VanillaScaleModifier}'s fold-in is disabled in this mode (see its config
 * guard) so the mirror does not double-apply.
 */
public final class VanillaScaleSyncBack
{
	private static final WeakHashMap<Entity, Float> WRITTEN = new WeakHashMap<>();

	private VanillaScaleSyncBack()
	{

	}

	public static void register()
	{
		ScaleTypes.BASE.getScaleChangedEvent().register(VanillaScaleSyncBack::writeBack);
	}

	/**
	 * Detects external attribute changes and folds them back into base.
	 * Called once per entity per tick (from {@code EntityMixin.pehkui$tick}).
	 */
	public static void tick(final Entity entity)
	{
		if (!PehkuiConfig.COMMON.vanillaScaleSyncBack.get() || !(entity instanceof LivingEntity living))
		{
			return;
		}

		final AttributeInstance attribute = living.getAttribute(Attributes.SCALE);

		if (attribute == null)
		{
			return;
		}

		final double current = attribute.getBaseValue();
		final Float written = WRITTEN.get(entity);

		if (written == null || current != written)
		{
			// 外部改了属性（或首次同步）：把属性值合并进 BASE，保持单一事实源。
			// 用 setScale（同时设 base + target）而不是 setBaseScale，否则 targetScale
			// 保持旧值会导致 BASE 在后续 tick 渐变回旧值，写回时覆盖外部写入。
			final float value = (float) current;

			ScaleTypes.BASE.getScaleData(living).setScale(value);
			WRITTEN.put(entity, value);
		}
	}

	private static void writeBack(final ScaleData scaleData)
	{
		if (!PehkuiConfig.COMMON.vanillaScaleSyncBack.get())
		{
			return;
		}

		final Entity entity = scaleData.getEntity();

		if (entity instanceof LivingEntity living)
		{
			final AttributeInstance attribute = living.getAttribute(Attributes.SCALE);

			if (attribute != null)
			{
				final float base = scaleData.getScale();

				attribute.setBaseValue(base);
				WRITTEN.put(entity, base);
			}
		}
	}
}
