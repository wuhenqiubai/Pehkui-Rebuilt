package virtuoel.pehkui.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import virtuoel.pehkui.Pehkui;
import virtuoel.pehkui.api.PehkuiConfig;
import virtuoel.pehkui.api.ScaleOperations;
import virtuoel.pehkui.api.ScaleTypes;
import virtuoel.pehkui.command.PehkuiEntitySelectorOptions;
import virtuoel.pehkui.neoforge.network.PehkuiPacketHandler;
import virtuoel.pehkui.util.CommandUtils;
import virtuoel.pehkui.util.GravityChangerCompatibility;
import virtuoel.pehkui.util.ImmersivePortalsCompatibility;
import virtuoel.pehkui.util.MulticonnectCompatibility;
import virtuoel.pehkui.util.Platform;
import virtuoel.pehkui.util.VanillaScaleSyncBack;

@Mod(Pehkui.MOD_ID)
public class PehkuiNeoForge
{
	public PehkuiNeoForge(final IEventBus modEventBus, final ModContainer container)
	{
		Platform.setInstance(new PlatformImpl());

		ScaleTypes.INVALID.getClass();
		ScaleOperations.NOOP.getClass();

		// NeoForge 侧配置照上游形态：原生 ModConfigSpec（TOML），不接 KanosConfig / ConfigSyncPayload。
		// 三个 spec 在 PehkuiConfig 的静态初始化里构建（同时为各 scale type 注册 ClampingScaleModifier），
		// onLoad / onFileChange 的 @SubscribeEvent 挂在 mod 事件总线上。
		container.registerConfig(ModConfig.Type.CLIENT, PehkuiConfig.clientSpec);
		container.registerConfig(ModConfig.Type.SERVER, PehkuiConfig.serverSpec);
		container.registerConfig(ModConfig.Type.COMMON, PehkuiConfig.commonSpec);
		modEventBus.register(PehkuiConfig.class);

		CommandUtilsImpl.register(modEventBus);

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
}
