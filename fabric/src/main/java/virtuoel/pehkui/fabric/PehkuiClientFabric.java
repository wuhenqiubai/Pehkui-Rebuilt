package virtuoel.pehkui.fabric;

import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.MixinEnvironment;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import virtuoel.pehkui.api.ScaleRegistries;
import virtuoel.pehkui.network.ConfigSyncPacket;
import virtuoel.pehkui.network.ConfigSyncPayload;
import virtuoel.pehkui.network.DebugPacket;
import virtuoel.pehkui.network.DebugPayload;
import virtuoel.pehkui.network.ScalePacket;
import virtuoel.pehkui.network.ScalePayload;
import virtuoel.pehkui.server.command.DebugCommand;
import virtuoel.pehkui.util.I18nUtils;

@ApiStatus.Internal
public class PehkuiClientFabric implements ClientModInitializer
{
	@Override
	public void onInitializeClient()
	{
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
	}

	public static void handleScalePacket(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, Object responseSender)
	{
		handleScalePacket(client, new ScalePacket(buf));
	}

	protected static void handleScalePacket(Minecraft client, ScalePacket packet)
	{
		client.execute(() ->
		{
			final Entity e = client.level.getEntity(packet.entityId);

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

	public static void handleConfigSyncPacket(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, Object responseSender)
	{
		client.execute(new ConfigSyncPacket(buf).action);
	}

	public static void handleDebugPacket(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, Object responseSender)
	{
		handleDebugPacket(client, new DebugPacket(buf).type);
	}

	protected static void handleDebugPacket(Minecraft client, DebugCommand.PacketType type)
	{
		client.execute(() ->
		{
			switch (type)
			{
				case MIXIN_AUDIT:
					client.player.sendSystemMessage(I18nUtils.translate("commands.pehkui.debug.audit.start.client", "Starting Mixin environment audit (client)..."));
					MixinEnvironment.getCurrentEnvironment().audit();
					client.player.sendSystemMessage(I18nUtils.translate("commands.pehkui.debug.audit.end.client", "Mixin environment audit (client) complete!"));

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
