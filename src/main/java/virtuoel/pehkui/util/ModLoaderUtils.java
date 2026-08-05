package virtuoel.pehkui.util;

import net.neoforged.fml.ModList;

public class ModLoaderUtils
{
	public static boolean isModLoaded(final String modId)
	{
		return ModList.get().isLoaded(modId);
	}
}
