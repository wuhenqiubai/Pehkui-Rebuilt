package virtuoel.pehkui.data;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import virtuoel.pehkui.Pehkui;

/**
 * Loads datapack scale rules from {@code data/<namespace>/pehkui_scale_rules/*.json}.
 */
public class ScaleRuleLoader implements SimpleSynchronousResourceReloadListener
{
	private static final String DIRECTORY = "pehkui_scale_rules";

	@Override
	public Identifier getFabricId()
	{
		return Pehkui.id("scale_rules");
	}

	@Override
	public void onResourceManagerReload(ResourceManager manager)
	{
		final List<JsonElement> rawJson = new ArrayList<>();

		for (final Map.Entry<Identifier, Resource> entry : manager.listResources(DIRECTORY, path -> path.getPath().endsWith(".json")).entrySet())
		{
			try (final BufferedReader reader = entry.getValue().openAsReader())
			{
				rawJson.add(JsonParser.parseReader(reader));
			}
			catch (Exception e)
			{
				Pehkui.LOGGER.error("Failed to read pehkui scale rule file '{}'", entry.getKey(), e);
			}
		}

		ScaleRules.reload(rawJson);
	}
}
