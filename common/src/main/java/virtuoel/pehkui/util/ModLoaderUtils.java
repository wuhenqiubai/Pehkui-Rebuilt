package virtuoel.pehkui.util;

public class ModLoaderUtils
{
	public static boolean isModLoaded(final String modId)
	{
		return Platform.INSTANCE.isModLoaded(modId);
	}
}
