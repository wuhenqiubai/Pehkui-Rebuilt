package virtuoel.pehkui.neoforge.data;

import virtuoel.pehkui.data.ScaleRule;
import virtuoel.pehkui.data.ScaleRuleApplier;
import virtuoel.pehkui.data.ScaleRuleOp;
import virtuoel.pehkui.data.ScaleRuleParser;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;

import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.ICondition;
import virtuoel.pehkui.Pehkui;
import virtuoel.pehkui.api.ScaleModifier;
import virtuoel.pehkui.api.ScaleType;
import virtuoel.pehkui.util.PehkuiEntityExtensions;

/**
 * Holds the loaded datapack scale rules and matches them against entities.
 *
 * Rules are parsed lazily: the reload listener stores the raw JSON and the
 * actual {@link EntityPredicate} values are decoded once a registry lookup
 * is available (i.e. after the server has started), since predicate codecs
 * need a {@link HolderLookup}.
 *
 * Safety: values are clamped to {@code scaleRuleMaxScale} (default 256) to
 * prevent absurd hitboxes, results that are non-finite or non-positive are
 * rejected, and rules are indexed by entity type so matching does not scan
 * every rule for every entity.
 */
public final class ScaleRules
{
	private static final Comparator<ScaleRule> BY_ASCENDING_PRIORITY = Comparator.comparingInt(ScaleRule::getPriority);

	private static volatile HolderLookup.Provider registryLookup = null;
	private static volatile ICondition.IContext conditionContext = null;
	private static Map<EntityType<?>, List<ScaleRule>> typeIndex = Map.of();
	private static List<ScaleRule> generalRules = List.of();
	private static Map<Identifier, JsonElement> pendingJson = Map.of();

	/**
	 * Called once the server has started and a registry lookup is available.
	 * Parses any rules that were loaded but could not be decoded yet.
	 */
	public static void setRegistryLookup(HolderLookup.Provider lookup)
	{
		registryLookup = lookup;

		if (!pendingJson.isEmpty())
		{
			reload(pendingJson);
			pendingJson = Map.of();
		}
	}

	/**
	 * Called during data reload to cache the condition context used to
	 * evaluate {@code neoforge:conditions}.
	 */
	public static void setConditionContext(ICondition.IContext context)
	{
		conditionContext = context;
	}

	/**
	 * Called by the reload listener. If a registry lookup is available the
	 * rules are decoded immediately, otherwise the raw JSON is kept until
	 * {@link #setRegistryLookup} is called.
	 */
	public static void reload(Map<Identifier, JsonElement> rawJson)
	{
		if (registryLookup == null)
		{
			pendingJson = rawJson;
			return;
		}

		buildIndex(parse(rawJson));
	}

	private static List<ScaleRule> parse(Map<Identifier, JsonElement> rawJson)
	{
		final List<ScaleRule> parsed = new ArrayList<>();

		final RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, registryLookup);

		for (final Map.Entry<Identifier, JsonElement> raw : rawJson.entrySet())
		{
			final Identifier fileId = raw.getKey();
			final JsonElement element = raw.getValue();

			try
			{
				if (!element.isJsonObject())
				{
					Pehkui.LOGGER.error("Expected a JSON object at root of '{}'.", fileId);
					continue;
				}

				final JsonObject json = element.getAsJsonObject();
				final JsonElement conditionsElement = json.get("conditions");

				if (conditionsElement == null)
				{
					Pehkui.LOGGER.error("Missing required 'conditions' field in '{}'. Expected an EntityPredicate object (e.g. {{\"entity_type\": [\"minecraft:zombie\"]}}).", fileId);
					continue;
				}

				if (!resourceConditionsMatch(json, ops))
				{
					Pehkui.LOGGER.info("Skipping '{}' due to load conditions.", fileId);
					continue;
				}

				final EntityPredicate predicate = EntityPredicate.CODEC.parse(ops, conditionsElement).getOrThrow();
				final Map<ScaleType, List<ScaleRuleOp>> scales = ScaleRuleParser.parseScales(json, fileId);
				final Map<ScaleType, List<ScaleModifier>> modifiers = ScaleRuleParser.parseModifiers(json, fileId);

				if (scales.isEmpty() && modifiers.isEmpty())
				{
					Pehkui.LOGGER.error("No valid scales or modifiers in '{}'. Provide a 'scales' map (e.g. {{\"pehkui:width\": 0.5}}) or the legacy 'scale_type' + 'value' pair.", fileId);
					continue;
				}

				final int priority = json.has("priority") ? json.get("priority").getAsInt() : 0;
				final String name = json.has("name") ? json.get("name").getAsString() : null;
				final String description = json.has("description") ? json.get("description").getAsString() : null;
				final List<EntityType<?>> entityTypes = ScaleRuleParser.parseEntityTypes(ops, conditionsElement);

				parsed.add(new ScaleRule(predicate, scales, modifiers, priority, name, description, entityTypes));
			}
			catch (Exception e)
			{
				Pehkui.LOGGER.error("Failed to parse pehkui scale rule '{}': {}", fileId, element, e);
			}
		}

		// Ascending: the fold applies lower priority rules first so higher priority ones win.
		parsed.sort(BY_ASCENDING_PRIORITY);

		return parsed;
	}

	private static void buildIndex(List<ScaleRule> rules)
	{
		final Map<EntityType<?>, List<ScaleRule>> index = new HashMap<>();
		final List<ScaleRule> general = new ArrayList<>();

		for (final ScaleRule rule : rules)
		{
			final List<EntityType<?>> types = rule.getEntityTypes();

			if (types != null && !types.isEmpty())
			{
				for (final EntityType<?> type : types)
				{
					index.computeIfAbsent(type, k -> new ArrayList<>()).add(rule);
				}
			}
			else
			{
				general.add(rule);
			}
		}

		typeIndex = index;
		generalRules = general;
	}

	private static boolean resourceConditionsMatch(JsonObject json, RegistryOps<JsonElement> ops)
	{
		return neoforgeConditionsMatch(json, ops) && fabricLoadConditionsMatch(json);
	}

	private static boolean neoforgeConditionsMatch(JsonObject json, RegistryOps<JsonElement> ops)
	{
		if (!json.has(ConditionalOps.DEFAULT_CONDITIONS_KEY))
		{
			return true;
		}

		try
		{
			final List<ICondition> conditions = ICondition.LIST_CODEC.parse(ops, json.get(ConditionalOps.DEFAULT_CONDITIONS_KEY)).getOrThrow();
			final ICondition.IContext context = conditionContext != null ? conditionContext : ICondition.IContext.EMPTY;

			for (final ICondition condition : conditions)
			{
				if (!condition.test(context))
				{
					return false;
				}
			}

			return true;
		}
		catch (Exception e)
		{
			Pehkui.LOGGER.error("Failed to parse {} in {}", ConditionalOps.DEFAULT_CONDITIONS_KEY, json, e);
			return false;
		}
	}

	/**
	 * Lightweight compatibility for the Fabric {@code fabric:load_conditions}
	 * format, so datapacks work across both loaders. Only {@code all_mods_loaded}
	 * and {@code any_mods_loaded} are honored; other conditions are ignored.
	 */
	private static boolean fabricLoadConditionsMatch(JsonObject json)
	{
		if (!json.has("fabric:load_conditions"))
		{
			return true;
		}

		try
		{
			final JsonArray conditions = json.getAsJsonArray("fabric:load_conditions");

			for (final JsonElement element : conditions)
			{
				final JsonObject condition = element.getAsJsonObject();
				final String type = condition.get("condition").getAsString();

				if ("fabric:all_mods_loaded".equals(type))
				{
					for (final JsonElement mod : condition.getAsJsonArray("values"))
					{
						if (!ModList.get().isLoaded(mod.getAsString()))
						{
							return false;
						}
					}
				}
				else if ("fabric:any_mods_loaded".equals(type))
				{
					boolean any = false;

					for (final JsonElement mod : condition.getAsJsonArray("values"))
					{
						if (ModList.get().isLoaded(mod.getAsString()))
						{
							any = true;
							break;
						}
					}

					if (!any)
					{
						return false;
					}
				}
			}

			return true;
		}
		catch (Exception e)
		{
			Pehkui.LOGGER.error("Failed to parse fabric:load_conditions in {}", json, e);
			return false;
		}
	}

	/**
	 * Returns every rule matching the given entity, sorted by ascending priority.
	 * <p>
	 * All matches are returned rather than just the winner: the operations of every matching rule
	 * are folded in order, so a rule using {@code multiply} / {@code add} composes with the rest.
	 * With plain {@code set} rules the highest priority one still wins outright.
	 */
	public static List<ScaleRule> getMatchingRules(Entity entity, ServerLevel world)
	{
		final List<ScaleRule> typeRules = typeIndex.get(entity.getType());
		final int typeSize = typeRules == null ? 0 : typeRules.size();
		final int generalSize = generalRules.size();

		if (typeSize == 0 && generalSize == 0)
		{
			return List.of();
		}

		final List<ScaleRule> candidates = new ArrayList<>(typeSize + generalSize);

		if (typeSize > 0)
		{
			candidates.addAll(typeRules);
		}

		if (generalSize > 0)
		{
			candidates.addAll(generalRules);
			candidates.sort(BY_ASCENDING_PRIORITY);
		}

		final List<ScaleRule> matching = new ArrayList<>();

		for (final ScaleRule rule : candidates)
		{
			if (rule.getPredicate().matches(world, entity.position(), entity))
			{
				matching.add(rule);
			}
		}

		return matching;
	}

	/**
	 * Applies the matching rules to the given entity (or restores previously applied rule scales
	 * when nothing matches). When {@code canApply} is false (e.g. players while
	 * {@code scaleRulesAffectPlayers} is off), any previously applied rule scales are still
	 * restored.
	 */
	public static void applyRuleToEntity(Entity entity, ServerLevel world, boolean canApply)
	{
		final PehkuiEntityExtensions extensions = (PehkuiEntityExtensions) entity;
		final List<ScaleRule> rules = canApply ? getMatchingRules(entity, world) : List.of();

		extensions.pehkui_setRuleState(ScaleRuleApplier.apply(entity, extensions.pehkui_getRuleState(), rules));
	}

	public static boolean isEmpty()
	{
		return typeIndex.isEmpty() && generalRules.isEmpty();
	}

	private ScaleRules()
	{

	}
}
