package virtuoel.pehkui.data;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jspecify.annotations.NonNull;
import virtuoel.pehkui.Pehkui;

/**
 * Loads datapack scale rules from {@code data/<namespace>/pehkui_scale_rules/*.json}.
 * <p>
 * Guarded against JSON bombs: files larger than {@link #MAX_FILE_SIZE} are
 * skipped and parse errors (including stack overflows from deeply nested
 * JSON) are caught and skipped.
 */
public class ScaleRuleLoader implements SimpleSynchronousResourceReloadListener
{
	private static final String DIRECTORY = "pehkui_scale_rules";
	private static final int MAX_FILE_SIZE = 1_000_000;

	@Override
	public @NonNull Identifier getFabricId()
	{
		return Pehkui.id("scale_rules");
	}

	@Override
	public void onResourceManagerReload(ResourceManager manager)
	{
		final Map<Identifier, JsonElement> rawJson = new LinkedHashMap<>();

		for (final Map.Entry<Identifier, Resource> entry : manager.listResources(DIRECTORY, path -> path.getPath().endsWith(".json")).entrySet())
		{
			try (final InputStream input = entry.getValue().open())
			{
				final String content = new String(input.readAllBytes(), StandardCharsets.UTF_8);

				if (content.length() > MAX_FILE_SIZE)
				{
					Pehkui.LOGGER.error("Skipping pehkui scale rule file '{}': file too large ({} bytes)", entry.getKey(), content.length());
					continue;
				}

				rawJson.put(entry.getKey(), JsonParser.parseString(content));
			}
			catch (Throwable e)
			{
				Pehkui.LOGGER.error("Failed to read pehkui scale rule file '{}'", entry.getKey(), e);
			}
		}

		ScaleRules.reload(rawJson);
	}
}
