package virtuoel.pehkui.data;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.world.entity.EntityType;
import virtuoel.pehkui.api.ScaleType;

/**
 * A single datapack scale rule: entities matching the predicate get every
 * scale type in {@link #getScales()} set to the corresponding value. Rules
 * with higher priority win when multiple rules match the same entity.
 */
public class ScaleRule
{
	private final EntityPredicate predicate;
	private final Map<ScaleType, Float> scales;
	private final int priority;
	private final String name;
	private final String description;
	private final List<EntityType<?>> entityTypes;

	public ScaleRule(EntityPredicate predicate, Map<ScaleType, Float> scales, int priority, @Nullable String name, @Nullable String description, @Nullable List<EntityType<?>> entityTypes)
	{
		this.predicate = predicate;
		this.scales = scales;
		this.priority = priority;
		this.name = name;
		this.description = description;
		this.entityTypes = entityTypes;
	}

	public EntityPredicate getPredicate()
	{
		return predicate;
	}

	public Map<ScaleType, Float> getScales()
	{
		return scales;
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
