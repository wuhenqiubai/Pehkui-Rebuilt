package virtuoel.pehkui.data;

import net.minecraft.advancements.predicates.entity.EntityPredicate;
import virtuoel.pehkui.api.ScaleType;

/**
 * A single datapack scale rule: entities matching the predicate get the
 * given scale type set to the given value. Rules with higher priority win
 * when multiple rules match the same entity.
 */
public class ScaleRule
{
	private final EntityPredicate predicate;
	private final ScaleType scaleType;
	private final float value;
	private final int priority;

	public ScaleRule(EntityPredicate predicate, ScaleType scaleType, float value, int priority)
	{
		this.predicate = predicate;
		this.scaleType = scaleType;
		this.value = value;
		this.priority = priority;
	}

	public EntityPredicate getPredicate()
	{
		return predicate;
	}

	public ScaleType getScaleType()
	{
		return scaleType;
	}

	public float getValue()
	{
		return value;
	}

	public int getPriority()
	{
		return priority;
	}
}
