package virtuoel.pehkui.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleBinaryOperator;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import net.minecraft.advancements.predicates.entity.EntityTypePredicate;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.entity.EntityType;
import virtuoel.pehkui.Pehkui;
import virtuoel.pehkui.api.ScaleModifier;
import virtuoel.pehkui.api.ScaleOperations;
import virtuoel.pehkui.api.ScaleRegistries;
import virtuoel.pehkui.api.ScaleType;

/**
 * JSON decoding shared by both loaders' {@code ScaleRules} copies.
 * <p>
 * The two platform files differ only in their resource-condition systems, so everything that
 * turns rule JSON into {@link ScaleRule} data lives here instead of being duplicated.
 */
public final class ScaleRuleParser
{
	/**
	 * Parses the {@code scales} map, falling back to the legacy {@code scale_type} + {@code value}
	 * pair. Each entry is either a plain number (shorthand for a {@code set}) or an object holding
	 * {@code operation} / {@code value} and the optional {@code delay} / {@code easing} /
	 * {@code persist} adjustments.
	 */
	public static Map<ScaleType, List<ScaleRuleOp>> parseScales(JsonObject json, Identifier fileId)
	{
		final Map<ScaleType, List<ScaleRuleOp>> scales = new LinkedHashMap<>();

		if (json.has("scales") && json.get("scales").isJsonObject())
		{
			for (final Map.Entry<String, JsonElement> entry : json.getAsJsonObject("scales").entrySet())
			{
				final ScaleType scaleType = parseScaleType(entry.getKey(), "scales", fileId);

				if (scaleType == null)
				{
					continue;
				}

				final ScaleRuleOp op = parseOp(entry.getValue(), "scales." + entry.getKey(), fileId);

				if (op != null)
				{
					scales.computeIfAbsent(scaleType, k -> new ArrayList<>()).add(op);
				}
			}
		}
		else if (json.has("scale_type") && json.has("value"))
		{
			final String typeId = json.get("scale_type").getAsString();
			final ScaleType scaleType = parseScaleType(typeId, "scale_type", fileId);

			if (scaleType != null)
			{
				// The legacy form keeps the operand at the top level; it may be a plain number or
				// the same object shape used inside "scales".
				final ScaleRuleOp op = parseOp(json.get("value"), "value", fileId);

				if (op != null)
				{
					scales.computeIfAbsent(scaleType, k -> new ArrayList<>()).add(op);
				}
			}
		}

		warnOnUnusableFold(scales, fileId);

		return scales;
	}

	/**
	 * Folds each scale type once starting from that type's default scale, and warns when the result
	 * is not a usable scale — so a rule that can never work is reported at load time rather than
	 * silently doing nothing in game.
	 * <p>
	 * This is a heads-up, not a verdict. The real fold starts from the <em>entity's</em> scale, so a
	 * value that is unusable at the default may still be fine for some entities, and a value that
	 * looks fine here can still fail for a differently-sized one. The authoritative check is the one
	 * in {@link ScaleRuleApplier}, which runs against the real baseline.
	 */
	private static void warnOnUnusableFold(Map<ScaleType, List<ScaleRuleOp>> scales, Identifier fileId)
	{
		for (final Map.Entry<ScaleType, List<ScaleRuleOp>> entry : scales.entrySet())
		{
			float probe = entry.getKey().getDefaultBaseScale();

			for (final ScaleRuleOp op : entry.getValue())
			{
				probe = op.apply(probe);
			}

			if (!Float.isFinite(probe) || probe <= 0.0F)
			{
				Pehkui.LOGGER.warn(
					"Rule in '{}' folds to {} for scale type '{}' starting from that type's default scale, which is not a usable scale. Affected entities keep their existing value for that type.",
					fileId, probe, ScaleRegistries.getId(ScaleRegistries.SCALE_TYPES, entry.getKey())
				);
			}
		}
	}

	/**
	 * Parses the optional {@code modifiers} map: scale type id to an array of registered
	 * {@code SCALE_MODIFIERS} ids.
	 */
	public static Map<ScaleType, List<ScaleModifier>> parseModifiers(JsonObject json, Identifier fileId)
	{
		final Map<ScaleType, List<ScaleModifier>> modifiers = new LinkedHashMap<>();

		if (!json.has("modifiers") || !json.get("modifiers").isJsonObject())
		{
			return modifiers;
		}

		for (final Map.Entry<String, JsonElement> entry : json.getAsJsonObject("modifiers").entrySet())
		{
			final ScaleType scaleType = parseScaleType(entry.getKey(), "modifiers", fileId);

			if (scaleType == null)
			{
				continue;
			}

			if (!entry.getValue().isJsonArray())
			{
				Pehkui.LOGGER.error("Expected an array of modifier ids for 'modifiers.{}' in '{}'.", entry.getKey(), fileId);
				continue;
			}

			final List<ScaleModifier> list = new ArrayList<>();

			for (final JsonElement element : entry.getValue().getAsJsonArray())
			{
				final String name = element.getAsString();
				final Identifier id = parseId(name);
				final ScaleModifier modifier = id == null ? null : ScaleRegistries.getEntry(ScaleRegistries.SCALE_MODIFIERS, id);

				if (modifier == null)
				{
					// Must be a registered modifier: ScaleData.toPacket only ships modifier ids, so
					// an unregistered one would silently vanish on the client and desync its size.
					Pehkui.LOGGER.error("Unknown scale modifier '{}' for 'modifiers.{}' in '{}'. Expected a registered modifier (e.g. 'pehkui:base_multiplier').", name, entry.getKey(), fileId);
					continue;
				}

				list.add(modifier);
			}

			if (!list.isEmpty())
			{
				modifiers.put(scaleType, list);
			}
		}

		return modifiers;
	}

	@Nullable
	private static ScaleRuleOp parseOp(JsonElement element, String path, Identifier fileId)
	{
		DoubleBinaryOperator operation = ScaleOperations.SET;
		int tickDelay = -1;
		Float2FloatFunction easing = null;
		Boolean persist = null;
		final float value;

		if (element.isJsonObject())
		{
			final JsonObject json = element.getAsJsonObject();

			if (json.has("operation"))
			{
				operation = parseOperation(json.get("operation").getAsString(), path, fileId);

				if (operation == null)
				{
					return null;
				}
			}

			if (!json.has("value"))
			{
				Pehkui.LOGGER.error("Missing required 'value' for '{}' in '{}'.", path, fileId);
				return null;
			}

			value = json.get("value").getAsFloat();

			if (json.has("delay"))
			{
				tickDelay = json.get("delay").getAsInt();

				if (tickDelay < 0)
				{
					Pehkui.LOGGER.error("Invalid 'delay' {} for '{}' in '{}'. Expected a non-negative tick count.", tickDelay, path, fileId);
					return null;
				}
			}

			if (json.has("easing"))
			{
				final String name = json.get("easing").getAsString();
				final Identifier id = parseId(name);
				easing = id == null ? null : ScaleRegistries.getEntry(ScaleRegistries.SCALE_EASINGS, id);

				if (easing == null)
				{
					Pehkui.LOGGER.error("Unknown easing '{}' for '{}' in '{}'. Expected a registered easing (e.g. 'pehkui:quadratic_out').", name, path, fileId);
					return null;
				}
			}

			if (json.has("persist"))
			{
				persist = json.get("persist").getAsBoolean();
			}
		}
		else
		{
			value = element.getAsFloat();
		}

		// Only the operand is checked here. Whether the *result* is a usable scale can only be
		// known once every rule has been folded in, so that check lives in ScaleRuleApplier.
		if (!Float.isFinite(value))
		{
			Pehkui.LOGGER.error("Invalid value '{}' for '{}' in '{}'. Expected a finite number.", value, path, fileId);
			return null;
		}

		return new ScaleRuleOp(operation, value, tickDelay, easing, persist);
	}

	@Nullable
	private static DoubleBinaryOperator parseOperation(String name, String path, Identifier fileId)
	{
		final Identifier id = parseId(name);

		if (id == null)
		{
			Pehkui.LOGGER.error("Invalid operation '{}' for '{}' in '{}'.", name, path, fileId);
			return null;
		}

		final DoubleBinaryOperator operation = ScaleRegistries.getEntry(ScaleRegistries.SCALE_OPERATIONS, id);

		// Mirrors the command's rule: 'noop' is the registry default and not a usable operator.
		if (operation == null || operation == ScaleOperations.NOOP)
		{
			Pehkui.LOGGER.error("Unknown operation '{}' for '{}' in '{}'. Expected a registered operation (e.g. 'set', 'multiply', 'pehkui:add').", name, path, fileId);
			return null;
		}

		return operation;
	}

	@Nullable
	private static ScaleType parseScaleType(String name, String path, Identifier fileId)
	{
		final ScaleType scaleType = getScaleType(name);

		if (scaleType == null)
		{
			Pehkui.LOGGER.error("Unknown scale type '{}' for '{}' in '{}'. Expected a registered type (e.g. 'pehkui:width').", name, path, fileId);
		}

		return scaleType;
	}

	@Nullable
	public static ScaleType getScaleType(String id)
	{
		final Identifier typeId = Identifier.tryParse(id);

		return typeId == null ? null : ScaleRegistries.getEntry(ScaleRegistries.SCALE_TYPES, typeId);
	}

	/**
	 * Resolves an entry name the way the commands do: a name without a namespace defaults to
	 * {@code pehkui}. Uses {@code tryParse} rather than {@code Pehkui.id} so a malformed name
	 * rejects just this entry instead of throwing out of the whole rule.
	 */
	@Nullable
	private static Identifier parseId(String name)
	{
		return Identifier.tryParse(name.contains(":") ? name : Pehkui.MOD_ID + ":" + name);
	}

	/**
	 * Extracts the concrete entity types from {@code conditions.entity_type} so rules can be
	 * indexed by type. Returns null when the predicate is a tag or otherwise not statically
	 * resolvable, which makes the rule a "general" one that is tested against every entity.
	 */
	@Nullable
	public static List<EntityType<?>> parseEntityTypes(RegistryOps<JsonElement> ops, JsonElement conditions)
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

	private ScaleRuleParser()
	{

	}
}
