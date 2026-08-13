package virtuoel.pehkui.data;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;

import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import virtuoel.pehkui.Pehkui;
import virtuoel.pehkui.api.ScaleRegistries;
import virtuoel.pehkui.api.ScaleType;

/**
 * Holds the loaded datapack scale rules and applies them to entities.
 *
 * Rules are parsed lazily: the reload listener stores the raw JSON and the
 * actual {@link EntityPredicate} values are decoded once a registry lookup
 * is available (i.e. after the server has started), since predicate codecs
 * need a {@link HolderLookup}.
 */
public final class ScaleRules
{
	private static volatile HolderLookup.Provider registryLookup = null;
	private static List<ScaleRule> rules = List.of();
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
			rules = parse(pendingJson);
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
		if (registryLookup != null)
		{
			rules = parse(rawJson);
		}
		else
		{
			pendingJson = rawJson;
		}
	}

	private static List<ScaleRule> parse(List<JsonElement> rawJson)
	{
		final List<ScaleRule> parsed = new ArrayList<>();

		final RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, registryLookup);

		for (final JsonElement element : rawJson)
		{
			final JsonObject json = element.getAsJsonObject();
			final JsonElement conditionsElement = json.get("conditions");

			if (conditionsElement == null)
			{
				Pehkui.LOGGER.error("Failed to parse pehkui scale rule: missing 'conditions' field in {}", element);
				continue;
			}

			try
			{
				final EntityPredicate predicate = EntityPredicate.CODEC.parse(ops, conditionsElement).getOrThrow();
				final Identifier typeId = Identifier.tryParse(json.get("scale_type").getAsString());

				if (typeId == null)
				{
					Pehkui.LOGGER.error("Failed to parse pehkui scale rule: invalid scale_type in {}", element);
					continue;
				}

				final ScaleType scaleType = ScaleRegistries.getEntry(ScaleRegistries.SCALE_TYPES, typeId);

				if (scaleType == null)
				{
					Pehkui.LOGGER.error("Failed to parse pehkui scale rule: unknown scale_type '{}' in {}", typeId, element);
					continue;
				}

				final float value = json.get("value").getAsFloat();
				final int priority = json.has("priority") ? json.get("priority").getAsInt() : 0;

				parsed.add(new ScaleRule(predicate, scaleType, value, priority));
			}
			catch (Exception e)
			{
				Pehkui.LOGGER.error("Failed to parse pehkui scale rule: {}", element, e);
			}
		}

		parsed.sort((a, b) -> Integer.compare(b.getPriority(), a.getPriority()));

		return List.copyOf(parsed);
	}

	/**
	 * Returns the highest priority rule matching the given entity, or null.
	 */
	@Nullable
	public static ScaleRule getApplicableRule(Entity entity, ServerLevel world)
	{
		for (final ScaleRule rule : rules)
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
		return rules.isEmpty();
	}

	private ScaleRules()
	{

	}
}
