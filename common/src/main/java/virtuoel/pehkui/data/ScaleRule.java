package virtuoel.pehkui.data;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.world.entity.EntityType;
import virtuoel.pehkui.api.ScaleModifier;
import virtuoel.pehkui.api.ScaleType;

/**
 * A single datapack scale rule: entities matching the predicate get every scale type in
 * {@link #getScales()} folded through its operations, and every modifier in
 * {@link #getModifiers()} attached.
 * <p>
 * Every matching rule applies, in ascending priority order — higher priority rules run last,
 * so a plain {@code set} rule with the highest priority still wins outright. That keeps the
 * behaviour of pre-existing datapacks (which only ever used absolute values) unchanged.
 */
public class ScaleRule
{
	private final EntityPredicate predicate;
	private final Map<ScaleType, List<ScaleRuleOp>> scales;
	private final Map<ScaleType, List<ScaleModifier>> modifiers;
	private final int priority;
	private final String name;
	private final String description;
	private final List<EntityType<?>> entityTypes;

	public ScaleRule(EntityPredicate predicate, Map<ScaleType, List<ScaleRuleOp>> scales, Map<ScaleType, List<ScaleModifier>> modifiers, int priority, @Nullable String name, @Nullable String description, @Nullable List<EntityType<?>> entityTypes)
	{
		this.predicate = predicate;
		this.scales = scales;
		this.modifiers = modifiers;
		this.priority = priority;
		this.name = name;
		this.description = description;
		this.entityTypes = entityTypes;
	}

	public EntityPredicate getPredicate()
	{
		return predicate;
	}

	/**
	 * @return scale type to the operations applied to it, in JSON declaration order
	 */
	public Map<ScaleType, List<ScaleRuleOp>> getScales()
	{
		return scales;
	}

	/**
	 * @return scale type to the registered modifiers to attach to it
	 */
	public Map<ScaleType, List<ScaleModifier>> getModifiers()
	{
		return modifiers;
	}

	public int getPriority()
	{
		return priority;
	}

	@Nullable
	public String getName()
	{
		return name;
	}

	@Nullable
	public String getDescription()
	{
		return description;
	}

	@Nullable
	public List<EntityType<?>> getEntityTypes()
	{
		return entityTypes;
	}
}
