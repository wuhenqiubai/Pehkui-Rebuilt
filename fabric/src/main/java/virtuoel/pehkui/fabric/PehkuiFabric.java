package virtuoel.pehkui.fabric;

import java.util.function.Supplier;

import com.mojang.brigadier.arguments.ArgumentType;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.resources.Identifier;
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
import virtuoel.pehkui.util.VanillaScaleSyncBack;

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
		CommandUtils.registerArgumentTypes(new CommandUtils.ArgumentTypeConsumer()
		{
			@Override
			public <T extends ArgumentType<?>> void register(Identifier id, Class<T> argClass, Supplier<T> supplier)
			{
				ArgumentTypeRegistry.registerArgumentType(id, argClass, SingletonArgumentInfo.contextFree(supplier));
			}
		});

		PehkuiEntitySelectorOptions.register();

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, dedicated) ->
			CommandUtils.registerCommands(dispatcher));

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

		VanillaScaleSyncBack.register();
	}
}
