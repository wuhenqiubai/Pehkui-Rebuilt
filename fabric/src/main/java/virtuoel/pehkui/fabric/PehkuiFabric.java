package virtuoel.pehkui.fabric;

import org.jetbrains.annotations.ApiStatus;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;
import virtuoel.pehkui.api.PehkuiConfig;
import virtuoel.pehkui.api.ScaleOperations;
import virtuoel.pehkui.api.ScaleTypes;
import virtuoel.pehkui.command.PehkuiEntitySelectorOptions;
import virtuoel.pehkui.data.ScaleRuleLoader;
import virtuoel.pehkui.data.ScaleRules;
import virtuoel.pehkui.network.ConfigSyncPayload;
import virtuoel.pehkui.network.DebugPayload;
import virtuoel.pehkui.network.ScalePayload;
import virtuoel.pehkui.util.CommandUtils;
import virtuoel.pehkui.util.ConfigSyncUtils;
import virtuoel.pehkui.util.GravityChangerCompatibility;
import virtuoel.pehkui.util.ImmersivePortalsCompatibility;
import virtuoel.pehkui.util.ModLoaderUtils;
import virtuoel.pehkui.util.MulticonnectCompatibility;
import virtuoel.pehkui.util.Platform;

/**
 * Fabric 平台入口。常量与 id 辅助在 {@code virtuoel.pehkui.Pehkui}（common）。
 */
@ApiStatus.Internal
public class PehkuiFabric implements ModInitializer
{
	public PehkuiFabric()
	{
		Platform.setInstance(new PlatformImpl());

		ScaleTypes.INVALID.getClass();
		ScaleOperations.NOOP.getClass();
		PehkuiConfig.BUILDER.config.get();
	}

	@Override
	public void onInitialize()
	{
		CommandUtils.registerArgumentTypes();

		PehkuiEntitySelectorOptions.register();

		CommandUtils.registerCommands();

		if (ModLoaderUtils.isModLoaded("fabric-networking-api-v1"))
		{
			ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
			{
				if (!handler.player.getGameProfile().equals(server.getSingleplayerProfile()))
				{
					ConfigSyncUtils.syncConfigs(handler);
				}
				else
				{
					ConfigSyncUtils.resetSyncedConfigs();
				}
			});

			PayloadTypeRegistry.clientboundPlay().register(ScalePayload.ID, ScalePayload.CODEC);
			PayloadTypeRegistry.clientboundPlay().register(ConfigSyncPayload.ID, ConfigSyncPayload.CODEC);
			PayloadTypeRegistry.clientboundPlay().register(DebugPayload.ID, DebugPayload.CODEC);
		}

		GravityChangerCompatibility.INSTANCE.getClass();
		ImmersivePortalsCompatibility.INSTANCE.getClass();
		MulticonnectCompatibility.INSTANCE.getClass();

		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new ScaleRuleLoader());

		ServerLifecycleEvents.SERVER_STARTED.register(server -> ScaleRules.setRegistryLookup(server.registryAccess()));
	}
}
