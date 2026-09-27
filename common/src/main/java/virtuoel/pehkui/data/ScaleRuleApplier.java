package virtuoel.pehkui.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import net.minecraft.world.entity.Entity;
import virtuoel.pehkui.Pehkui;
import virtuoel.pehkui.api.PehkuiConfig;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleModifier;
import virtuoel.pehkui.api.ScaleRegistries;
import virtuoel.pehkui.api.ScaleType;
import virtuoel.pehkui.data.RuleApplicationState.Baseline;

/**
 * Applies the datapack scale rules that match an entity.
 * <p>
 * Shared by both loaders so the two platform {@code ScaleRules} copies cannot drift apart on the
 * part that actually mutates entities.
 * <p>
 * The rules are re-evaluated periodically ({@code scaleRuleCheckInterval} ticks), so the fold
 * always starts from the baseline captured when a rule first took the scale type over — never
 * from the value the previous round wrote. Feeding a relative operator its own output is exactly
 * how {@code multiply} / {@code add} would snowball to the clamp limit.
 */
public final class ScaleRuleApplier
{
	/**
	 * Scale types already reported as folding to an unusable value, for the lifetime of the
	 * process. Deliberately never cleared: a reload does not re-arm it, because the load-time check
	 * in {@link ScaleRuleParser} already re-reports per reload (with the offending file name), and
	 * the server parses the rules more than once while starting up.
	 */
	private static final Set<ScaleType> REPORTED_FOLDS = ConcurrentHashMap.newKeySet();

	/**
	 * @param previous the entity's current rule state, or null if it has none
	 * @param rules    every rule matching the entity, sorted by <em>ascending</em> priority so
	 *                 that later (higher priority) rules are folded last and win
	 * @return the new rule state, or null when no rule applies any more
	 */
	@Nullable
	public static RuleApplicationState apply(Entity entity, @Nullable RuleApplicationState previous, List<ScaleRule> rules)
	{
		final Map<ScaleType, List<ScaleRuleOp>> opsByType = new LinkedHashMap<>();
		final Map<ScaleType, List<ScaleModifier>> modifiersByType = new LinkedHashMap<>();

		for (final ScaleRule rule : rules)
		{
			for (final Map.Entry<ScaleType, List<ScaleRuleOp>> entry : rule.getScales().entrySet())
			{
				opsByType.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).addAll(entry.getValue());
			}

			for (final Map.Entry<ScaleType, List<ScaleModifier>> entry : rule.getModifiers().entrySet())
			{
				modifiersByType.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).addAll(entry.getValue());
			}
		}

		if (opsByType.isEmpty() && modifiersByType.isEmpty())
		{
			if (previous == null)
			{
				return null;
			}

			restoreAll(entity, previous);

			return null;
		}

		final RuleApplicationState state = previous != null ? previous : new RuleApplicationState();

		applyScales(entity, state, opsByType);
		applyModifiers(entity, state, modifiersByType);

		return state.isEmpty() ? null : state;
	}

	private static void applyScales(Entity entity, RuleApplicationState state, Map<ScaleType, List<ScaleRuleOp>> opsByType)
	{
		final Map<ScaleType, Baseline> baselines = state.getBaselines();

		// Scale types no longer covered by any rule go back to the value they had before a rule
		// took them over (not to the type default — that would throw away the entity's own scale).
		if (!baselines.isEmpty())
		{
			final Iterator<Map.Entry<ScaleType, Baseline>> iterator = baselines.entrySet().iterator();

			while (iterator.hasNext())
			{
				final Map.Entry<ScaleType, Baseline> entry = iterator.next();

				if (!opsByType.containsKey(entry.getKey()))
				{
					entry.getValue().restore(entry.getKey().getScaleData(entity));
					iterator.remove();
				}
			}
		}

		final float maxScale = (float) (double) PehkuiConfig.COMMON.scaleRuleMaxScale.get();

		for (final Map.Entry<ScaleType, List<ScaleRuleOp>> entry : opsByType.entrySet())
		{
			final ScaleType type = entry.getKey();
			final ScaleData data = type.getScaleData(entity);

			// Snapshot happens before anything is written, so it captures the pre-rule value.
			final Baseline baseline = state.getOrCreateBaseline(type, data);

			float result = baseline.getScale();
			ScaleRuleOp last = null;

			for (final ScaleRuleOp op : entry.getValue())
			{
				result = op.apply(result);
				last = op;
			}

			// Guard the *result*, not the input: "add -0.5" is a legal operand but
			// "1.0 - 2.0" is not a legal scale. Non-finite catches divide-by-zero too.
			//
			// This cannot be fully checked at load time: the fold starts from the entity's own
			// scale, so whether "subtract 5" is usable depends on how big that entity is.
			if (!Float.isFinite(result) || result <= 0.0F)
			{
				// Reported once per scale type per data reload. This runs for every matching entity
				// on every check cycle, so a single bad rule over a world with a few dozen mobs
				// otherwise prints an uninterrupted wall of identical lines. The load-time check in
				// ScaleRuleParser carries the which-file information instead.
				if (REPORTED_FOLDS.add(type))
				{
					Pehkui.LOGGER.error(
						"Scale rule folds to the unusable value {} for scale type '{}' (baseline {}). Leaving that scale type untouched. Further reports for this scale type are suppressed for the rest of this session.",
						result, ScaleRegistries.getId(ScaleRegistries.SCALE_TYPES, type), baseline.getScale()
					);
				}

				continue;
			}

			result = Math.min(result, maxScale);

			// Adjustments go first. setScale / setTargetScale pick up the tick delay and easing in
			// effect at the moment they are called, so setting them afterwards cannot affect the
			// transition that was just started — that is what made "delay" a no-op.
			if (last != null)
			{
				applyAdjustments(data, last);
			}

			if (last != null && last.getTickDelay() >= 0)
			{
				// The rule asked for a transition, so move the target and let ScaleData interpolate.
				//
				// Compared against targetScale rather than baseScale on purpose: baseScale changes
				// every tick while interpolating, so comparing on it would re-issue setTargetScale
				// on every check — and setTargetScale resets initialScale to the *current* target,
				// which snaps the animation to its end value immediately.
				if (Float.floatToIntBits(data.getTargetScale()) != Float.floatToIntBits(result))
				{
					data.setTargetScale(result);
				}
			}
			else if (Float.floatToIntBits(data.getBaseScale()) != Float.floatToIntBits(result))
			{
				// No delay requested — snap to the value, exactly as before.
				data.setScale(result);
			}
		}
	}

	private static void applyAdjustments(ScaleData data, ScaleRuleOp op)
	{
		final int tickDelay = op.getTickDelay();

		if (tickDelay >= 0 && data.getScaleTickDelay() != tickDelay)
		{
			data.setScaleTickDelay(tickDelay);
		}

		final Float2FloatFunction easing = op.getEasing();

		if (easing != null && data.getEasing() != easing)
		{
			data.setEasing(easing);
		}

		final Boolean persist = op.getPersist();

		if (persist != null && !persist.equals(data.getPersistence()))
		{
			data.setPersistence(persist);
		}
	}

	private static void applyModifiers(Entity entity, RuleApplicationState state, Map<ScaleType, List<ScaleModifier>> modifiersByType)
	{
		if (sameModifiers(state.getModifiers(), modifiersByType))
		{
			return;
		}

		for (final Map.Entry<ScaleType, Set<ScaleModifier>> entry : state.getModifiers().entrySet())
		{
			entry.getKey().getScaleData(entity).getBaseValueModifiers().removeAll(entry.getValue());
		}

		state.getModifiers().clear();

		for (final Map.Entry<ScaleType, List<ScaleModifier>> entry : modifiersByType.entrySet())
		{
			final Set<ScaleModifier> set = new LinkedHashSet<>(entry.getValue());

			entry.getKey().getScaleData(entity).getBaseValueModifiers().addAll(set);

			state.getModifiers().put(entry.getKey(), set);
		}
	}

	private static boolean sameModifiers(Map<ScaleType, Set<ScaleModifier>> current, Map<ScaleType, List<ScaleModifier>> next)
	{
		if (current.size() != next.size())
		{
			return false;
		}

		for (final Map.Entry<ScaleType, List<ScaleModifier>> entry : next.entrySet())
		{
			final Set<ScaleModifier> existing = current.get(entry.getKey());

			if (existing == null || !existing.equals(new LinkedHashSet<>(entry.getValue())))
			{
				return false;
			}
		}

		return true;
	}

	private static void restoreAll(Entity entity, RuleApplicationState state)
	{
		for (final Map.Entry<ScaleType, Baseline> entry : state.getBaselines().entrySet())
		{
			entry.getValue().restore(entry.getKey().getScaleData(entity));
		}

		for (final Map.Entry<ScaleType, Set<ScaleModifier>> entry : state.getModifiers().entrySet())
		{
			entry.getKey().getScaleData(entity).getBaseValueModifiers().removeAll(entry.getValue());
		}

		state.getBaselines().clear();
		state.getModifiers().clear();
	}

	private ScaleRuleApplier()
	{

	}
}
