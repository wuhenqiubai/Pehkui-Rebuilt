package virtuoel.pehkui;

import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.service.MixinService;

import net.minecraft.resources.Identifier;
import virtuoel.pehkui.util.ReflectionUtils;

/**
 * common 常量和 id 辅助。平台入口（fabric / neoforge）各自持有其入口类（{@code PehkuiFabric} / {@code PehkuiNeoforge}），
 * 本站不含任何加载器初始化逻辑。
 */
@ApiStatus.Internal
public class Pehkui
{
	public static final String MOD_ID = "pehkui";

	public static final ILogger LOGGER = MixinService.getService().getLogger(MOD_ID);

	public static Identifier id(String path)
	{
		return ReflectionUtils.constructIdentifier(MOD_ID, path);
	}

	public static Identifier id(String path, String... paths)
	{
		return id(paths.length == 0 ? path : path + "/" + String.join("/", paths));
	}

	public static final Identifier SCALE_PACKET = id("scale");
	public static final Identifier CONFIG_SYNC_PACKET = id("config_sync");
	public static final Identifier DEBUG_PACKET = id("debug");
}
