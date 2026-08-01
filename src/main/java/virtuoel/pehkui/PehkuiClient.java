package virtuoel.pehkui;

import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.MixinEnvironment;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import virtuoel.pehkui.api.ScaleRegistries;
import virtuoel.pehkui.network.ConfigSyncPacket;
import virtuoel.pehkui.network.ConfigSyncPayload;
import virtuoel.pehkui.network.DebugPacket;
import virtuoel.pehkui.network.DebugPayload;
import virtuoel.pehkui.network.ScalePacket;
import virtuoel.pehkui.network.ScalePayload;
import virtuoel.pehkui.server.command.DebugCommand;
import virtuoel.pehkui.util.I18nUtils;
import virtuoel.pehkui.util.ModLoaderUtils;
import virtuoel.pehkui.util.ScaleRenderUtils;
import virtuoel.pehkui.util.VersionUtils;

@ApiStatus.Internal
public class PehkuiClient implements ClientModInitializer
{
	@Override
	public void onInitializeClient()
	{
		if (ModLoaderUtils.isModLoaded("fabric-networking-api-v1"))
		{
			if (VersionUtils.MINOR > 20 || (VersionUtils.MINOR == 20 && VersionUtils.PATCH >= 5))
			{
				((Runnable) () -> {
					ClientPlayNetworking.registerGlobalReceiver(ScalePayload.ID, (payload, context) ->
					{
						handleScalePacket(context.client(), payload);
					});

					ClientPlayNetworking.registerGlobalReceiver(ConfigSyncPayload.ID, (payload, context) ->
					{
						context.client().execute(payload.action);
					});

					ClientPlayNetworking.registerGlobalReceiver(DebugPayload.ID, (payload, context) ->
					{
						handleDebugPacket(context.client(), payload.type);
					});
				}).run();
			}
			else
			{
				ScaleRenderUtils.registerPacketHandler(Pehkui.SCALE_PACKET, PehkuiClient.class, "handleScalePacket");
				ScaleRenderUtils.registerPacketHandler(Pehkui.CONFIG_SYNC_PACKET, PehkuiClient.class, "handleConfigSyncPacket");
				ScaleRenderUtils.registerPacketHandler(Pehkui.DEBUG_PACKET, PehkuiClient.class, "handleDebugPacket");
			}
		}
		else
		{
			Pehkui.LOGGER.error("Failed to register Pehkui's packet handlers! Is Fabric API's networking module missing?");
		}
	}
	
	public static void handleScalePacket(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, Object responseSender)
	{
		handleScalePacket(client, new ScalePacket(buf));
	}
	
	protected static void handleScalePacket(MinecraftClient client, ScalePacket packet)
	{
		client.execute(() ->
		{
			final Entity e = client.world.getEntityById(packet.entityId);
			
			if (e != null)
			{
				packet.syncedScales.forEach((typeId, scaleData) ->
				{
					if (ScaleRegistries.SCALE_TYPES.containsKey(typeId))
					{
						ScaleRegistries.getEntry(ScaleRegistries.SCALE_TYPES, typeId).getScaleData(e).readNbt(scaleData);
					}
				});
			}
		});
	}
	
	public static void handleConfigSyncPacket(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, Object responseSender)
	{
		client.execute(new ConfigSyncPacket(buf).action);
	}
	
	public static void handleDebugPacket(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, Object responseSender)
	{
		handleDebugPacket(client, new DebugPacket(buf).type);
	}
	
	protected static void handleDebugPacket(MinecraftClient client, DebugCommand.PacketType type)
	{
		client.execute(() ->
		{
			switch (type)
			{
				case MIXIN_AUDIT:
					client.player.sendMessage(I18nUtils.translate("commands.pehkui.debug.audit.start.client", "Starting Mixin environment audit (client)..."), false);
					MixinEnvironment.getCurrentEnvironment().audit();
					client.player.sendMessage(I18nUtils.translate("commands.pehkui.debug.audit.end.client", "Mixin environment audit (client) complete!"), false);
					
					break;
				case GARBAGE_COLLECT:
					System.gc();
					break;
				default:
					break;
			}
		});
	}
}
