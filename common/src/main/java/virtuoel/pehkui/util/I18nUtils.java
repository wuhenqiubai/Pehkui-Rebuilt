package virtuoel.pehkui.util;

import net.minecraft.network.chat.Component;

public class I18nUtils
{
	public static final Object[] EMPTY_VARARGS = new Object[0];

	private static final boolean RESOURCE_LOADER_LOADED = ModLoaderUtils.isModLoaded("fabric-resource-loader-v0");

	public static Component translate(final String unlocalized, final String defaultLocalized)
	{
		return translate(unlocalized, defaultLocalized, EMPTY_VARARGS);
	}

	public static Component translate(final String unlocalized, final String defaultLocalized, final Object... args)
	{
		if (RESOURCE_LOADER_LOADED)
		{
			return Component.translatable(unlocalized, args);
		}

		return literal(defaultLocalized, args);
	}

	public static Component literal(final String text, final Object... args)
	{
		return Component.literal(String.format(text, args));
	}
}
