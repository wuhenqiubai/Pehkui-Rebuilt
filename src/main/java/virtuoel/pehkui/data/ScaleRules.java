package virtuoel.pehkui.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;

import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.predicates.entity.EntityTypePredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import virtuoel.pehkui.Pehkui;
import virtuoel.pehkui.api.PehkuiConfig;
import virtuoel.pehkui.api.ScaleRegistries;
import virtuoel.pehkui.api.ScaleType;

/**
 * Holds the loaded datapack scale rules and applies them to entities.
 *
 * Rules are parsed lazily: the reload listener stores the raw JSON and the
 * actual {@link EntityPredicate} values are decoded once a registry lookup
 * is available (i.e. after the server has started), since predicate codecs
 * need a {@link HolderLookup}.
 *
 * Safety: values are clamped to {@code scaleRuleMaxScale} (default 256) to
 * prevent absurd hitboxes, non-finite/negative values are rejected, and
 * rules are indexed by entity type so matching does not scan every rule for
 * every entity.
 */
public final class ScaleRules
{
	private static volatile HolderLookup.Provider registryLookup = null;
	private static Map<EntityType<?>, List<ScaleRule>> typeIndex = Map.of();
	private static List<ScaleRule> generalRules = List.of();
	private static List<JsonElement> pendingJson = List.of();

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
			pendingJson = List.of();
		}
	}

	/**
	 * Called by the reload listener. If a registry lookup is available the
	 * rules are decoded immediately, otherwise the raw JSON is kept until
	 * {@link #setRegistryLookup} is called.
	 */
	public static void reload(List<JsonElement> rawJson)
	{
		if (registryLookup == null)
		{
			pendingJson = rawJson;
			return;
		}

		final List<ScaleRule> parsed = parse(rawJson);
		buildIndex(parsed);
	}

	private static List<ScaleRule> parse(List<JsonElement> rawJson)
	{
		final List<ScaleRule> parsed = new ArrayList<>();

		final RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, registryLookup);

		for (final JsonElement element : rawJson)
		{
			try
			{
				if (!element.isJsonObject())
				{
					Pehkui.LOGGER.error("Failed to parse pehkui scale rule: root must be a JSON object, got {}", element);
					continue;
				}

				final JsonObject json = element.getAsJsonObject();
				final JsonElement conditionsElement = json.get("conditions");

				if (conditionsElement == null)
				{
					Pehkui.LOGGER.error("Failed to parse pehkui scale rule: missing 'conditions' field in {}", element);
					continue;
				}

				if (!resourceConditionsMatch(json, ops))
				{
					Pehkui.LOGGER.info("Skipping pehkui scale rule {} due to fabric:load_conditions", element);
					continue;
				}

				final EntityPredicate predicate = EntityPredicate.CODEC.parse(ops, conditionsElement).getOrThrow();
				final Map<ScaleType, Float> scales = parseScales(json);

				if (scales.isEmpty())
				{
					Pehkui.LOGGER.error("Failed to parse pehkui scale rule: no valid scales specified in {}", element);
					continue;
				}

				final int priority = json.has("priority") ? json.get("priority").getAsInt() : 0;
				final String name = json.has("name") ? json.get("name").getAsString() : null;
				final String description = json.has("description") ? json.get("description").getAsString() : null;
				final List<EntityType<?>> entityTypes = parseEntityTypes(ops, conditionsElement);

				parsed.add(new ScaleRule(predicate, scales, priority, name, description, entityTypes));
			}
			catch (Exception e)
			{
				Pehkui.LOGGER.error("Failed to parse pehkui scale rule: {}", element, e);
			}
		}

		parsed.sort((a, b) -> Integer.compare(b.getPriority(), a.getPriority()));

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

	@Nullable
	private static List<EntityType<?>> parseEntityTypes(RegistryOps<JsonElement> ops, JsonElement conditions)
	{
		if (!conditions.isJsonObject())
		{
			return null;
		}

		final JsonObject conditionsObj = conditions.getAsJsonObject();

		if (!conditionsObj.has("entity_type"))
		{
			return null;
		}

		try
		{
			final EntityTypePredicate typePredicate = EntityTypePredicate.CODEC.parse(ops, conditionsObj.get("entity_type")).getOrThrow();
			final List<EntityType<?>> types = new ArrayList<>();

			for (final Holder<EntityType<?>> holder : typePredicate.types())
			{
				types.add(holder.value());
			}

			return types;
		}
		catch (Exception e)
		{
			return null;
		}
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
			public <T> Optional<RegistryOps.RegistryInfo<T>> lookup(ResourceKey<? extends Registry<? extends T>> key)
			{
				final Optional<? extends HolderLookup.RegistryLookup<T>> registryLookup = provider.lookup((ResourceKey<? extends Registry<T>>) (ResourceKey<?>) key);

				return registryLookup.map(RegistryOps.RegistryInfo::fromRegistryLookup);
			}
		};
	}

	private static Map<ScaleType, Float> parseScales(JsonObject json)
	{
		final Map<ScaleType, Float> scales = new LinkedHashMap<>();
		final float maxScale = (float) (double) PehkuiConfig.COMMON.scaleRuleMaxScale.get();

		if (json.has("scales") && json.get("scales").isJsonObject())
		{
			final JsonObject scalesJson = json.getAsJsonObject("scales");

			for (final Map.Entry<String, JsonElement> entry : scalesJson.entrySet())
			{
				final ScaleType scaleType = getScaleType(entry.getKey());

				if (scaleType != null)
				{
					final float raw = entry.getValue().getAsFloat();

					if (!Float.isFinite(raw) || raw <= 0)
					{
						Pehkui.LOGGER.error("Failed to parse pehkui scale rule: invalid scale value '{}' for scale type '{}'", raw, entry.getKey());
						continue;
					}

					scales.put(scaleType, Math.min(raw, maxScale));
				}
			}
		}
		else if (json.has("scale_type") && json.has("value"))
		{
			final ScaleType scaleType = getScaleType(json.get("scale_type").getAsString());

			if (scaleType != null)
			{
				final float raw = json.get("value").getAsFloat();

				if (!Float.isFinite(raw) || raw <= 0)
				{
					Pehkui.LOGGER.error("Failed to parse pehkui scale rule: invalid scale value '{}' for scale type '{}'", raw, json.get("scale_type").getAsString());
				}
				else
				{
					scales.put(scaleType, Math.min(raw, maxScale));
				}
			}
		}

		return scales;
	}

	@Nullable
	private static ScaleType getScaleType(String id)
	{
		final Identifier typeId = Identifier.tryParse(id);

		return typeId == null ? null : ScaleRegistries.getEntry(ScaleRegistries.SCALE_TYPES, typeId);
	}

	/**
	 * Returns the highest priority rule matching the given entity, or null.
	 */
	@Nullable
	public static ScaleRule getApplicableRule(Entity entity, ServerLevel world)
	{
		final List<ScaleRule> typeRules = typeIndex.get(entity.getType());
		final int typeSize = typeRules == null ? 0 : typeRules.size();
		final int generalSize = generalRules.size();

		if (typeSize == 0)
		{
			for (final ScaleRule rule : generalRules)
			{
				if (rule.getPredicate().matches(world, entity.position(), entity))
				{
					return rule;
				}
			}

			return null;
		}

		final List<ScaleRule> candidates = new ArrayList<>(typeSize + generalSize);
		candidates.addAll(typeRules);
		candidates.addAll(generalRules);
		candidates.sort((a, b) -> Integer.compare(b.getPriority(), a.getPriority()));

		for (final ScaleRule rule : candidates)
		{
			if (rule.getPredicate().matches(world, entity.position(), entity))
			{
				return rule;
			}
		}

		return null;
	}

	public static boolean isEmpty()
	{
		return typeIndex.isEmpty() && generalRules.isEmpty();
	}

	private ScaleRules()
	{

	}
}
