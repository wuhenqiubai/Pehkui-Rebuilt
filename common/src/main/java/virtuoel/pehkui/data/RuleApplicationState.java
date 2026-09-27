package virtuoel.pehkui.data;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleModifier;
import virtuoel.pehkui.api.ScaleRegistries;
import virtuoel.pehkui.api.ScaleType;

/**
 * Per-entity bookkeeping for datapack scale rules.
 * <p>
 * The key piece is {@link Baseline}: the value a scale type held the moment a rule first took it
 * over. Rules are re-evaluated every {@code scaleRuleCheckInterval} ticks, so every application
 * recomputes from that frozen baseline rather than from whatever the previous round wrote —
 * otherwise {@code multiply} / {@code add} would feed their own output back in and snowball.
 * <p>
 * This state is persisted to entity NBT. Without persistence an entity reload would forget its
 * baseline, re-snapshot the rule-written value as the baseline, and start snowballing anyway.
 */
public final class RuleApplicationState
{
	public static final String KEY_BASELINES = "baselines";
	public static final String KEY_MODIFIERS = "modifiers";

	private final Map<ScaleType, Baseline> baselines = new LinkedHashMap<>();
	private final Map<ScaleType, Set<ScaleModifier>> modifiers = new LinkedHashMap<>();

	public Map<ScaleType, Baseline> getBaselines()
	{
		return baselines;
	}

	public Map<ScaleType, Set<ScaleModifier>> getModifiers()
	{
		return modifiers;
	}

	public boolean isEmpty()
	{
		return baselines.isEmpty() && modifiers.isEmpty();
	}

	/**
	 * Returns the frozen baseline for this scale type, snapshotting the current state the first
	 * time the type is taken over by a rule.
	 */
	public Baseline getOrCreateBaseline(ScaleType type, ScaleData data)
	{
		return baselines.computeIfAbsent(type, t -> Baseline.of(data));
	}

	public CompoundTag writeNbt()
	{
		final CompoundTag tag = new CompoundTag();

		if (!baselines.isEmpty())
		{
			final CompoundTag baselinesTag = new CompoundTag();

			for (final Map.Entry<ScaleType, Baseline> entry : baselines.entrySet())
			{
				final ResourceLocation id = ScaleRegistries.getId(ScaleRegistries.SCALE_TYPES, entry.getKey());

				if (id != null)
				{
					baselinesTag.put(id.toString(), entry.getValue().writeNbt());
				}
			}

			if (!baselinesTag.isEmpty())
			{
				tag.put(KEY_BASELINES, baselinesTag);
			}
		}

		if (!modifiers.isEmpty())
		{
			final CompoundTag modifiersTag = new CompoundTag();

			for (final Map.Entry<ScaleType, Set<ScaleModifier>> entry : modifiers.entrySet())
			{
				final ResourceLocation id = ScaleRegistries.getId(ScaleRegistries.SCALE_TYPES, entry.getKey());

				if (id == null)
				{
					continue;
				}

				final ListTag list = new ListTag();

				for (final ScaleModifier modifier : entry.getValue())
				{
					final ResourceLocation modifierId = ScaleRegistries.getId(ScaleRegistries.SCALE_MODIFIERS, modifier);

					if (modifierId != null)
					{
						list.add(StringTag.valueOf(modifierId.toString()));
					}
				}

				if (!list.isEmpty())
				{
					modifiersTag.put(id.toString(), list);
				}
			}

			if (!modifiersTag.isEmpty())
			{
				tag.put(KEY_MODIFIERS, modifiersTag);
			}
		}

		return tag;
	}

	public void readNbt(CompoundTag tag)
	{
		baselines.clear();
		modifiers.clear();

		final CompoundTag baselinesTag = tag.contains(KEY_BASELINES, Tag.TAG_COMPOUND) ? tag.getCompound(KEY_BASELINES) : new CompoundTag();

		for (final String key : baselinesTag.getAllKeys())
		{
			final ScaleType type = ScaleRuleParser.getScaleType(key);

			if (type != null)
			{
				baselines.put(type, Baseline.readNbt(baselinesTag.getCompound(key)));
			}
		}

		final CompoundTag modifiersTag = tag.contains(KEY_MODIFIERS, Tag.TAG_COMPOUND) ? tag.getCompound(KEY_MODIFIERS) : new CompoundTag();

		for (final String key : modifiersTag.getAllKeys())
		{
			final ScaleType type = ScaleRuleParser.getScaleType(key);

			if (type == null)
			{
				continue;
			}

			final ListTag list = modifiersTag.getList(key, Tag.TAG_STRING);
			final Set<ScaleModifier> set = new LinkedHashSet<>();

			for (int i = 0; i < list.size(); i++)
			{
				final ResourceLocation id = ResourceLocation.tryParse(list.getString(i));
				final ScaleModifier modifier = id == null ? null : ScaleRegistries.getEntry(ScaleRegistries.SCALE_MODIFIERS, id);

				if (modifier != null)
				{
					set.add(modifier);
				}
			}

			if (!set.isEmpty())
			{
				modifiers.put(type, set);
			}
		}
	}

	/**
	 * The state a scale type held before any rule touched it.
	 */
	public static final class Baseline
	{
		private final float scale;
		private final int tickDelay;
		private final Float2FloatFunction easing;
		private final Boolean persist;

		private Baseline(float scale, int tickDelay, @Nullable Float2FloatFunction easing, @Nullable Boolean persist)
		{
			this.scale = scale;
			this.tickDelay = tickDelay;
			this.easing = easing;
			this.persist = persist;
		}

		public static Baseline of(ScaleData data)
		{
			return new Baseline(data.getBaseScale(), data.getScaleTickDelay(), data.getEasing(), data.getPersistence());
		}

		public float getScale()
		{
			return scale;
		}

		public void restore(ScaleData data)
		{
			data.setScale(scale);
			data.setScaleTickDelay(tickDelay);
			data.setEasing(easing);
			data.setPersistence(persist);
		}

		private CompoundTag writeNbt()
		{
			final CompoundTag tag = new CompoundTag();

			tag.putFloat("scale", scale);
			tag.putInt("delay", tickDelay);

			if (easing != null)
			{
				final ResourceLocation id = ScaleRegistries.getId(ScaleRegistries.SCALE_EASINGS, easing);

				if (id != null)
				{
					tag.put("easing", StringTag.valueOf(id.toString()));
				}
			}

			if (persist != null)
			{
				tag.putBoolean("persist", persist);
			}

			return tag;
		}

		private static Baseline readNbt(CompoundTag tag)
		{
			return new Baseline(
				tag.contains("scale") ? tag.getFloat("scale") : 1.0F,
				tag.contains("delay") ? tag.getInt("delay") : 0,
				readEasing(tag),
				tag.contains("persist") ? tag.getBoolean("persist") : null
			);
		}

		@Nullable
		private static Float2FloatFunction readEasing(CompoundTag tag)
		{
			if (!tag.contains("easing"))
			{
				return null;
			}

			// getEntry 直接就是 BiMap.get，null key 会抛 NPE，所以先判 id
			final ResourceLocation id = ResourceLocation.tryParse(tag.getString("easing"));

			return id == null ? null : ScaleRegistries.getEntry(ScaleRegistries.SCALE_EASINGS, id);
		}
	}
}
