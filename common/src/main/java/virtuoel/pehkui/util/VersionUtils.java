package virtuoel.pehkui.util;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.Version;

public class VersionUtils
{
	@Nullable
	public static final SemanticVersion MINECRAFT_VERSION = lookupMinecraftVersion();
	public static final int MAJOR = getVersionComponent(0);
	public static final int MINOR = getMinorComponent();
	public static final int PATCH = getVersionComponent(2);

	private static SemanticVersion lookupMinecraftVersion()
	{
		final Version version = FabricLoader.getInstance().getModContainer("minecraft").get().getMetadata().getVersion();

		return (SemanticVersion) (version instanceof SemanticVersion ? version : null);
	}

	private static int getVersionComponent(int pos)
	{
		return MINECRAFT_VERSION != null ? MINECRAFT_VERSION.getVersionComponent(pos) : -1;
	}
	
	private static int getMinorComponent()
	{
		final int component = getVersionComponent(1);
		// 26.x 起 Minecraft 彻底移除混淆（新版本格式），SemanticVersion.parse("26.1") 得 MINOR=1 会让版本判断全错。
		// 单版本架构下直接将 26.x 视为「现代版本」（MINOR>20 恒真、旧版本块恒 false）。
		return MAJOR > 21 ? 100 : component;
	}
}
