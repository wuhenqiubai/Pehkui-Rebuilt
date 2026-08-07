package virtuoel.pehkui.util;

import org.jetbrains.annotations.Nullable;

import net.neoforged.fml.loading.FMLLoader;

public class VersionUtils
{
	@Nullable
	public static final String MINECRAFT_VERSION = lookupMinecraftVersion();
	public static final int MAJOR = getVersionComponent(0);
	public static final int MINOR = getVersionComponent(1);
	public static final int PATCH = getVersionComponent(2);

	private static String lookupMinecraftVersion()
	{
		return FMLLoader.getCurrent().getVersionInfo().mcVersion();
	}

	private static int getVersionComponent(int pos)
	{
		if (MINECRAFT_VERSION != null)
		{
			final String[] parts = MINECRAFT_VERSION.split("\\.");

			if (parts.length > pos)
			{
				try
				{
					return Integer.parseInt(parts[pos]);
				}
				catch (NumberFormatException e)
				{
					// ignore
				}
			}
		}

		return -1;
	}
}
