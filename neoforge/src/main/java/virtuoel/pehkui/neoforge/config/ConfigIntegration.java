package virtuoel.pehkui.neoforge.config;

import virtuoel.pehkui.config.PehkuiConfigScreen;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import virtuoel.pehkui.Pehkui;

/**
 * Client-only entry point that registers the Pehkui config screen with the
 * NeoForge mod list (Mods screen -> Pehkui -> config button).
 */
@Mod(value = Pehkui.MOD_ID, dist = Dist.CLIENT)
public class ConfigIntegration
{
	public ConfigIntegration(final ModContainer container)
	{
		container.registerExtensionPoint(IConfigScreenFactory.class,
			(modContainer, parent) -> new PehkuiConfigScreen(parent));
	}
}
