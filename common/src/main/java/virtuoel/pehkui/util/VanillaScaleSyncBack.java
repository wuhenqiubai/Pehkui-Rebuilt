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
 * Pehkui 的 {@code pehkui:base} 与原版 {@code minecraft:scale} 属性之间的反向同步，
 * 由 {@code vanillaScaleSyncBack} 开启。
 *
 * <p>开启后该属性成为 {@code pehkui:base} 的镜像：base 变化会写回属性（原版代码与其他
 * 读取该属性的 mod 就能看到 Pehkui 的缩放），外部对属性的改动每 tick 检出并折回 base。
 * 此模式下 {@link VanillaScaleModifier} 的折入被停用（见其配置守卫），避免镜像二次应用。
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
	 * 检出外部属性改动并折回 base。每实体每 tick 调用一次（来自 {@code EntityMixin} 的 tick 注入）。
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
