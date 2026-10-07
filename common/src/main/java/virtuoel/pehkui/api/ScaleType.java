package virtuoel.pehkui.api;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.ToDoubleBiFunction;

import org.jetbrains.annotations.ApiStatus;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
import net.minecraft.world.entity.Entity;
import virtuoel.pehkui.util.PehkuiEntityExtensions;
import virtuoel.pehkui.util.ReflectionUtils;
import virtuoel.pehkui.util.ScaleUtils;

// ==== PLATFORM-SPECIFIC BEGIN（class javadoc）====
/**
 * 注意：本类是 <b>common 侧的平台中性版本</b>，<b>不会被打进任何产物</b>
 * （{@code common/build.gradle} 的 {@code jar} 任务把它排除掉了）。
 *
 * <p>上游 Pehkui 的 {@code ScaleType} 三个事件成员在两个平台上类型不同：
 * Fabric 是 {@code net.fabricmc.fabric.api.event.Event<ScaleEventCallback>}，
 * NeoForge 是 {@code java.util.Collection<ScaleEventCallback>}。因此这里
 * <b>不含任何事件成员</b> —— common 内部的调用点一律经
 * {@link virtuoel.pehkui.util.Platform} 间接层转发，不直接触碰事件类型。
 *
 * <p>真正发货的是两份完整实现：
 * {@code fabric/src/main/java/virtuoel/pehkui/api/ScaleType.java}（上游 Fabric 原样）与
 * {@code neoforge/src/main/java/virtuoel/pehkui/api/ScaleType.java}（上游 NeoForge 原样）。
 * 三份之间只允许「事件成员那一段」不同，其余必须逐字一致（见仓库校验脚本）。
 */
// ==== PLATFORM-SPECIFIC END ====
public class ScaleType
{
	/**
	 * @see {@link ScaleType.Builder}
	 */
	private ScaleType(Builder builder)
	{
		this.defaultBaseScale = builder.defaultBaseScale;
		this.defaultTickDelay = builder.defaultTickDelay;
		this.defaultBaseValueModifiers = builder.defaultBaseValueModifiers;
		this.baseScaleClampFunction = builder.baseScaleClampFunction;
		this.targetScaleClampFunction = builder.targetScaleClampFunction;
		this.defaultPersistence = builder.defaultPersistence;
		this.defaultEasing = builder.defaultEasing;
		this.affectsDimensions = builder.affectsDimensions;
	}

	public ScaleData getScaleData(Entity entity)
	{
		return ((PehkuiEntityExtensions) entity).pehkui_getScaleData(this);
	}

	private boolean affectsDimensions;

	@ApiStatus.Internal
	public boolean getAffectsDimensions()
	{
		return affectsDimensions;
	}

	private boolean defaultPersistence;

	public void setDefaultPersistence(boolean persistent)
	{
		this.defaultPersistence = persistent;
	}

	public boolean getDefaultPersistence()
	{
		return defaultPersistence;
	}

	private Float2FloatFunction defaultEasing;

	public void setDefaultEasing(Float2FloatFunction defaultEasing)
	{
		this.defaultEasing = defaultEasing;
	}

	public Float2FloatFunction getDefaultEasing()
	{
		return defaultEasing;
	}

	private float defaultBaseScale;

	public final float getDefaultBaseScale()
	{
		return defaultBaseScale;
	}

	private int defaultTickDelay;

	public final int getDefaultTickDelay()
	{
		return defaultTickDelay;
	}

	private final ToDoubleBiFunction<ScaleData, Double> baseScaleClampFunction;

	public double clampBaseScale(ScaleData data, double newScale)
	{
		return baseScaleClampFunction.applyAsDouble(data, newScale);
	}

	private final ToDoubleBiFunction<ScaleData, Double> targetScaleClampFunction;

	public double clampTargetScale(ScaleData data, double newScale)
	{
		return targetScaleClampFunction.applyAsDouble(data, newScale);
	}

	private final Set<ScaleModifier> defaultBaseValueModifiers;

	/**
	 * Returns a mutable sorted set of scale modifiers. These modifiers are applied to all scale data of this type.
	 * @return Set of scale modifiers sorted by priority
	 */
	public Set<ScaleModifier> getDefaultBaseValueModifiers()
	{
		return defaultBaseValueModifiers;
	}

	public static class Builder
	{
		private Set<ScaleModifier> defaultBaseValueModifiers = new ObjectRBTreeSet<>();
		private float defaultBaseScale = 1.0F;
		private int defaultTickDelay = 20;
		private float defaultMinPositiveScale = ScaleUtils.DEFAULT_MINIMUM_POSITIVE_SCALE;
		private float defaultMaxPositiveScale = ScaleUtils.DEFAULT_MAXIMUM_POSITIVE_SCALE;
		private ToDoubleBiFunction<ScaleData, Double> baseScaleClampFunction = (scaleData, newScale) ->
		{
			if (newScale > defaultMaxPositiveScale)
			{
				return defaultMaxPositiveScale;
			}
			else if (newScale < -defaultMaxPositiveScale)
			{
				return -defaultMaxPositiveScale;
			}
			else if (newScale > defaultMinPositiveScale || newScale < -defaultMinPositiveScale)
			{
				return newScale;
			}

			return scaleData.getTargetScale() < 0 ? -defaultMinPositiveScale : defaultMinPositiveScale;
		};
		private ToDoubleBiFunction<ScaleData, Double> targetScaleClampFunction = (scaleData, newScale) ->
		{
			if (newScale > defaultMaxPositiveScale)
			{
				return defaultMaxPositiveScale;
			}
			else if (newScale < -defaultMaxPositiveScale)
			{
				return -defaultMaxPositiveScale;
			}
			else if (newScale > defaultMinPositiveScale || newScale < -defaultMinPositiveScale)
			{
				return newScale;
			}

			return newScale < 0 ? -defaultMinPositiveScale : defaultMinPositiveScale;
		};
		private boolean affectsDimensions = false;
		private Set<ScaleModifier> dependentModifiers = new ObjectRBTreeSet<>();
		private boolean defaultPersistence = false;
		private Float2FloatFunction defaultEasing = ScaleEasings.LINEAR;

		public static Builder create()
		{
			return new Builder();
		}

		private Builder()
		{

		}

		public Builder defaultBaseScale(float defaultBaseScale)
		{
			this.defaultBaseScale = defaultBaseScale;
			return this;
		}

		public Builder defaultTickDelay(int defaultTickDelay)
		{
			this.defaultTickDelay = defaultTickDelay;
			return this;
		}

		public Builder defaultMinPositiveScale(float defaultMinPositiveScale)
		{
			this.defaultMinPositiveScale = defaultMinPositiveScale;
			return this;
		}

		public Builder defaultMaxPositiveScale(float defaultMaxPositiveScale)
		{
			this.defaultMaxPositiveScale = defaultMaxPositiveScale;
			return this;
		}

		public Builder clampedBaseScale(ToDoubleBiFunction<ScaleData, Double> baseScaleClampFunction)
		{
			this.baseScaleClampFunction = baseScaleClampFunction;
			return this;
		}

		public Builder clampedTargetScale(ToDoubleBiFunction<ScaleData, Double> targetScaleClampFunction)
		{
			this.targetScaleClampFunction = targetScaleClampFunction;
			return this;
		}

		public Builder addBaseValueModifier(ScaleModifier scaleModifier)
		{
			this.defaultBaseValueModifiers.add(scaleModifier);
			return this;
		}

		public Builder defaultPersistence(boolean defaultPersistence)
		{
			this.defaultPersistence = defaultPersistence;
			return this;
		}

		public Builder defaultEasing(Float2FloatFunction defaultEasing)
		{
			this.defaultEasing = defaultEasing;
			return this;
		}

		public Builder affectsDimensions()
		{
			this.affectsDimensions = true;
			return this;
		}

		public Builder addDependentModifier(ScaleModifier scaleModifier)
		{
			this.dependentModifiers.add(scaleModifier);
			return this;
		}

		public ScaleType build()
		{
			final ScaleType type = new ScaleType(this);

			// ==== PLATFORM-SPECIFIC BEGIN（事件注册）====
			// 两份平台实现在这里注册 dependentModifiers 的回调（Fabric 用 .register(...)、
			// NeoForge 用 .add(...)）；本文件是编译占位、从不发货，不触碰事件类型，故此处留空。
			// 这不影响运行 —— 发货的是平台模块里那份完整实现。
			// ==== PLATFORM-SPECIFIC END ====

			return type;
		}

		private static ScaleEventCallback createScaleChangedEvent(final Collection<ScaleModifier> modifiers)
		{
			return s ->
			{
				final Entity e = s.getEntity();

				if (e != null)
				{
					boolean recalculateDimensions = s.getScaleType().getAffectsDimensions();

					if (!modifiers.isEmpty())
					{
						ScaleData data;
						for (ScaleType scaleType : ScaleRegistries.SCALE_TYPES.values())
						{
							data = scaleType.getScaleData(e);

							if (!Collections.disjoint(modifiers, data.getBaseValueModifiers()))
							{
								data.invalidateCachedScales();
								data.markForSync(true);
								recalculateDimensions |= scaleType.getAffectsDimensions();
							}
						}
					}

					if (recalculateDimensions)
					{
						final PehkuiEntityExtensions en = (PehkuiEntityExtensions) e;
						final boolean onGround = en.pehkui_getOnGround();

						e.refreshDimensions();

						ReflectionUtils.setOnGround(e, onGround);
					}
				}
			};
		}
	}
}
