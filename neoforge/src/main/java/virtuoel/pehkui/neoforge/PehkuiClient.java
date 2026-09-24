package virtuoel.pehkui.neoforge;

import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.MixinEnvironment;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import virtuoel.pehkui.api.ScaleRegistries;
import virtuoel.pehkui.network.ConfigSyncPayload;
import virtuoel.pehkui.network.DebugPayload;
import virtuoel.pehkui.network.ScalePacket;
import virtuoel.pehkui.network.ScalePayload;
import virtuoel.pehkui.server.command.DebugCommand;
import virtuoel.pehkui.util.I18nUtils;

@ApiStatus.Internal
public class PehkuiClient
{
	public static void handleScalePacket(IPayloadContext context, ScalePayload packet)
	{
		handleScalePacket(Minecraft.getInstance(), packet);
	}

	public static void handleScalePacket(Minecraft client, ScalePacket packet)
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

	public static void handleConfigSyncPacket(IPayloadContext context, ConfigSyncPayload packet)
	{
		Minecraft.getInstance().execute(packet.action);
	}

	public static void handleDebugPacket(IPayloadContext context, DebugPayload packet)
	{
		handleDebugPacket(Minecraft.getInstance(), packet.type);
	}

	public static void handleDebugPacket(Minecraft client, DebugCommand.PacketType type)
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
