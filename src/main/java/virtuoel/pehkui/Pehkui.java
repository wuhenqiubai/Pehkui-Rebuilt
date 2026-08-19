package virtuoel.pehkui;

import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.service.MixinService;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import virtuoel.pehkui.api.PehkuiConfig;
import virtuoel.pehkui.api.ScaleOperations;
import virtuoel.pehkui.api.ScaleTypes;
import virtuoel.pehkui.command.PehkuiEntitySelectorOptions;
import virtuoel.pehkui.network.PehkuiPacketHandler;
import virtuoel.pehkui.util.CommandUtils;
import virtuoel.pehkui.util.GravityChangerCompatibility;
import virtuoel.pehkui.util.ImmersivePortalsCompatibility;
import virtuoel.pehkui.util.MulticonnectCompatibility;
import virtuoel.pehkui.util.ReflectionUtils;
import virtuoel.pehkui.util.VanillaScaleSyncBack;

@ApiStatus.Internal
@Mod(Pehkui.MOD_ID)
public class Pehkui
{
	public static final String MOD_ID = "pehkui";

	public static final ILogger LOGGER = MixinService.getService().getLogger(MOD_ID);

	public Pehkui(final IEventBus modEventBus)
	{
		ScaleTypes.INVALID.getClass();
		ScaleOperations.NOOP.getClass();

		PehkuiConfig.BUILDER.config.get();

		CommandUtils.registerArgumentTypes(modEventBus);

		PehkuiEntitySelectorOptions.register();

		modEventBus.addListener(PehkuiPacketHandler::register);

		NeoForge.EVENT_BUS.register(this);
		NeoForge.EVENT_BUS.register(PehkuiEvents.class);

		VanillaScaleSyncBack.register();

		GravityChangerCompatibility.INSTANCE.getClass();
		ImmersivePortalsCompatibility.INSTANCE.getClass();
		MulticonnectCompatibility.INSTANCE.getClass();
	}

	@SubscribeEvent
	public void onRegisterCommands(RegisterCommandsEvent event)
	{
		CommandUtils.registerCommands(event.getDispatcher());
	}

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
