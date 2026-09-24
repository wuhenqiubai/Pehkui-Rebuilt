package virtuoel.pehkui.data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;

import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import virtuoel.pehkui.Pehkui;
import virtuoel.pehkui.api.ScaleModifier;
import virtuoel.pehkui.api.ScaleType;

/**
 * Holds the loaded datapack scale rules and matches them against entities.
 *
 * Rules are parsed lazily: the reload listener stores the raw JSON and the
 * actual {@link EntityPredicate} values are decoded once a registry lookup
 * is available (i.e. after the server has started), since predicate codecs
 * need a {@link HolderLookup}.
 *
 * Safety: values are clamped to {@code scaleRuleMaxScale} (default 256) to
 * prevent absurd hitboxes, non-finite values are rejected, and rules are
 * indexed by entity type so matching does not scan every rule for every
 * entity.
 */
public final class ScaleRules
{
	private static final Comparator<ScaleRule> BY_ASCENDING_PRIORITY = Comparator.comparingInt(ScaleRule::getPriority);

	private static volatile HolderLookup.Provider registryLookup = null;
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
					Pehkui.LOGGER.info("Skipping '{}' due to fabric:load_conditions.", fileId);
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
		if (!json.has(ResourceConditions.CONDITIONS_KEY))
		{
			return true;
		}

		try
		{
			final List<ResourceCondition> conditions = ResourceCondition.LIST_CODEC.parse(ops, json.get(ResourceConditions.CONDITIONS_KEY)).getOrThrow();
			final RegistryOps.RegistryInfoLookup lookup = createRegistryInfoLookup(registryLookup);

			for (final ResourceCondition condition : conditions)
			{
				if (!condition.test(lookup))
				{
					return false;
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

	private static RegistryOps.RegistryInfoLookup createRegistryInfoLookup(HolderLookup.Provider provider)
	{
		return new RegistryOps.RegistryInfoLookup()
		{
			@SuppressWarnings("unchecked")
			@Override
			public <T> Optional<HolderGetter<T>> lookup(ResourceKey<? extends Registry<? extends T>> key)
			{
				// 26.3 起 RegistryOps.RegistryInfo 被移除，lookup 直接返回 HolderGetter
				// （HolderLookup.RegistryLookup 传递继承自 HolderGetter）
				return provider.lookup((ResourceKey<? extends Registry<T>>) (ResourceKey<?>) key)
					.<HolderGetter<T>>map(registryLookup -> registryLookup);
			}
		};
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

	public static boolean isEmpty()
	{
		return typeIndex.isEmpty() && generalRules.isEmpty();
	}

	private ScaleRules()
	{

	}
}
